package com.tutorassist.grade.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ExamVO {

    private Long id;
    private Long studentId;
    private String studentName;
    private String examName;
    private String examType;
    private LocalDate examDate;
    private String subject;
    private BigDecimal score;
    private BigDecimal fullScore;
    private BigDecimal percentage;
    private Integer classRank;
    private Integer gradeRank;
    private String paperUrl;
    private String analysis;
    private LocalDateTime createdAt;
}
