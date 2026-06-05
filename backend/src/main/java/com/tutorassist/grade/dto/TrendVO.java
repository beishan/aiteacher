package com.tutorassist.grade.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TrendVO {

    private String subject;
    private List<TrendPoint> points;

    @Data
    @Builder
    public static class TrendPoint {
        private LocalDate examDate;
        private String examName;
        private BigDecimal score;
        private BigDecimal fullScore;
        private BigDecimal percentage;
    }
}
