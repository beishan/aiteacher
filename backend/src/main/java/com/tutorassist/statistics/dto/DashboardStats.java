package com.tutorassist.statistics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardStats {

    private Long totalStudents;
    private Long activeStudents;
    private Long todayCourses;
    private Long pendingHomeworks;
    private BigDecimal monthRevenue;

    private List<RecentCourse> recentCourses;
    private List<PendingTask> pendingTasks;

    @Data
    @Builder
    public static class RecentCourse {
        private Long id;
        private String studentName;
        private String subject;
        private String startTime;
        private String status;
    }

    @Data
    @Builder
    public static class PendingTask {
        private String type;
        private String title;
        private String description;
        private String time;
    }
}
