package com.tutorassist.subject.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubjectVO {

    private Long id;
    private String name;
    private Integer sortOrder;
    private long courseCount;
    private long usageCount;
    private boolean canDelete;
}
