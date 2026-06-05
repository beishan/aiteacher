package com.tutorassist.classroom.controller;

import com.tutorassist.classroom.dto.ClassMemberVO;
import com.tutorassist.classroom.dto.ClassroomRequest;
import com.tutorassist.classroom.dto.ClassroomVO;
import com.tutorassist.classroom.service.ClassroomService;
import com.tutorassist.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "虚拟班级管理", description = "班级创建、成员管理")
@RestController
@RequestMapping("/api/v1/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @Operation(summary = "班级列表")
    @GetMapping
    public Result<List<ClassroomVO>> listClassrooms() {
        return Result.success(classroomService.listClassrooms());
    }

    @Operation(summary = "班级详情")
    @GetMapping("/{id}")
    public Result<ClassroomVO> getClassroom(@PathVariable Long id) {
        return Result.success(classroomService.getClassroom(id));
    }

    @Operation(summary = "创建班级")
    @PostMapping
    public Result<ClassroomVO> createClassroom(Authentication authentication,
                                                @Valid @RequestBody ClassroomRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(classroomService.createClassroom(request, operatorId));
    }

    @Operation(summary = "更新班级")
    @PutMapping("/{id}")
    public Result<ClassroomVO> updateClassroom(Authentication authentication,
                                                @PathVariable Long id,
                                                @Valid @RequestBody ClassroomRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(classroomService.updateClassroom(id, request, operatorId));
    }

    @Operation(summary = "删除班级")
    @DeleteMapping("/{id}")
    public Result<Void> deleteClassroom(Authentication authentication,
                                         @PathVariable Long id) {
        Long operatorId = (Long) authentication.getPrincipal();
        classroomService.deleteClassroom(id, operatorId);
        return Result.success();
    }

    @Operation(summary = "修改班级状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateClassroomStatus(Authentication authentication,
                                               @PathVariable Long id,
                                               @RequestParam String status) {
        Long operatorId = (Long) authentication.getPrincipal();
        classroomService.updateClassroomStatus(id, status, operatorId);
        return Result.success();
    }

    @Operation(summary = "班级成员列表")
    @GetMapping("/{id}/members")
    public Result<List<ClassMemberVO>> listMembers(@PathVariable Long id) {
        return Result.success(classroomService.listMembers(id));
    }

    @Operation(summary = "添加班级成员")
    @PostMapping("/{id}/members")
    public Result<Void> addMember(Authentication authentication,
                                   @PathVariable Long id,
                                   @RequestParam Long studentId) {
        Long operatorId = (Long) authentication.getPrincipal();
        classroomService.addMember(id, studentId, operatorId);
        return Result.success();
    }

    @Operation(summary = "移除班级成员")
    @DeleteMapping("/{id}/members/{studentId}")
    public Result<Void> removeMember(Authentication authentication,
                                      @PathVariable Long id,
                                      @PathVariable Long studentId) {
        Long operatorId = (Long) authentication.getPrincipal();
        classroomService.removeMember(id, studentId, operatorId);
        return Result.success();
    }
}
