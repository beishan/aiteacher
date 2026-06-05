package com.tutorassist.statistics.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CourseStats {

    private Long totalCourses;
    private Long completedCourses;
    private Long cancelledCourses;
    private Long thisMonthCourses;

    private List<WeeklyCourse> weeklyData;

    @Data
    @Builder
    public static class WeeklyCourse {
        private String week;
        private Long count;
    }
}
