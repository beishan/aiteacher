package com.tutorassist.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student_materials")
public class StudentMaterial extends BaseEntity {

    private Long studentId;
    private Long materialId;
    private LocalDateTime assignedAt;
}
