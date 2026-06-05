package com.tutorassist.student.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fee_records")
public class FeeRecord extends BaseEntity {

    private Long studentId;
    private Long feeId;
    private BigDecimal amount;
    private String paymentType;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String note;
    private Long createdBy;
}
