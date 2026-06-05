package com.tutorassist.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/actuator/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now().toString(),
                "service", "tutor-assist"
        );
    }

    @GetMapping("/actuator/info")
    public Map<String, Object> info() {
        return Map.of(
                "name", "TutorAssist",
                "version", "1.0.0",
                "description", "家教助手系统"
        );
    }
}
