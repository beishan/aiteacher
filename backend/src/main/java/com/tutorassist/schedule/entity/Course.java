package com.tutorassist.schedule.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("courses")
public class Course extends BaseEntity {

    private Long studentId;
    private Long classId;
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
}
