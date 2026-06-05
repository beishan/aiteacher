package com.tutorassist.homework.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SubmissionVO {

    private Long id;
    private Long homeworkId;
    private Long studentId;
    private String studentName;
    private LocalDateTime submittedAt;
    private List<String> files;
    private String content;
    private String status;
}
