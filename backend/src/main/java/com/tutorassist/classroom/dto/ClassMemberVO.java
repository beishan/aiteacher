package com.tutorassist.classroom.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ClassMemberVO {

    private Long id;
    private Long classId;
    private Long studentId;
    private String studentName;
    private String grade;
    private LocalDateTime joinedAt;
}
