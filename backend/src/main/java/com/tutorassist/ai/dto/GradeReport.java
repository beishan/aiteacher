package com.tutorassist.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GradeReport {

    private Integer totalScore;
    private Integer correctCount;
    private Integer wrongCount;
    private List<QuestionResult> questions;
    private String summary;
    private List<String> knowledgePoints;
    private String suggestion;

    @Data
    @Builder
    public static class QuestionResult {
        private Integer questionNumber;
        private String questionContent;
        private String studentAnswer;
        private String correctAnswer;
        private Boolean isCorrect;
        private String explanation;
    }
}
