package com.tutorassist.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_records")
public class AIRecord extends BaseEntity {

    private Long userId;
    private String modelName;
    private String functionType;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer durationMs;
    private String requestText;
    private String responseText;
}
