package com.tutorassist.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutorassist.common.PageResult;
import com.tutorassist.finance.dto.*;
import com.tutorassist.student.entity.FeeRecord;
import com.tutorassist.student.entity.Student;
import com.tutorassist.student.mapper.FeeRecordMapper;
import com.tutorassist.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceService {

    private final FeeRecordMapper feeRecordMapper;
    private final StudentMapper studentMapper;

    public PageResult<FinanceRecordVO> listRecords(FinanceRecordQuery query) {
        LambdaQueryWrapper<FeeRecord> wrapper = new LambdaQueryWrapper<>();

        if (query.getStudentId() != null) {
            wrapper.eq(FeeRecord::getStudentId, query.getStudentId());
        }
        if (StringUtils.hasText(query.getPaymentType())) {
            wrapper.eq(FeeRecord::getPaymentType, query.getPaymentType());
        }
        if (StringUtils.hasText(query.getPaymentMethod())) {
            wrapper.eq(FeeRecord::getPaymentMethod, query.getPaymentMethod());
        }
        if (query.getStartDate() != null) {
            wrapper.ge(FeeRecord::getPaymentDate, query.getStartDate());
        }
        if (query.getEndDate() != null) {
            wrapper.le(FeeRecord::getPaymentDate, query.getEndDate());
        }

        wrapper.orderByDesc(FeeRecord::getPaymentDate);

        Page<FeeRecord> page = feeRecordMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()),
                wrapper
        );

        // 批量获取学生姓名
        Set<Long> studentIds = page.getRecords().stream()
                .map(FeeRecord::getStudentId)
                .collect(Collectors.toSet());

        Map<Long, String> studentNameMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
            studentWrapper.in(Student::getId, studentIds);
            studentMapper.selectList(studentWrapper).forEach(
                    s -> studentNameMap.put(s.getId(), s.getName())
            );
        }

        Map<Long, String> finalNameMap = studentNameMap;
        List<FinanceRecordVO> records = page.getRecords().stream()
                .map(r -> toFinanceRecordVO(r, finalNameMap.getOrDefault(r.getStudentId(), "未知")))
                .toList();

        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public FinanceSummaryVO getSummary() {
        // 查询所有费用记录
        LambdaQueryWrapper<FeeRecord> allWrapper = new LambdaQueryWrapper<>();
        List<FeeRecord> allRecords = feeRecordMapper.selectList(allWrapper);

        // 总收入
        BigDecimal totalIncome = allRecords.stream()
                .filter(r -> "INCOME".equals(r.getPaymentType()))
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 总退款
        BigDecimal totalRefund = allRecords.stream()
                .filter(r -> "REFUND".equals(r.getPaymentType()))
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 本月收入
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        BigDecimal monthIncome = allRecords.stream()
                .filter(r -> "INCOME".equals(r.getPaymentType()))
                .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(monthStart))
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 上月收入
        LocalDateTime lastMonthStart = LocalDateTime.of(LocalDate.now().minusMonths(1).withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime lastMonthEnd = monthStart;
        BigDecimal lastMonthIncome = allRecords.stream()
                .filter(r -> "INCOME".equals(r.getPaymentType()))
                .filter(r -> r.getCreatedAt() != null
                        && r.getCreatedAt().isAfter(lastMonthStart)
                        && r.getCreatedAt().isBefore(lastMonthEnd))
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 环比增长率
        BigDecimal growthRate = BigDecimal.ZERO;
        if (lastMonthIncome.compareTo(BigDecimal.ZERO) > 0) {
            growthRate = monthIncome.subtract(lastMonthIncome)
                    .multiply(new BigDecimal("100"))
                    .divide(lastMonthIncome, 1, RoundingMode.HALF_UP);
        }

        return FinanceSummaryVO.builder()
                .totalIncome(totalIncome)
                .monthIncome(monthIncome)
                .lastMonthIncome(lastMonthIncome)
                .growthRate(growthRate)
                .totalRefund(totalRefund)
                .recordCount((long) allRecords.size())
                .build();
    }

    public List<MonthlyTrendVO> getMonthlyTrend(int months) {
        YearMonth now = YearMonth.now();
        YearMonth startMonth = now.minusMonths(months - 1);

        // 查询起始月份至今的所有记录
        LocalDate startDate = startMonth.atDay(1);
        LambdaQueryWrapper<FeeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(FeeRecord::getPaymentDate, startDate);
        List<FeeRecord> records = feeRecordMapper.selectList(wrapper);

        // 按月分组
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, List<FeeRecord>> grouped = records.stream()
                .collect(Collectors.groupingBy(r -> YearMonth.from(r.getPaymentDate()).format(formatter)));

        // 生成每个月的数据
        List<MonthlyTrendVO> trend = new ArrayList<>();
        for (int i = 0; i < months; i++) {
            YearMonth month = startMonth.plusMonths(i);
            String key = month.format(formatter);
            List<FeeRecord> monthRecords = grouped.getOrDefault(key, List.of());

            BigDecimal income = monthRecords.stream()
                    .filter(r -> "INCOME".equals(r.getPaymentType()))
                    .map(FeeRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal refund = monthRecords.stream()
                    .filter(r -> "REFUND".equals(r.getPaymentType()))
                    .map(FeeRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            trend.add(MonthlyTrendVO.builder()
                    .month(key)
                    .income(income)
                    .refund(refund)
                    .build());
        }

        return trend;
    }

    private FinanceRecordVO toFinanceRecordVO(FeeRecord record, String studentName) {
        return FinanceRecordVO.builder()
                .id(record.getId())
                .studentId(record.getStudentId())
                .studentName(studentName)
                .feeId(record.getFeeId())
                .amount(record.getAmount())
                .paymentType(record.getPaymentType())
                .paymentDate(record.getPaymentDate())
                .paymentMethod(record.getPaymentMethod())
                .note(record.getNote())
                .createdBy(record.getCreatedBy())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
