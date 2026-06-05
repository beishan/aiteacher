package com.tutorassist.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class StudentRequest {

    @NotBlank(message = "学生姓名不能为空")
    private String name;

    private String gender;

    private LocalDate birthDate;

    @NotBlank(message = "年级不能为空")
    private String grade;

    private String school;

    private List<String> subjects;

    private String source;

    private String address;

    private String phone;

    private String parentName;

    @NotBlank(message = "家长联系方式不能为空")
    private String parentPhone;

    private String parentRelation;

    private String remark;

    @NotNull(message = "入学日期不能为空")
    private LocalDate enrollmentDate;

    private List<String> tags;
}
