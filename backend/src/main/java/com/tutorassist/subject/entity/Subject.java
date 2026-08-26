package com.tutorassist.subject.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("subjects")
public class Subject extends BaseEntity {

    private String name;
    private Integer sortOrder;
}
