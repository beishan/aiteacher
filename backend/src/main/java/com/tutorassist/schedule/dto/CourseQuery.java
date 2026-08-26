package com.tutorassist.schedule.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseQuery {

    private Long studentId;
    private Long classId;
    private String subject;
    private String courseType;
    private String status;
    private LocalDateTime startTimeFrom;
    private LocalDateTime startTimeTo;
    private String sortBy = "startTime";
    private String sortOrder = "asc";
    private Integer page = 1;
    private Integer size = 50;
}
