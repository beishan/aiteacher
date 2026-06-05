package com.tutorassist.statistics.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class StudentStats {

    private Long totalStudents;
    private Long activeStudents;
    private Long pausedStudents;
    private Long graduatedStudents;
    private Long lostStudents;

    private List<GradeDistribution> gradeDistribution;
    private List<SubjectDistribution> subjectDistribution;
    private List<SourceDistribution> sourceDistribution;

    @Data
    @Builder
    public static class GradeDistribution {
        private String grade;
        private Long count;
    }

    @Data
    @Builder
    public static class SubjectDistribution {
        private String subject;
        private Long count;
    }

    @Data
    @Builder
    public static class SourceDistribution {
        private String source;
        private Long count;
    }
}
