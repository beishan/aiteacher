package com.tutorassist.homework.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class HomeworkVO {

    private Long id;
    private String title;
    private Long studentId;
    private String studentName;
    private Long classId;
    private String className;
    private String subject;
    private String content;
    private LocalDate dueDate;
    private String status;
    private String scoreType;
    private String score;
    private String comment;
    private String aiReport;
    private Integer submissionCount;
    private LocalDateTime createdAt;
}
