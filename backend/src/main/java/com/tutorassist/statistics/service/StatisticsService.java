package com.tutorassist.statistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorassist.schedule.entity.Course;
import com.tutorassist.schedule.mapper.CourseMapper;
import com.tutorassist.student.entity.FeeRecord;
import com.tutorassist.student.mapper.FeeRecordMapper;
import com.tutorassist.homework.entity.Homework;
import com.tutorassist.homework.mapper.HomeworkMapper;
import com.tutorassist.statistics.dto.*;
import com.tutorassist.student.entity.Student;
import com.tutorassist.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final HomeworkMapper homeworkMapper;
    private final FeeRecordMapper feeRecordMapper;

    public DashboardStats getDashboardStats() {
        // 学生统计
        Long totalStudents = studentMapper.selectCount(null);
        LambdaQueryWrapper<Student> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(Student::getStatus, "ACTIVE");
        Long activeStudents = studentMapper.selectCount(activeWrapper);

        // 今日课程
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.ge(Course::getStartTime, todayStart)
                .le(Course::getStartTime, todayEnd);
        Long todayCourses = courseMapper.selectCount(courseWrapper);

        // 待批作业
        LambdaQueryWrapper<Homework> homeworkWrapper = new LambdaQueryWrapper<>();
        homeworkWrapper.eq(Homework::getStatus, "SUBMITTED");
        Long pendingHomeworks = homeworkMapper.selectCount(homeworkWrapper);

        // 本月收入
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LambdaQueryWrapper<FeeRecord> feeWrapper = new LambdaQueryWrapper<>();
        feeWrapper.ge(FeeRecord::getCreatedAt, monthStart)
                .eq(FeeRecord::getPaymentType, "INCOME");
        List<FeeRecord> monthFees = feeRecordMapper.selectList(feeWrapper);
        BigDecimal monthRevenue = monthFees.stream()
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 最近课程
        LambdaQueryWrapper<Course> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.ge(Course::getStartTime, LocalDateTime.now())
                .orderByAsc(Course::getStartTime)
                .last("LIMIT 5");
        List<Course> recentCourses = courseMapper.selectList(recentWrapper);

        List<DashboardStats.RecentCourse> recentCourseList = recentCourses.stream()
                .map(c -> DashboardStats.RecentCourse.builder()
                        .id(c.getId())
                        .subject(c.getSubject())
                        .startTime(c.getStartTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")))
                        .status(c.getStatus())
                        .build())
                .toList();

        return DashboardStats.builder()
                .totalStudents(totalStudents)
                .activeStudents(activeStudents)
                .todayCourses(todayCourses)
                .pendingHomeworks(pendingHomeworks)
                .monthRevenue(monthRevenue)
                .recentCourses(recentCourseList)
                .pendingTasks(List.of())
                .build();
    }

    public RevenueStats getRevenueStats() {
        // 总收入
        LambdaQueryWrapper<FeeRecord> allWrapper = new LambdaQueryWrapper<>();
        allWrapper.eq(FeeRecord::getPaymentType, "INCOME");
        List<FeeRecord> allFees = feeRecordMapper.selectList(allWrapper);
        BigDecimal totalRevenue = allFees.stream()
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 本月收入
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        BigDecimal monthRevenue = allFees.stream()
                .filter(f -> f.getCreatedAt().isAfter(monthStart))
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 上月收入
        LocalDateTime lastMonthStart = LocalDateTime.of(LocalDate.now().minusMonths(1).withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime lastMonthEnd = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        BigDecimal lastMonthRevenue = allFees.stream()
                .filter(f -> f.getCreatedAt().isAfter(lastMonthStart) && f.getCreatedAt().isBefore(lastMonthEnd))
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 增长率
        BigDecimal growthRate = BigDecimal.ZERO;
        if (lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthRate = monthRevenue.subtract(lastMonthRevenue)
                    .multiply(new BigDecimal("100"))
                    .divide(lastMonthRevenue, 1, RoundingMode.HALF_UP);
        }

        return RevenueStats.builder()
                .totalRevenue(totalRevenue)
                .monthRevenue(monthRevenue)
                .lastMonthRevenue(lastMonthRevenue)
                .growthRate(growthRate)
                .monthlyData(List.of())
                .build();
    }

    public StudentStats getStudentStats() {
        Long totalStudents = studentMapper.selectCount(null);

        LambdaQueryWrapper<Student> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(Student::getStatus, "ACTIVE");
        Long activeStudents = studentMapper.selectCount(activeWrapper);

        LambdaQueryWrapper<Student> pausedWrapper = new LambdaQueryWrapper<>();
        pausedWrapper.eq(Student::getStatus, "PAUSED");
        Long pausedStudents = studentMapper.selectCount(pausedWrapper);

        LambdaQueryWrapper<Student> graduatedWrapper = new LambdaQueryWrapper<>();
        graduatedWrapper.eq(Student::getStatus, "GRADUATED");
        Long graduatedStudents = studentMapper.selectCount(graduatedWrapper);

        LambdaQueryWrapper<Student> lostWrapper = new LambdaQueryWrapper<>();
        lostWrapper.eq(Student::getStatus, "LOST");
        Long lostStudents = studentMapper.selectCount(lostWrapper);

        return StudentStats.builder()
                .totalStudents(totalStudents)
                .activeStudents(activeStudents)
                .pausedStudents(pausedStudents)
                .graduatedStudents(graduatedStudents)
                .lostStudents(lostStudents)
                .gradeDistribution(List.of())
                .subjectDistribution(List.of())
                .sourceDistribution(List.of())
                .build();
    }

    public CourseStats getCourseStats() {
        Long totalCourses = courseMapper.selectCount(null);

        LambdaQueryWrapper<Course> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(Course::getStatus, "COMPLETED");
        Long completedCourses = courseMapper.selectCount(completedWrapper);

        LambdaQueryWrapper<Course> cancelledWrapper = new LambdaQueryWrapper<>();
        cancelledWrapper.eq(Course::getStatus, "CANCELLED");
        Long cancelledCourses = courseMapper.selectCount(cancelledWrapper);

        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LambdaQueryWrapper<Course> thisMonthWrapper = new LambdaQueryWrapper<>();
        thisMonthWrapper.ge(Course::getStartTime, monthStart);
        Long thisMonthCourses = courseMapper.selectCount(thisMonthWrapper);

        return CourseStats.builder()
                .totalCourses(totalCourses)
                .completedCourses(completedCourses)
                .cancelledCourses(cancelledCourses)
                .thisMonthCourses(thisMonthCourses)
                .weeklyData(List.of())
                .build();
    }

    public HomeworkStats getHomeworkStats() {
        Long totalHomeworks = homeworkMapper.selectCount(null);

        LambdaQueryWrapper<Homework> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Homework::getStatus, "PENDING");
        Long pendingHomeworks = homeworkMapper.selectCount(pendingWrapper);

        LambdaQueryWrapper<Homework> submittedWrapper = new LambdaQueryWrapper<>();
        submittedWrapper.eq(Homework::getStatus, "SUBMITTED");
        Long submittedHomeworks = homeworkMapper.selectCount(submittedWrapper);

        LambdaQueryWrapper<Homework> gradedWrapper = new LambdaQueryWrapper<>();
        gradedWrapper.eq(Homework::getStatus, "GRADED");
        Long gradedHomeworks = homeworkMapper.selectCount(gradedWrapper);

        double completionRate = 0;
        if (totalHomeworks > 0) {
            completionRate = (double) (submittedHomeworks + gradedHomeworks) / totalHomeworks * 100;
        }

        return HomeworkStats.builder()
                .totalHomeworks(totalHomeworks)
                .pendingHomeworks(pendingHomeworks)
                .submittedHomeworks(submittedHomeworks)
                .gradedHomeworks(gradedHomeworks)
                .completionRate(completionRate)
                .subjectData(List.of())
                .build();
    }
}
