package com.tutorassist.material.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class MaterialRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String filePath;
    private String fileType;
    private Long fileSize;
    private String subject;
    private String grade;
    private List<String> tags;
    private Long parentId;
    private Boolean isFolder;
    private String color;
}
