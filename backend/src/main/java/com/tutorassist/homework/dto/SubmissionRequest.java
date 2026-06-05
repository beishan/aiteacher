package com.tutorassist.homework.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubmissionRequest {

    private Long studentId;
    private List<String> files;
    private String content;
}
