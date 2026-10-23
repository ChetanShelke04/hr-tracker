package com.tracker.hrtracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping(value = {"/", "/api/v1", "/api/v1/health"})
    public ResponseEntity<Map<String, Object>> checkHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "hr-tracker-backend",
                "message", "Spring Boot Backend API is running successfully. Please open your Vercel frontend URL in the browser.",
                "timestamp", Instant.now().toString()
        ));
    }
}
