package com.tutorassist.grade.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_records")
public class ExamRecord extends BaseEntity {

    private Long studentId;
    private String examName;
    private String examType;
    private LocalDate examDate;
    private String subject;
    private BigDecimal score;
    private BigDecimal fullScore;
    private Integer classRank;
    private Integer gradeRank;
    private String paperUrl;
    private String analysis;
}
