package com.tutorassist.student.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    /** 阶梯价格列表 */
    private List<PriceTier> priceTiers;
}
