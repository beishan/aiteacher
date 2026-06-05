package com.tutorassist.homework.controller;

import com.tutorassist.common.PageResult;
import com.tutorassist.common.Result;
import com.tutorassist.homework.dto.*;
import com.tutorassist.homework.service.HomeworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "作业管理", description = "作业创建、提交、批改")
@RestController
@RequestMapping("/api/v1/homeworks")
@RequiredArgsConstructor
public class HomeworkController {

    private final HomeworkService homeworkService;

    @Operation(summary = "作业列表")
    @GetMapping
    public Result<PageResult<HomeworkVO>> listHomeworks(HomeworkQuery query) {
        return Result.success(homeworkService.listHomeworks(query));
    }

    @Operation(summary = "作业详情")
    @GetMapping("/{id}")
    public Result<HomeworkVO> getHomework(@PathVariable Long id) {
        return Result.success(homeworkService.getHomework(id));
    }

    @Operation(summary = "创建作业")
    @PostMapping
    public Result<HomeworkVO> createHomework(Authentication authentication,
                                              @Valid @RequestBody HomeworkRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(homeworkService.createHomework(request, operatorId));
    }

    @Operation(summary = "更新作业")
    @PutMapping("/{id}")
    public Result<HomeworkVO> updateHomework(Authentication authentication,
                                              @PathVariable Long id,
                                              @Valid @RequestBody HomeworkRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(homeworkService.updateHomework(id, request, operatorId));
    }

    @Operation(summary = "删除作业")
    @DeleteMapping("/{id}")
    public Result<Void> deleteHomework(Authentication authentication,
                                        @PathVariable Long id) {
        Long operatorId = (Long) authentication.getPrincipal();
        homeworkService.deleteHomework(id, operatorId);
        return Result.success();
    }

    @Operation(summary = "提交作业")
    @PostMapping("/{id}/submit")
    public Result<SubmissionVO> submitHomework(Authentication authentication,
                                                @PathVariable Long id,
                                                @Valid @RequestBody SubmissionRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(homeworkService.submitHomework(id, request, operatorId));
    }

    @Operation(summary = "查看提交记录")
    @GetMapping("/{id}/submissions")
    public Result<List<SubmissionVO>> getSubmissions(@PathVariable Long id) {
        return Result.success(homeworkService.getSubmissions(id));
    }

    @Operation(summary = "批改作业")
    @PutMapping("/{id}/grade")
    public Result<HomeworkVO> gradeHomework(Authentication authentication,
                                             @PathVariable Long id,
                                             @Valid @RequestBody GradeRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(homeworkService.gradeHomework(id, request, operatorId));
    }
}
