package com.tutorassist.material.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MaterialVO {

    private Long id;
    private String title;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String subject;
    private String grade;
    private List<String> tags;
    private Long parentId;
    private Boolean isFolder;
    private Boolean isFavorite;
    private Long ownerId;
    private String shareToken;
    private LocalDateTime shareExpiresAt;
    private Integer versionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
