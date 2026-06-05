package com.tutorassist.homework.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("homeworks")
public class Homework extends BaseEntity {

    private String title;
    private Long studentId;
    private Long classId;
    private String subject;
    private String content;
    private LocalDate dueDate;
    private String status;
    private String scoreType;
    private String score;
    private String comment;
    private String aiReport;
}
