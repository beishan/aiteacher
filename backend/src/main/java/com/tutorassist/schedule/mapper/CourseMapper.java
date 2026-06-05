package com.tutorassist.schedule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutorassist.schedule.entity.Course;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
