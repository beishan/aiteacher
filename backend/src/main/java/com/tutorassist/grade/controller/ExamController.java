package com.tutorassist.grade.controller;

import com.tutorassist.common.PageResult;
import com.tutorassist.common.Result;
import com.tutorassist.grade.dto.*;
import com.tutorassist.grade.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "成绩管理", description = "考试成绩记录、成绩分析")
@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @Operation(summary = "成绩列表")
    @GetMapping
    public Result<PageResult<ExamVO>> listExams(ExamQuery query) {
        return Result.success(examService.listExams(query));
    }

    @Operation(summary = "成绩详情")
    @GetMapping("/{id}")
    public Result<ExamVO> getExam(@PathVariable Long id) {
        return Result.success(examService.getExam(id));
    }

    @Operation(summary = "新增成绩")
    @PostMapping
    public Result<ExamVO> createExam(Authentication authentication,
                                      @Valid @RequestBody ExamRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(examService.createExam(request, operatorId));
    }

    @Operation(summary = "更新成绩")
    @PutMapping("/{id}")
    public Result<ExamVO> updateExam(Authentication authentication,
                                      @PathVariable Long id,
                                      @Valid @RequestBody ExamRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(examService.updateExam(id, request, operatorId));
    }

    @Operation(summary = "删除成绩")
    @DeleteMapping("/{id}")
    public Result<Void> deleteExam(Authentication authentication,
                                    @PathVariable Long id) {
        Long operatorId = (Long) authentication.getPrincipal();
        examService.deleteExam(id, operatorId);
        return Result.success();
    }

    @Operation(summary = "学生历次成绩")
    @GetMapping("/student/{studentId}")
    public Result<List<ExamVO>> getStudentExams(@PathVariable Long studentId) {
        return Result.success(examService.getStudentExams(studentId));
    }

    @Operation(summary = "学生成绩趋势")
    @GetMapping("/student/{studentId}/trend")
    public Result<List<TrendVO>> getStudentTrend(@PathVariable Long studentId) {
        return Result.success(examService.getStudentTrend(studentId));
    }
}
