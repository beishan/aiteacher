package com.tutorassist.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("material_versions")
public class MaterialVersion extends BaseEntity {

    private Long materialId;
    private Integer versionNum;
    private String filePath;
    private Long fileSize;
    private Long createdBy;
}
