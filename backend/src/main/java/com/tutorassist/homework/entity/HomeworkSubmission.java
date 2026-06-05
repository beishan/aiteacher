package com.tutorassist.homework.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("homework_submissions")
public class HomeworkSubmission extends BaseEntity {

    private Long homeworkId;
    private Long studentId;
    private LocalDateTime submittedAt;
    private String files;
    private String content;
    private String status;
}
