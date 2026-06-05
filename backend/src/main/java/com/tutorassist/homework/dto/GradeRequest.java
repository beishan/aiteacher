package com.tutorassist.homework.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GradeRequest {

    @NotBlank(message = "分数不能为空")
    private String score;

    private String comment;
}
