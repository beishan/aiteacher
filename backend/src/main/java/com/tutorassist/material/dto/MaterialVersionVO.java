package com.tutorassist.material.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MaterialVersionVO {

    private Long id;
    private Long materialId;
    private Integer versionNum;
    private String filePath;
    private Long fileSize;
    private Long createdBy;
    private LocalDateTime createdAt;
}
