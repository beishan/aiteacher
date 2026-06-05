package com.tutorassist.ai.controller;

import com.tutorassist.ai.dto.ChatRequest;
import com.tutorassist.ai.dto.ChatResponse;
import com.tutorassist.ai.dto.GradeReport;
import com.tutorassist.ai.dto.TeachingPlan;
import com.tutorassist.ai.service.AIChatService;
import com.tutorassist.ai.service.AIHomeworkService;
import com.tutorassist.ai.service.AITeachingPlanService;
import com.tutorassist.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI 辅助功能", description = "智能问答、作业批改、教学计划")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIChatService chatService;
    private final AIHomeworkService homeworkService;
    private final AITeachingPlanService teachingPlanService;

    @Operation(summary = "AI 智能问答")
    @PostMapping("/chat")
    public Result<ChatResponse> chat(Authentication authentication,
                                      @Valid @RequestBody ChatRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(chatService.chat(userId, request));
    }

    @Operation(summary = "AI 批改作业")
    @PostMapping("/grade-homework")
    public Result<GradeReport> gradeHomework(Authentication authentication,
                                              @RequestParam String subject,
                                              @RequestParam String grade,
                                              @RequestParam String content,
                                              @RequestParam String answer) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(homeworkService.gradeHomework(userId, subject, grade, content, answer));
    }

    @Operation(summary = "AI 生成教学计划")
    @PostMapping("/teaching-plan")
    public Result<TeachingPlan> generateTeachingPlan(Authentication authentication,
                                                      @RequestParam String studentName,
                                                      @RequestParam String grade,
                                                      @RequestParam String subject,
                                                      @RequestParam String level,
                                                      @RequestParam String goal) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(teachingPlanService.generatePlan(userId, studentName, grade, subject, level, goal));
    }
}
