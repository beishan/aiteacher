package com.tutorassist.classroom.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("virtual_classes")
public class VirtualClass extends BaseEntity {

    private String name;
    private String subject;
    private String description;
    private String status;
}
