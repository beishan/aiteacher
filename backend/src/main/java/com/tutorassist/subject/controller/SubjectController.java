package com.tutorassist.subject.controller;

import com.tutorassist.common.Result;
import com.tutorassist.subject.dto.SubjectRequest;
import com.tutorassist.subject.dto.SubjectVO;
import com.tutorassist.subject.service.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "科目管理", description = "维护系统统一科目目录")
@RestController
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @Operation(summary = "查询科目列表")
    @GetMapping
    public Result<List<SubjectVO>> list() {
        return Result.success(subjectService.listSubjects());
    }

    @Operation(summary = "新增科目")
    @PostMapping
    public Result<SubjectVO> create(@Valid @RequestBody SubjectRequest request) {
        return Result.success(subjectService.createSubject(request));
    }

    @Operation(summary = "修改科目")
    @PutMapping("/{id}")
    public Result<SubjectVO> update(@PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        return Result.success(subjectService.updateSubject(id, request));
    }

    @Operation(summary = "删除科目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return Result.success();
    }
}
