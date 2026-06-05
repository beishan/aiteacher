package com.tutorassist.schedule.controller;

import com.tutorassist.common.PageResult;
import com.tutorassist.common.Result;
import com.tutorassist.schedule.dto.*;
import com.tutorassist.schedule.entity.Course;
import com.tutorassist.schedule.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "排课管理", description = "课程排课、日历、上课记录")
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "课程列表")
    @GetMapping
    public Result<PageResult<CourseVO>> listCourses(CourseQuery query) {
        return Result.success(courseService.listCourses(query));
    }

    @Operation(summary = "课程详情")
    @GetMapping("/{id}")
    public Result<CourseVO> getCourse(@PathVariable Long id) {
        return Result.success(courseService.getCourse(id));
    }

    @Operation(summary = "新增课程（支持周期排课）")
    @PostMapping
    public Result<List<CourseVO>> createCourse(Authentication authentication,
                                                @Valid @RequestBody CourseRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(courseService.createCourse(request, operatorId));
    }

    @Operation(summary = "更新课程")
    @PutMapping("/{id}")
    public Result<CourseVO> updateCourse(Authentication authentication,
                                          @PathVariable Long id,
                                          @Valid @RequestBody CourseRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(courseService.updateCourse(id, request, operatorId));
    }

    @Operation(summary = "删除课程")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCourse(Authentication authentication,
                                      @PathVariable Long id) {
        Long operatorId = (Long) authentication.getPrincipal();
        courseService.deleteCourse(id, operatorId);
        return Result.success();
    }

    @Operation(summary = "修改课程状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateCourseStatus(Authentication authentication,
                                            @PathVariable Long id,
                                            @RequestParam String status) {
        Long operatorId = (Long) authentication.getPrincipal();
        courseService.updateCourseStatus(id, status, operatorId);
        return Result.success();
    }

    @Operation(summary = "完成课程（生成上课记录）")
    @PostMapping("/{id}/complete")
    public Result<CourseRecordVO> completeCourse(Authentication authentication,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody CourseRecordRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(courseService.completeCourse(id, request, operatorId));
    }

    @Operation(summary = "课程上课记录")
    @GetMapping("/{id}/records")
    public Result<List<CourseRecordVO>> getCourseRecords(@PathVariable Long id) {
        return Result.success(courseService.getCourseRecords(id));
    }

    @Operation(summary = "日历视图数据")
    @GetMapping("/calendar")
    public Result<List<CourseVO>> getCalendarCourses(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String courseType) {
        return Result.success(courseService.getCalendarCourses(start, end, studentId, subject, courseType));
    }

    @Operation(summary = "冲突检测")
    @GetMapping("/conflicts")
    public Result<List<Course>> detectConflicts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) Long excludeId) {
        return Result.success(courseService.detectConflicts(start, end, excludeId));
    }
}
