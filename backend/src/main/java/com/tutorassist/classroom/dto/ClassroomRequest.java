package com.tutorassist.classroom.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClassroomRequest {

    @NotBlank(message = "班级名称不能为空")
    private String name;

    private String subject;
    private String description;
}
