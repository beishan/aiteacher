package com.tutorassist.schedule.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CourseRecordVO {

    private Long id;
    private Long courseId;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private String attendanceStatus;
    private String contentSummary;
    private String homeworkAssigned;
    private String remark;
    private LocalDateTime createdAt;
}
