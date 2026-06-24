package com.tutorassist.backup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutorassist.backup.entity.BackupRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BackupRecordMapper extends BaseMapper<BackupRecord> {
}
