package com.tutorassist.student.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class FeeRecordVO {

    private Long id;
    private Long studentId;
    private Long feeId;
    private BigDecimal amount;
    private String paymentType;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String note;
    private Long createdBy;
    private LocalDateTime createdAt;
}
