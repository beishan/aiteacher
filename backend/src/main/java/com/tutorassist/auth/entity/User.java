package com.tutorassist.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {

    private String username;
    private String passwordHash;
    private String displayName;
    private String role;
    private String avatarUrl;
}
