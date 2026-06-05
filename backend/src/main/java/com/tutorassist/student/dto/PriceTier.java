package com.tutorassist.student.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceTier {

    /** 时长（小时） */
    private double hours;

    /** 对应价格 */
    private BigDecimal price;
}
