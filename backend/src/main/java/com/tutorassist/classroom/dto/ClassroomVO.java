package com.tutorassist.classroom.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ClassroomVO {

    private Long id;
    private String name;
    private String subject;
    private String description;
    private String status;
    private Integer memberCount;
    private List<ClassMemberVO> members;
    private LocalDateTime createdAt;
}
