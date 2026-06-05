package com.tutorassist.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StudentFeeRequest {

    @NotBlank(message = "计费类型不能为空")
    private String feeType;

    private BigDecimal unitPrice;

    private Integer prepaidCount;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private String subject;

    private String remark;
}
