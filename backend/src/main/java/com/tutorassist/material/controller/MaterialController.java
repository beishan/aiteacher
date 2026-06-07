package com.tutorassist.material.controller;

import com.tutorassist.common.PageResult;
import com.tutorassist.common.Result;
import com.tutorassist.material.dto.*;
import com.tutorassist.material.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @Operation(summary = "上传文件并创建资料")
    @PostMapping("/upload")
    public Result<MaterialVO> uploadMaterial(Authentication authentication,
                                              @RequestParam("file") MultipartFile file,
                                              @RequestParam("title") String title,
                                              @RequestParam(value = "subject", required = false) String subject,
                                              @RequestParam(value = "grade", required = false) String grade,
                                              @RequestParam(value = "tags", required = false) String tags,
                                              @RequestParam(value = "parentId", required = false) Long parentId) {
        Long operatorId = (Long) authentication.getPrincipal();
        return Result.success(materialService.uploadMaterial(file, title, subject, grade, tags, parentId, operatorId));
    }

    @Operation(summary = "创建资料/文件夹（纯元数据）")
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

    @Operation(summary = "下载文件")
    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable Long id) {
        MaterialVO material = materialService.getMaterial(id);
        if (material.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(
                materialService.getFileStream(material.getFilePath()));

        String encodedName = URLEncoder.encode(material.getTitle(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        String ext = material.getFileType() != null ? "." + material.getFileType() : "";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName + ext)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @Operation(summary = "获取文件预签名 URL（用于预览）")
    @GetMapping("/{id}/preview-url")
    public Result<String> getPreviewUrl(@PathVariable Long id) {
        MaterialVO material = materialService.getMaterial(id);
        if (material.getFilePath() == null) {
            return Result.success(null);
        }
        return Result.success(materialService.getPresignedUrl(material.getFilePath()));
    }
}
