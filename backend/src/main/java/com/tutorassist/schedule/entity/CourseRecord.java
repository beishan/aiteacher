package com.tutorassist.schedule.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_records")
public class CourseRecord extends BaseEntity {

    private Long courseId;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private String attendanceStatus;
    private String contentSummary;
    private String homeworkAssigned;
    private String remark;
}
