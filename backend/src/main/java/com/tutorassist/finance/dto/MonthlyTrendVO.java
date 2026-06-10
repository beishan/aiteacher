package com.tutorassist.finance.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MonthlyTrendVO {

    private String month;
    private BigDecimal income;
    private BigDecimal refund;
}
