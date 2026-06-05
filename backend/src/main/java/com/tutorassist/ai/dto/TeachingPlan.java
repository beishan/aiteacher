package com.tutorassist.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TeachingPlan {

    private String title;
    private String goal;
    private List<PlanPhase> phases;
    private List<String> materials;
    private String summary;

    @Data
    @Builder
    public static class PlanPhase {
        private String phase;
        private String duration;
        private List<String> tasks;
        private String expectedOutcome;
    }
}
