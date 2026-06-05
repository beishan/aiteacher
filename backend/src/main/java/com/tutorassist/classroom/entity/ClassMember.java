package com.tutorassist.classroom.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("class_members")
public class ClassMember extends BaseEntity {

    private Long classId;
    private Long studentId;
    private LocalDateTime joinedAt;
}
