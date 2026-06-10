package com.tutorassist.finance.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FinanceRecordQuery {

    private Long studentId;
    private String paymentType;
    private String paymentMethod;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer page = 1;
    private Integer size = 20;
}
