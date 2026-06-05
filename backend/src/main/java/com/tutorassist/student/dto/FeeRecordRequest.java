package com.tutorassist.student.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FeeRecordRequest {

    private Long feeId;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    @NotNull(message = "收支类型不能为空")
    private String paymentType;

    @NotNull(message = "缴费日期不能为空")
    private LocalDate paymentDate;

    private String paymentMethod;

    private String note;
}
