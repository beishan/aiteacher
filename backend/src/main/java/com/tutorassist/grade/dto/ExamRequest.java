package com.tutorassist.grade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExamRequest {

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotBlank(message = "考试名称不能为空")
    private String examName;

    private String examType;

    @NotNull(message = "考试日期不能为空")
    private LocalDate examDate;

    @NotBlank(message = "科目不能为空")
    private String subject;

    @NotNull(message = "分数不能为空")
    private BigDecimal score;

    @NotNull(message = "满分不能为空")
    private BigDecimal fullScore;

    private Integer classRank;
    private Integer gradeRank;
    private String paperUrl;
    private String analysis;
}
