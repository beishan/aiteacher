package com.tutorassist.schedule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorassist.schedule.entity.Course;
import com.tutorassist.schedule.entity.CourseRecord;
import com.tutorassist.schedule.mapper.CourseMapper;
import com.tutorassist.schedule.mapper.CourseRecordMapper;
import com.tutorassist.student.entity.FeeRecord;
import com.tutorassist.student.entity.StudentFee;
import com.tutorassist.student.mapper.FeeRecordMapper;
import com.tutorassist.student.mapper.StudentFeeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程自动结算服务
 * 定时扫描已过期但状态仍为 SCHEDULED 的课程，自动标记完成并生成费用记录
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseAutoSettlementService {

    private final CourseMapper courseMapper;
    private final CourseRecordMapper courseRecordMapper;
    private final StudentFeeMapper studentFeeMapper;
    private final FeeRecordMapper feeRecordMapper;

    /**
     * 每 5 分钟扫描一次已过期课程，自动完成结算
     */
    @Scheduled(fixedRate = 300000)
    public void autoSettleExpiredCourses() {
        // 查询所有 endTime 已过但状态仍为 SCHEDULED 的课程
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, "SCHEDULED")
                .lt(Course::getEndTime, LocalDateTime.now());
        List<Course> expiredCourses = courseMapper.selectList(wrapper);

        if (expiredCourses.isEmpty()) {
            return;
        }

        log.info("自动结算：发现 {} 门过期课程", expiredCourses.size());

        for (Course course : expiredCourses) {
            try {
                settleCourse(course);
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

        // 3. 查找匹配的课时费（同学生 + 同科目，状态为 ACTIVE）
        if (course.getStudentId() == null) {
            return;
        }

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

        // 4. 创建费用记录
        BigDecimal amount = fee.getUnitPrice() != null ? fee.getUnitPrice() : BigDecimal.ZERO;
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
}
