package com.tutorassist.material.dto;

import lombok.Data;

@Data
public class MaterialQuery {

    private Long parentId;
    private String subject;
    private String grade;
    private String keyword;
    private Boolean isFolder;
    private Boolean isFavorite;
    private Integer page = 1;
    private Integer size = 50;
}
