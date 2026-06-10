package com.tutorassist.finance.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FinanceSummaryVO {

    private BigDecimal totalIncome;
    private BigDecimal monthIncome;
    private BigDecimal lastMonthIncome;
    private BigDecimal growthRate;
    private BigDecimal totalRefund;
    private Long recordCount;
}
