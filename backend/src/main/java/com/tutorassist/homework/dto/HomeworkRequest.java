package com.tutorassist.homework.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HomeworkRequest {

    @NotBlank(message = "作业标题不能为空")
    private String title;

    private Long studentId;
    private Long classId;
    private String subject;
    private String content;
    private LocalDate dueDate;
    private String scoreType;
}
