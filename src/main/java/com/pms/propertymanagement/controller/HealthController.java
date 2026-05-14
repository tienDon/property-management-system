package com.pms.propertymanagement.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final Environment environment;

    @Value("${spring.application.name:application}")
    private String applicationName;

    public HealthController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "UP");
        payload.put("application", applicationName);
        String[] activeProfiles = environment.getActiveProfiles();
        payload.put("profiles", activeProfiles.length > 0 ? activeProfiles : environment.getDefaultProfiles());
        payload.put("timestamp", OffsetDateTime.now().toString());
        return payload;
    }
}
