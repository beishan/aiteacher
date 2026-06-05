package com.tutorassist.statistics.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HomeworkStats {

    private Long totalHomeworks;
    private Long pendingHomeworks;
    private Long submittedHomeworks;
    private Long gradedHomeworks;
    private Double completionRate;

    private List<SubjectHomework> subjectData;

    @Data
    @Builder
    public static class SubjectHomework {
        private String subject;
        private Long total;
        private Long completed;
    }
}
