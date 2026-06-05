package com.tutorassist.grade.dto;

import lombok.Data;

@Data
public class ExamQuery {

    private Long studentId;
    private String subject;
    private String examType;
    private Integer page = 1;
    private Integer size = 20;
}
