package com.tutorassist.config.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SystemInfoVO {

    private String applicationName;
    private String version;
    private String overallStatus;
    private String environment;
    private LocalDateTime startedAt;
    private LocalDateTime serverTime;
    private Long uptimeSeconds;
    private String javaVersion;
    private String operatingSystem;
    private Integer processors;
    private Long memoryUsedMb;
    private Long memoryMaxMb;
    private List<ServiceStatusVO> services;
}
