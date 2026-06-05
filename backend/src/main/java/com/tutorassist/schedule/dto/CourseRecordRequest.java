package com.tutorassist.schedule.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseRecordRequest {

    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;

    @NotNull(message = "出勤状态不能为空")
    private String attendanceStatus;

    private String contentSummary;
    private String homeworkAssigned;
    private String remark;
}
