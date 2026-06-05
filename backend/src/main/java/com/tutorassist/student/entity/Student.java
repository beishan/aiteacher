package com.tutorassist.student.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("students")
public class Student extends BaseEntity {

    private String name;
    private String gender;
    private LocalDate birthDate;
    private String grade;
    private String school;
    private String subjects;
    private String source;
    private String address;
    private String phone;
    private String parentName;
    private String parentPhone;
    private String parentRelation;
    private String remark;
    private LocalDate enrollmentDate;
    private String status;
    private String avatarUrl;
    private String tags;
}
