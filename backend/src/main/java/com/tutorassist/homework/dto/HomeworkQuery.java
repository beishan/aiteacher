package com.tutorassist.homework.dto;

import lombok.Data;

@Data
public class HomeworkQuery {

    private Long studentId;
    private Long classId;
    private String subject;
    private String status;
    private Integer page = 1;
    private Integer size = 20;
}
