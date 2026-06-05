package com.tutorassist.student.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student_fees")
public class StudentFee extends BaseEntity {

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
    /** 阶梯价格JSON，格式：[{"hours":1,"price":200},{"hours":2,"price":380}] */
    private String priceTiers;
}
