package com.tutorassist.statistics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class RevenueStats {

    private BigDecimal totalRevenue;
    private BigDecimal monthRevenue;
    private BigDecimal lastMonthRevenue;
    private BigDecimal growthRate;

    private List<MonthlyRevenue> monthlyData;

    @Data
    @Builder
    public static class MonthlyRevenue {
        private String month;
        private BigDecimal amount;
    }
}
