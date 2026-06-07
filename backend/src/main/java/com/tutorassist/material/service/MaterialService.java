package com.tutorassist.material.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutorassist.common.PageResult;
import com.tutorassist.common.exception.BusinessException;
import com.tutorassist.material.dto.*;
import com.tutorassist.material.entity.Material;
import com.tutorassist.material.entity.MaterialVersion;
import com.tutorassist.material.entity.StudentMaterial;
import com.tutorassist.material.mapper.MaterialMapper;
import com.tutorassist.material.mapper.MaterialVersionMapper;
import com.tutorassist.material.mapper.StudentMaterialMapper;
import com.tutorassist.student.entity.Student;
import com.tutorassist.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialMapper materialMapper;
    private final MaterialVersionMapper versionMapper;
    private final StudentMaterialMapper studentMaterialMapper;
    private final StudentMapper studentMapper;
    private final ObjectMapper objectMapper;
    private final MinioService minioService;

    public PageResult<MaterialVO> listMaterials(MaterialQuery query) {
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();

        if (query.getParentId() != null) {
            wrapper.eq(Material::getParentId, query.getParentId());
        } else {
            wrapper.isNull(Material::getParentId);
        }

        if (StringUtils.hasText(query.getSubject())) {
            wrapper.eq(Material::getSubject, query.getSubject());
        }
        if (StringUtils.hasText(query.getGrade())) {
            wrapper.eq(Material::getGrade, query.getGrade());
        }
        if (query.getIsFolder() != null) {
            wrapper.eq(Material::getIsFolder, query.getIsFolder());
        }
        if (query.getIsFavorite() != null) {
            wrapper.eq(Material::getIsFavorite, query.getIsFavorite());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(Material::getTitle, query.getKeyword())
                    .or()
                    .like(Material::getTags, query.getKeyword())
            );
        }

        wrapper.orderByDesc(Material::getIsFolder)
                .orderByDesc(Material::getUpdatedAt);

        Page<Material> page = materialMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()),
                wrapper
        );

        List<MaterialVO> records = page.getRecords().stream()
                .map(this::toMaterialVO)
                .toList();

        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public MaterialVO getMaterial(Long id) {
        Material material = materialMapper.selectById(id);
        if (material == null) {
            throw new BusinessException(404, "资料不存在");
        }
        return toMaterialVO(material);
    }

    @Transactional
    public MaterialVO createMaterial(MaterialRequest request, Long operatorId) {
        Material material = new Material();
        material.setTitle(request.getTitle());
        material.setFilePath(request.getFilePath());
        material.setFileType(request.getFileType());
        material.setFileSize(request.getFileSize());
        material.setSubject(request.getSubject());
        material.setGrade(request.getGrade());
        material.setTags(toJson(request.getTags()));
        material.setParentId(request.getParentId());
        material.setIsFolder(request.getIsFolder() != null ? request.getIsFolder() : false);
        material.setIsFavorite(false);
        material.setOwnerId(operatorId);

        materialMapper.insert(material);
        log.info("创建资料：{}，操作人：{}", material.getTitle(), operatorId);

        return toMaterialVO(material);
    }

    @Transactional
    public MaterialVO updateMaterial(Long id, MaterialRequest request, Long operatorId) {
        Material material = materialMapper.selectById(id);
        if (material == null) {
            throw new BusinessException(404, "资料不存在");
        }

        // 如果文件路径变化，保存版本
        if (StringUtils.hasText(request.getFilePath()) && !request.getFilePath().equals(material.getFilePath())) {
            saveVersion(material, operatorId);
            material.setFilePath(request.getFilePath());
            material.setFileSize(request.getFileSize());
        }

        material.setTitle(request.getTitle());
        material.setFileType(request.getFileType());
        material.setSubject(request.getSubject());
        material.setGrade(request.getGrade());
        material.setTags(toJson(request.getTags()));

        materialMapper.updateById(material);
        log.info("更新资料：{}，操作人：{}", material.getTitle(), operatorId);

        return toMaterialVO(material);
    }

    @Transactional
    public void deleteMaterial(Long id, Long operatorId) {
        Material material = materialMapper.selectById(id);
        if (material == null) {
            throw new BusinessException(404, "资料不存在");
        }

        // 如果是文件夹，删除子项
        if (material.getIsFolder()) {
            LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Material::getParentId, id);
            List<Material> children = materialMapper.selectList(wrapper);
            children.forEach(c -> deleteMaterial(c.getId(), operatorId));
        }

        // 删除 MinIO 中的文件
        if (StringUtils.hasText(material.getFilePath())) {
            minioService.deleteFile(material.getFilePath());
        }

        // 删除版本记录
        LambdaQueryWrapper<MaterialVersion> versionWrapper = new LambdaQueryWrapper<>();
        versionWrapper.eq(MaterialVersion::getMaterialId, id);
        versionMapper.delete(versionWrapper);

        // 删除学生资料关联
        LambdaQueryWrapper<StudentMaterial> smWrapper = new LambdaQueryWrapper<>();
        smWrapper.eq(StudentMaterial::getMaterialId, id);
        studentMaterialMapper.delete(smWrapper);

        materialMapper.deleteById(id);
        log.info("删除资料：{}，操作人：{}", material.getTitle(), operatorId);
    }

    @Transactional
    public void toggleFavorite(Long id, Long operatorId) {
        Material material = materialMapper.selectById(id);
        if (material == null) {
            throw new BusinessException(404, "资料不存在");
        }
        material.setIsFavorite(!material.getIsFavorite());
        materialMapper.updateById(material);
        log.info("{}收藏：{}，操作人：{}", material.getIsFavorite() ? "添加" : "取消", material.getTitle(), operatorId);
    }

    public List<MaterialVersionVO> getVersions(Long materialId) {
        LambdaQueryWrapper<MaterialVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialVersion::getMaterialId, materialId)
                .orderByDesc(MaterialVersion::getVersionNum);

        return versionMapper.selectList(wrapper).stream()
                .map(this::toVersionVO)
                .toList();
    }

    @Transactional
    public void assignToStudent(Long materialId, Long studentId, Long operatorId) {
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            throw new BusinessException(404, "资料不存在");
        }

        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }

        // 检查是否已分配
        LambdaQueryWrapper<StudentMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentMaterial::getMaterialId, materialId)
                .eq(StudentMaterial::getStudentId, studentId);
        Long count = studentMaterialMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException("该资料已分配给该学生");
        }

        StudentMaterial sm = new StudentMaterial();
        sm.setMaterialId(materialId);
        sm.setStudentId(studentId);
        sm.setAssignedAt(LocalDateTime.now());

        studentMaterialMapper.insert(sm);
        log.info("分配资料：{} -> 学生{}，操作人：{}", material.getTitle(), studentId, operatorId);
    }

    public List<MaterialVO> getStudentMaterials(Long studentId) {
        LambdaQueryWrapper<StudentMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentMaterial::getStudentId, studentId);

        List<StudentMaterial> smList = studentMaterialMapper.selectList(wrapper);

        return smList.stream()
                .map(sm -> {
                    Material material = materialMapper.selectById(sm.getMaterialId());
                    return material != null ? toMaterialVO(material) : null;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional
    public MaterialVO uploadMaterial(MultipartFile file, String title, String subject,
                                      String grade, String tags, Long parentId, Long operatorId) {
        // 上传文件到 MinIO
        String objectKey = minioService.uploadFile(file, "materials");

        // 解析文件类型
        String originalName = file.getOriginalFilename();
        String fileType = "other";
        if (originalName != null && originalName.contains(".")) {
            String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
            String[] knownTypes = {"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "jpg", "jpeg", "png", "gif"};
            if (Arrays.asList(knownTypes).contains(ext)) {
                fileType = ext;
            }
        }

        Material material = new Material();
        material.setTitle(title);
        material.setFilePath(objectKey);
        material.setFileType(fileType);
        material.setFileSize(file.getSize());
        material.setSubject(subject);
        material.setGrade(grade);
        material.setTags(toJson(tags != null ? Arrays.asList(tags.split(",")) : List.of()));
        material.setParentId(parentId);
        material.setIsFolder(false);
        material.setIsFavorite(false);
        material.setOwnerId(operatorId);

        materialMapper.insert(material);
        log.info("上传资料：{}，文件：{}，操作人：{}", title, objectKey, operatorId);

        return toMaterialVO(material);
    }

    public InputStream getFileStream(String objectKey) {
        return minioService.getFile(objectKey);
    }

    public String getPresignedUrl(String objectKey) {
        return minioService.getPresignedUrl(objectKey);
    }

    private void saveVersion(Material material, Long operatorId) {
        // 获取当前最大版本号
        LambdaQueryWrapper<MaterialVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialVersion::getMaterialId, material.getId())
                .orderByDesc(MaterialVersion::getVersionNum)
                .last("LIMIT 1");
        MaterialVersion latest = versionMapper.selectOne(wrapper);

        int nextVersion = (latest != null) ? latest.getVersionNum() + 1 : 1;

        MaterialVersion version = new MaterialVersion();
        version.setMaterialId(material.getId());
        version.setVersionNum(nextVersion);
        version.setFilePath(material.getFilePath());
        version.setFileSize(material.getFileSize());
        version.setCreatedBy(operatorId);

        versionMapper.insert(version);
    }

    private MaterialVO toMaterialVO(Material material) {
        // 统计版本数
        LambdaQueryWrapper<MaterialVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialVersion::getMaterialId, material.getId());
        Long versionCount = versionMapper.selectCount(wrapper);

        // 统计文件夹内子项数
        Integer childCount = 0;
        if (Boolean.TRUE.equals(material.getIsFolder())) {
            LambdaQueryWrapper<Material> childWrapper = new LambdaQueryWrapper<>();
            childWrapper.eq(Material::getParentId, material.getId());
            Long count = materialMapper.selectCount(childWrapper);
            childCount = count != null ? count.intValue() : 0;
        }

        return MaterialVO.builder()
                .id(material.getId())
                .title(material.getTitle())
                .filePath(material.getFilePath())
                .fileType(material.getFileType())
                .fileSize(material.getFileSize())
                .subject(material.getSubject())
                .grade(material.getGrade())
                .tags(fromJson(material.getTags()))
                .parentId(material.getParentId())
                .isFolder(material.getIsFolder())
                .isFavorite(material.getIsFavorite())
                .ownerId(material.getOwnerId())
                .shareToken(material.getShareToken())
                .shareExpiresAt(material.getShareExpiresAt())
                .versionCount(versionCount != null ? versionCount.intValue() : 0)
                .childCount(childCount)
                .createdAt(material.getCreatedAt())
                .updatedAt(material.getUpdatedAt())
                .build();
    }

    private MaterialVersionVO toVersionVO(MaterialVersion version) {
        return MaterialVersionVO.builder()
                .id(version.getId())
                .materialId(version.getMaterialId())
                .versionNum(version.getVersionNum())
                .filePath(version.getFilePath())
                .fileSize(version.getFileSize())
                .createdBy(version.getCreatedBy())
                .createdAt(version.getCreatedAt())
                .build();
    }

    private String toJson(List<String> list) {
        if (list == null) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<String> fromJson(String json) {
        if (!StringUtils.hasText(json)) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
