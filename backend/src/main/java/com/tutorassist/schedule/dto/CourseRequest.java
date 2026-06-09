package com.tutorassist.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CourseRequest {

    private Long studentId;
    private Long classId;

    @NotBlank(message = "科目不能为空")
    private String subject;

    @NotBlank(message = "课程类型不能为空")
    private String courseType;

    private String title;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    private String location;
    private String meetingLink;

    // 周期排课配置
    private String repeatType;           // NONE, WEEKLY, BIWEEKLY, CUSTOM
    private Integer repeatInterval;      // 自定义间隔天数
    private LocalDate repeatEndDate;     // 周期截止日期

    private String remark;
    private String color;
    private String status;
}
