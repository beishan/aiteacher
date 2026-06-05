package com.tutorassist.schedule.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CourseVO {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long classId;
    private String className;
    private String subject;
    private String courseType;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String meetingLink;
    private String recurrenceRule;
    private LocalDate recurrenceEndDate;
    private Long parentCourseId;
    private String status;
    private String remark;
    private String color;
    private LocalDateTime createdAt;
}
