package com.tutorassist.backup.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorassist.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("backup_records")
public class BackupRecord extends BaseEntity {

    private String fileName;
    private String filePath;
    private Long fileSize;
    private String backupType;
    private String status;
    private Long durationMs;
    private String remark;
    private String errorMessage;
}
