package com.tutorassist.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("materials")
public class Material extends BaseEntity {

    private String title;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String subject;
    private String grade;
    private String tags;
    private Long parentId;
    private Boolean isFolder;
    private Boolean isFavorite;
    private Long ownerId;
    private String shareToken;
    private LocalDateTime shareExpiresAt;
}
