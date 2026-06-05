package com.tutorassist.student.dto;

import lombok.Data;

@Data
public class StudentQuery {

    private String name;
    private String grade;
    private String status;
    private String subject;
    private Integer page = 1;
    private Integer size = 20;
}
