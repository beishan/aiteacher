package com.tutorassist.material.controller;

import com.tutorassist.common.PageResult;
import com.tutorassist.common.Result;
import com.tutorassist.material.dto.*;
import com.tutorassist.material.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "资料库管理", description = "资料上传、目录管理、版本管理")
@RestController
@RequestMapping("/api/v1/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @Operation(summary = "资料列表")
    @GetMapping
    public Result<PageResult<MaterialVO>> listMaterials(MaterialQuery query) {
        return Result.success(materialService.listMaterials(query));
    }

    @Operation(summary = "资料详情")
    @GetMapping("/{id}")
    public Result<MaterialVO> getMaterial(@PathVariable Long id) {
        return Result.success(materialService.getMaterial(id));
    }

    @Operation(summary = "创建资料/文件夹")
    @PostMapping
    public Result<MaterialVO> createMaterial(Authentication authentication,
                                              @Valid @RequestBody MaterialRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(materialService.createMaterial(request, operatorId));
    }

    @Operation(summary = "更新资料")
    @PutMapping("/{id}")
    public Result<MaterialVO> updateMaterial(Authentication authentication,
                                              @PathVariable Long id,
                                              @Valid @RequestBody MaterialRequest request) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(materialService.updateMaterial(id, request, operatorId));
    }

    @Operation(summary = "删除资料")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMaterial(Authentication authentication,
                                        @PathVariable Long id) {
        Long operatorId = (Long) authentication.getPrincipal();
        materialService.deleteMaterial(id, operatorId);
        return Result.success();
    }

    @Operation(summary = "收藏/取消收藏")
    @PostMapping("/{id}/favorite")
    public Result<Void> toggleFavorite(Authentication authentication,
                                        @PathVariable Long id) {
        Long operatorId = (Long) authentication.getPrincipal();
        materialService.toggleFavorite(id, operatorId);
        return Result.success();
    }

    @Operation(summary = "版本历史")
    @GetMapping("/{id}/versions")
    public Result<List<MaterialVersionVO>> getVersions(@PathVariable Long id) {
        return Result.success(materialService.getVersions(id));
    }

    @Operation(summary = "分配给学生")
    @PostMapping("/{id}/assign")
    public Result<Void> assignToStudent(Authentication authentication,
                                         @PathVariable Long id,
                                         @RequestParam Long studentId) {
        Long operatorId = (Long) authentication.getPrincipal();
        materialService.assignToStudent(id, studentId, operatorId);
        return Result.success();
    }

    @Operation(summary = "学生专属资料")
    @GetMapping("/student/{studentId}")
    public Result<List<MaterialVO>> getStudentMaterials(@PathVariable Long studentId) {
        return Result.success(materialService.getStudentMaterials(studentId));
    }
}
