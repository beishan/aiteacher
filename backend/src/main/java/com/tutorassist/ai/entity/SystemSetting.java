package com.tutorassist.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("system_settings")
public class SystemSetting {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String key;
    private String value;
    private String description;
    private LocalDateTime updatedAt;
}
