package com.tutorassist.student.controller;

import com.tutorassist.common.PageResult;
import com.tutorassist.common.Result;
import com.tutorassist.student.dto.*;
import com.tutorassist.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "学生管理", description = "学生信息、课时费、费用记录")
@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    // ==================== 学生 CRUD ====================

    @Operation(summary = "学生列表")
    @GetMapping
    public Result<PageResult<StudentVO>> listStudents(StudentQuery query) {
        return Result.success(studentService.listStudents(query));
    }

    @Operation(summary = "学生详情")
    @GetMapping("/{id}")
    public Result<StudentVO> getStudent(@PathVariable Long id) {
        return Result.success(studentService.getStudent(id));
    }

    @Operation(summary = "新增学生")
    @PostMapping
    public Result<StudentVO> createStudent(Authentication authentication,
                                            @Valid @RequestBody StudentRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(studentService.createStudent(request, operatorId));
    }

    @Operation(summary = "更新学生")
    @PutMapping("/{id}")
    public Result<StudentVO> updateStudent(Authentication authentication,
                                            @PathVariable Long id,
                                            @Valid @RequestBody StudentRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(studentService.updateStudent(id, request, operatorId));
    }

    @Operation(summary = "删除学生")
    @DeleteMapping("/{id}")
    public Result<Void> deleteStudent(Authentication authentication,
                                       @PathVariable Long id) {
        Long operatorId = (Long) authentication.getPrincipal();
        studentService.deleteStudent(id, operatorId);
        return Result.success();
    }

    @Operation(summary = "修改学生状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStudentStatus(Authentication authentication,
                                             @PathVariable Long id,
                                             @RequestParam String status) {
        Long operatorId = (Long) authentication.getPrincipal();
        studentService.updateStudentStatus(id, status, operatorId);
        return Result.success();
    }

    // ==================== 课时费管理 ====================

    @Operation(summary = "学生课时费列表")
    @GetMapping("/{id}/fees")
    public Result<List<StudentFeeVO>> listStudentFees(@PathVariable Long id) {
        return Result.success(studentService.listStudentFees(id));
    }

    @Operation(summary = "新增课时费")
    @PostMapping("/{id}/fees")
    public Result<StudentFeeVO> createStudentFee(Authentication authentication,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody StudentFeeRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(studentService.createStudentFee(id, request, operatorId));
    }

    @Operation(summary = "更新课时费")
    @PutMapping("/{id}/fees/{feeId}")
    public Result<StudentFeeVO> updateStudentFee(Authentication authentication,
                                                  @PathVariable Long id,
                                                  @PathVariable Long feeId,
                                                  @Valid @RequestBody StudentFeeRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(studentService.updateStudentFee(id, feeId, request, operatorId));
    }

    // ==================== 费用记录管理 ====================

    @Operation(summary = "费用记录列表")
    @GetMapping("/{id}/fee-records")
    public Result<PageResult<FeeRecordVO>> listFeeRecords(@PathVariable Long id,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(studentService.listFeeRecords(id, page, size));
    }

    @Operation(summary = "新增费用记录")
    @PostMapping("/{id}/fee-records")
    public Result<FeeRecordVO> createFeeRecord(Authentication authentication,
                                                @PathVariable Long id,
                                                @Valid @RequestBody FeeRecordRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(studentService.createFeeRecord(id, request, operatorId));
    }
}
