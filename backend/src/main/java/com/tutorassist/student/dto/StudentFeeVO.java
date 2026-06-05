package com.tutorassist.student.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StudentFeeVO {

    private Long id;
    private Long studentId;
    private String feeType;
    private BigDecimal unitPrice;
    private Integer prepaidCount;
    private Integer remainingCount;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String subject;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private List<PriceTier> priceTiers;
}
