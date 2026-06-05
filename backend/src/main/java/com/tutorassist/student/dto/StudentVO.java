package com.tutorassist.student.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StudentVO {

    private Long id;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private Integer age;
    private String grade;
    private String school;
    private List<String> subjects;
    private String source;
    private String address;
    private String phone;
    private String parentName;
    private String parentPhone;
    private String parentRelation;
    private String remark;
    private LocalDate enrollmentDate;
    private String status;
    private String avatarUrl;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
