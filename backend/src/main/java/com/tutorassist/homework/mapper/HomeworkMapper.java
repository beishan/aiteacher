package com.tutorassist.homework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutorassist.homework.entity.Homework;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HomeworkMapper extends BaseMapper<Homework> {
}
