package com.tutorassist.schedule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutorassist.common.PageResult;
import com.tutorassist.common.exception.BusinessException;
import com.tutorassist.schedule.dto.*;
import com.tutorassist.schedule.entity.Course;
import com.tutorassist.schedule.entity.CourseRecord;
import com.tutorassist.schedule.mapper.CourseMapper;
import com.tutorassist.schedule.mapper.CourseRecordMapper;
import com.tutorassist.student.entity.Student;
import com.tutorassist.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseMapper courseMapper;
    private final CourseRecordMapper courseRecordMapper;
    private final StudentMapper studentMapper;

    // ==================== 课程 CRUD ====================

    public PageResult<CourseVO> listCourses(CourseQuery query) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();

        if (query.getStudentId() != null) {
            wrapper.eq(Course::getStudentId, query.getStudentId());
        }
        if (query.getClassId() != null) {
            wrapper.eq(Course::getClassId, query.getClassId());
        }
        if (StringUtils.hasText(query.getSubject())) {
            wrapper.eq(Course::getSubject, query.getSubject());
        }
        if (StringUtils.hasText(query.getCourseType())) {
            wrapper.eq(Course::getCourseType, query.getCourseType());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Course::getStatus, query.getStatus());
        }
        if (query.getStartTimeFrom() != null) {
            wrapper.ge(Course::getStartTime, query.getStartTimeFrom());
        }
        if (query.getStartTimeTo() != null) {
            wrapper.le(Course::getStartTime, query.getStartTimeTo());
        }

        wrapper.orderByAsc(Course::getStartTime);

        Page<Course> page = courseMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()),
                wrapper
        );

        List<CourseVO> records = page.getRecords().stream()
                .map(this::toCourseVO)
                .toList();

        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public CourseVO getCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        return toCourseVO(course);
    }

    @Transactional
    public List<CourseVO> createCourse(CourseRequest request, Long operatorId) {
        // 冲突检测
        List<Course> conflicts = detectConflicts(request.getStartTime(), request.getEndTime(), null);
        if (!conflicts.isEmpty()) {
            throw new BusinessException("该时间段存在课程冲突：" + conflicts.get(0).getTitle());
        }

        List<Course> courses = new ArrayList<>();

        if ("NONE".equals(request.getRepeatType()) || request.getRepeatType() == null) {
            // 单次排课
            Course course = buildCourse(request);
            courseMapper.insert(course);
            courses.add(course);
        } else {
            // 周期排课
            courses = createRecurringCourses(request);
        }

        log.info("新增课程：{}，共{}节，操作人：{}", request.getTitle(), courses.size(), operatorId);

        return courses.stream().map(this::toCourseVO).toList();
    }

    @Transactional
    public CourseVO updateCourse(Long id, CourseRequest request, Long operatorId) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        // 冲突检测（排除自身）
        List<Course> conflicts = detectConflicts(request.getStartTime(), request.getEndTime(), id);
        if (!conflicts.isEmpty()) {
            throw new BusinessException("该时间段存在课程冲突：" + conflicts.get(0).getTitle());
        }

        course.setStudentId(request.getStudentId());
        course.setClassId(request.getClassId());
        course.setSubject(request.getSubject());
        course.setCourseType(request.getCourseType());
        course.setTitle(request.getTitle());
        course.setStartTime(request.getStartTime());
        course.setEndTime(request.getEndTime());
        course.setLocation(request.getLocation());
        course.setMeetingLink(request.getMeetingLink());
        course.setRemark(request.getRemark());
        course.setColor(request.getColor());

        courseMapper.updateById(course);
        log.info("更新课程：{}，操作人：{}", course.getTitle(), operatorId);

        return toCourseVO(course);
    }

    @Transactional
    public void deleteCourse(Long id, Long operatorId) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        // 如果是周期课程的父课程，删除所有子课程
        if (course.getParentCourseId() == null) {
            LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Course::getParentCourseId, id);
            List<Course> children = courseMapper.selectList(wrapper);
            children.forEach(c -> courseMapper.deleteById(c.getId()));
        }

        courseMapper.deleteById(id);
        log.info("删除课程：{}，操作人：{}", course.getTitle(), operatorId);
    }

    @Transactional
    public void updateCourseStatus(Long id, String status, Long operatorId) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        course.setStatus(status);
        courseMapper.updateById(course);
        log.info("更新课程状态：{} -> {}，操作人：{}", course.getTitle(), status, operatorId);
    }

    // ==================== 上课记录 ====================

    @Transactional
    public CourseRecordVO completeCourse(Long courseId, CourseRecordRequest request, Long operatorId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        CourseRecord record = new CourseRecord();
        record.setCourseId(courseId);
        record.setActualStartTime(request.getActualStartTime() != null ? request.getActualStartTime() : course.getStartTime());
        record.setActualEndTime(request.getActualEndTime() != null ? request.getActualEndTime() : course.getEndTime());
        record.setAttendanceStatus(request.getAttendanceStatus());
        record.setContentSummary(request.getContentSummary());
        record.setHomeworkAssigned(request.getHomeworkAssigned());
        record.setRemark(request.getRemark());

        courseRecordMapper.insert(record);

        // 更新课程状态为已完成
        course.setStatus("COMPLETED");
        courseMapper.updateById(course);

        log.info("完成课程：{}，操作人：{}", course.getTitle(), operatorId);

        return toCourseRecordVO(record);
    }

    public List<CourseRecordVO> getCourseRecords(Long courseId) {
        LambdaQueryWrapper<CourseRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseRecord::getCourseId, courseId)
                .orderByDesc(CourseRecord::getCreatedAt);

        return courseRecordMapper.selectList(wrapper).stream()
                .map(this::toCourseRecordVO)
                .toList();
    }

    // ==================== 日历视图 ====================

    public List<CourseVO> getCalendarCourses(LocalDateTime start, LocalDateTime end,
                                              Long studentId, String subject, String courseType) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Course::getStartTime, start)
                .le(Course::getStartTime, end);

        if (studentId != null) {
            wrapper.eq(Course::getStudentId, studentId);
        }
        if (StringUtils.hasText(subject)) {
            wrapper.eq(Course::getSubject, subject);
        }
        if (StringUtils.hasText(courseType)) {
            wrapper.eq(Course::getCourseType, courseType);
        }

        wrapper.orderByAsc(Course::getStartTime);

        return courseMapper.selectList(wrapper).stream()
                .map(this::toCourseVO)
                .toList();
    }

    // ==================== 冲突检测 ====================

    public List<Course> detectConflicts(LocalDateTime start, LocalDateTime end, Long excludeId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .and(inner -> inner.le(Course::getStartTime, start).gt(Course::getEndTime, start))
                .or(inner -> inner.lt(Course::getStartTime, end).ge(Course::getEndTime, end))
                .or(inner -> inner.ge(Course::getStartTime, start).le(Course::getEndTime, end))
        );
        wrapper.ne(Course::getStatus, "CANCELLED");

        if (excludeId != null) {
            wrapper.ne(Course::getId, excludeId);
        }

        return courseMapper.selectList(wrapper);
    }

    // ==================== 内部方法 ====================

    private Course buildCourse(CourseRequest request) {
        Course course = new Course();
        course.setStudentId(request.getStudentId());
        course.setClassId(request.getClassId());
        course.setSubject(request.getSubject());
        course.setCourseType(request.getCourseType());
        course.setTitle(request.getTitle());
        course.setStartTime(request.getStartTime());
        course.setEndTime(request.getEndTime());
        course.setLocation(request.getLocation());
        course.setMeetingLink(request.getMeetingLink());
        course.setRemark(request.getRemark());
        course.setColor(request.getColor());
        course.setStatus("SCHEDULED");
        return course;
    }

    private List<Course> createRecurringCourses(CourseRequest request) {
        List<Course> courses = new ArrayList<>();
        LocalDate endDate = request.getRepeatEndDate();
        if (endDate == null) {
            endDate = LocalDate.now().plusMonths(6); // 默认6个月
        }

        LocalDateTime currentStart = request.getStartTime();
        LocalDateTime currentEnd = request.getEndTime();
        long durationMinutes = ChronoUnit.MINUTES.between(currentStart, currentEnd);

        // 先创建父课程
        Course parentCourse = buildCourse(request);
        parentCourse.setRecurrenceRule(buildRecurrenceRule(request));
        parentCourse.setRecurrenceEndDate(endDate);
        courseMapper.insert(parentCourse);
        courses.add(parentCourse);

        // 生成子课程
        while (currentStart.toLocalDate().isBefore(endDate) || currentStart.toLocalDate().isEqual(endDate)) {
            // 计算下一次课程时间
            switch (request.getRepeatType()) {
                case "WEEKLY" -> currentStart = currentStart.plusWeeks(1);
                case "BIWEEKLY" -> currentStart = currentStart.plusWeeks(2);
                case "CUSTOM" -> currentStart = currentStart.plusDays(request.getRepeatInterval() != null ? request.getRepeatInterval() : 7);
                default -> { return courses; }
            }
            currentEnd = currentStart.plusMinutes(durationMinutes);

            if (currentStart.toLocalDate().isAfter(endDate)) {
                break;
            }

            // 冲突检测
            List<Course> conflicts = detectConflicts(currentStart, currentEnd, null);
            if (!conflicts.isEmpty()) {
                log.warn("跳过冲突课程：{}", currentStart);
                continue;
            }

            Course childCourse = buildCourse(request);
            childCourse.setStartTime(currentStart);
            childCourse.setEndTime(currentEnd);
            childCourse.setParentCourseId(parentCourse.getId());
            childCourse.setRecurrenceRule(parentCourse.getRecurrenceRule());
            courseMapper.insert(childCourse);
            courses.add(childCourse);
        }

        return courses;
    }

    private String buildRecurrenceRule(CourseRequest request) {
        return switch (request.getRepeatType()) {
            case "WEEKLY" -> "FREQ=WEEKLY;INTERVAL=1";
            case "BIWEEKLY" -> "FREQ=WEEKLY;INTERVAL=2";
            case "CUSTOM" -> "FREQ=DAILY;INTERVAL=" + (request.getRepeatInterval() != null ? request.getRepeatInterval() : 7);
            default -> "";
        };
    }

    private CourseVO toCourseVO(Course course) {
        String studentName = null;
        if (course.getStudentId() != null) {
            Student student = studentMapper.selectById(course.getStudentId());
            if (student != null) {
                studentName = student.getName();
            }
        }

        return CourseVO.builder()
                .id(course.getId())
                .studentId(course.getStudentId())
                .studentName(studentName)
                .classId(course.getClassId())
                .subject(course.getSubject())
                .courseType(course.getCourseType())
                .title(course.getTitle())
                .startTime(course.getStartTime())
                .endTime(course.getEndTime())
                .location(course.getLocation())
                .meetingLink(course.getMeetingLink())
                .recurrenceRule(course.getRecurrenceRule())
                .recurrenceEndDate(course.getRecurrenceEndDate())
                .parentCourseId(course.getParentCourseId())
                .status(course.getStatus())
                .remark(course.getRemark())
                .color(course.getColor())
                .createdAt(course.getCreatedAt())
                .build();
    }

    private CourseRecordVO toCourseRecordVO(CourseRecord record) {
        return CourseRecordVO.builder()
                .id(record.getId())
                .courseId(record.getCourseId())
                .actualStartTime(record.getActualStartTime())
                .actualEndTime(record.getActualEndTime())
                .attendanceStatus(record.getAttendanceStatus())
                .contentSummary(record.getContentSummary())
                .homeworkAssigned(record.getHomeworkAssigned())
                .remark(record.getRemark())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
