package com.tutorassist.config.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceStatusVO {

    private String key;
    private String name;
    private String status;
    private String detail;
}
