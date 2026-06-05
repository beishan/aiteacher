package com.tutorassist.statistics.controller;

import com.tutorassist.common.Result;
import com.tutorassist.statistics.dto.*;
import com.tutorassist.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "数据统计", description = "仪表盘、收入统计、学生分布等")
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "首页仪表盘数据")
    @GetMapping("/dashboard")
    public Result<DashboardStats> getDashboardStats() {
        return Result.success(statisticsService.getDashboardStats());
    }

    @Operation(summary = "收入统计")
    @GetMapping("/revenue")
    public Result<RevenueStats> getRevenueStats() {
        return Result.success(statisticsService.getRevenueStats());
    }

    @Operation(summary = "学生分布统计")
    @GetMapping("/students")
    public Result<StudentStats> getStudentStats() {
        return Result.success(statisticsService.getStudentStats());
    }

    @Operation(summary = "课时统计")
    @GetMapping("/courses")
    public Result<CourseStats> getCourseStats() {
        return Result.success(statisticsService.getCourseStats());
    }

    @Operation(summary = "作业统计")
    @GetMapping("/homework")
    public Result<HomeworkStats> getHomeworkStats() {
        return Result.success(statisticsService.getHomeworkStats());
    }
}
