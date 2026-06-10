package com.tutorassist.schedule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorassist.schedule.entity.Course;
import com.tutorassist.schedule.entity.CourseRecord;
import com.tutorassist.schedule.mapper.CourseMapper;
import com.tutorassist.schedule.mapper.CourseRecordMapper;
import com.tutorassist.student.dto.PriceTier;
import com.tutorassist.student.entity.FeeRecord;
import com.tutorassist.student.entity.StudentFee;
import com.tutorassist.student.mapper.FeeRecordMapper;
import com.tutorassist.student.mapper.StudentFeeMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 课程自动结算服务
 * 定时扫描已过期但状态仍为 SCHEDULED 的课程，自动标记完成并生成费用记录
 */
@Slf4j
@Service
public class CourseAutoSettlementService {

    private final CourseMapper courseMapper;
    private final CourseRecordMapper courseRecordMapper;
    private final StudentFeeMapper studentFeeMapper;
    private final FeeRecordMapper feeRecordMapper;
    private final ObjectMapper objectMapper;

    /**
     * 自引用，确保 @Transactional 通过代理调用生效
     */
    @Lazy
    @Autowired
    private CourseAutoSettlementService self;

    public CourseAutoSettlementService(CourseMapper courseMapper,
                                       CourseRecordMapper courseRecordMapper,
                                       StudentFeeMapper studentFeeMapper,
                                       FeeRecordMapper feeRecordMapper,
                                       ObjectMapper objectMapper) {
        this.courseMapper = courseMapper;
        this.courseRecordMapper = courseRecordMapper;
        this.studentFeeMapper = studentFeeMapper;
        this.feeRecordMapper = feeRecordMapper;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        log.info("课程自动结算服务已启动，每 5 分钟扫描过期课程");
    }

    /**
     * 每 5 分钟扫描一次已过期课程，自动完成结算
     */
    @Scheduled(fixedRate = 300000)
    public void autoSettleExpiredCourses() {
        log.debug("自动结算：开始扫描过期课程...");

        // 查询所有 endTime 已过但状态仍为 SCHEDULED 的课程
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, "SCHEDULED")
                .lt(Course::getEndTime, LocalDateTime.now());
        List<Course> expiredCourses = courseMapper.selectList(wrapper);

        if (expiredCourses.isEmpty()) {
            log.debug("自动结算：未发现过期课程");
            return;
        }

        log.info("自动结算：发现 {} 门过期课程", expiredCourses.size());

        for (Course course : expiredCourses) {
            try {
                self.settleCourse(course);
            } catch (Exception e) {
                log.error("自动结算课程失败：courseId={}, title={}, error={}",
                        course.getId(), course.getTitle(), e.getMessage(), e);
            }
        }
    }

    @Transactional
    public void settleCourse(Course course) {
        // 1. 创建上课记录
        CourseRecord record = new CourseRecord();
        record.setCourseId(course.getId());
        record.setActualStartTime(course.getStartTime());
        record.setActualEndTime(course.getEndTime());
        record.setAttendanceStatus("PRESENT");
        courseRecordMapper.insert(record);

        // 2. 更新课程状态为已完成
        course.setStatus("COMPLETED");
        courseMapper.updateById(course);

        log.info("自动结算：课程 [{}] 已标记完成", course.getTitle());

        // 3. 结算费用
        settleCourseFee(course);
    }

    /**
     * 为已完成的课程生成费用记录并扣减课时
     * 可被定时任务和手动完成课程共同调用
     */
    @Transactional
    public void settleCourseFee(Course course) {
        if (course.getStudentId() == null) {
            return;
        }

        // 查找匹配的课时费（同学生 + 同科目，状态为 ACTIVE）

        LambdaQueryWrapper<StudentFee> feeWrapper = new LambdaQueryWrapper<>();
        feeWrapper.eq(StudentFee::getStudentId, course.getStudentId())
                .eq(StudentFee::getStatus, "ACTIVE");

        // 优先匹配同科目的课时费
        if (course.getSubject() != null) {
            feeWrapper.eq(StudentFee::getSubject, course.getSubject());
        }

        StudentFee fee = studentFeeMapper.selectOne(feeWrapper);

        // 如果没找到同科目的，尝试找不区分科目的课时费
        if (fee == null && course.getSubject() != null) {
            LambdaQueryWrapper<StudentFee> fallbackWrapper = new LambdaQueryWrapper<>();
            fallbackWrapper.eq(StudentFee::getStudentId, course.getStudentId())
                    .eq(StudentFee::getStatus, "ACTIVE")
                    .and(w -> w.isNull(StudentFee::getSubject).or().eq(StudentFee::getSubject, ""));
            fee = studentFeeMapper.selectOne(fallbackWrapper);
        }

        if (fee == null) {
            log.info("自动结算：课程 [{}] 未找到匹配的课时费，跳过费用扣减", course.getTitle());
            return;
        }

        // 4. 计算费用金额（优先按阶梯价格，回退到 unitPrice）
        BigDecimal amount = resolvePrice(fee, course);
        FeeRecord feeRecord = new FeeRecord();
        feeRecord.setStudentId(course.getStudentId());
        feeRecord.setFeeId(fee.getId());
        feeRecord.setAmount(amount);
        feeRecord.setPaymentType("INCOME");
        feeRecord.setPaymentDate(LocalDate.now());
        feeRecord.setNote("自动结算: " + course.getTitle());
        feeRecordMapper.insert(feeRecord);

        // 5. 扣减剩余课时
        if (fee.getRemainingCount() != null && fee.getRemainingCount() > 0) {
            fee.setRemainingCount(fee.getRemainingCount() - 1);
            studentFeeMapper.updateById(fee);
            log.info("自动结算：课时费 [{}] 剩余课时 -> {}", fee.getId(), fee.getRemainingCount());

            // 6. 剩余课时为 0 时标记过期
            if (fee.getRemainingCount() == 0) {
                fee.setStatus("EXPIRED");
                studentFeeMapper.updateById(fee);
                log.info("自动结算：课时费 [{}] 已标记为过期（课时用尽）", fee.getId());
            }
        }
    }

    /**
     * 根据课程时长匹配阶梯价格，无匹配则回退到 unitPrice
     */
    private BigDecimal resolvePrice(StudentFee fee, Course course) {
        // 尝试阶梯价格
        if (StringUtils.hasText(fee.getPriceTiers()) && course.getStartTime() != null && course.getEndTime() != null) {
            try {
                List<PriceTier> tiers = objectMapper.readValue(fee.getPriceTiers(), new TypeReference<>() {});
                double hours = Duration.between(course.getStartTime(), course.getEndTime()).toMinutes() / 60.0;

                // 取 hours <= 实际时长 中最大的阶梯
                PriceTier matched = tiers.stream()
                        .filter(t -> t.getHours() <= hours)
                        .max(Comparator.comparingDouble(PriceTier::getHours))
                        .orElse(null);

                if (matched != null && matched.getPrice() != null) {
                    log.info("自动结算：课程时长 {}h，匹配阶梯价格 {}元/{}h", hours, matched.getPrice(), matched.getHours());
                    return matched.getPrice();
                }
            } catch (Exception e) {
                log.warn("解析阶梯价格失败：{}", e.getMessage());
            }
        }

        // 回退到 unitPrice
        return fee.getUnitPrice() != null ? fee.getUnitPrice() : BigDecimal.ZERO;
    }
}
