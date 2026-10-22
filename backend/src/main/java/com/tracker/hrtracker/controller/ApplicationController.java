package com.tracker.hrtracker.controller;

import com.tracker.hrtracker.dto.ApplicationDtos.*;
import com.tracker.hrtracker.security.UserPrincipal;
import com.tracker.hrtracker.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(applicationService.getAllApplications(currentUser.getId()));
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(applicationService.createApplication(currentUser.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponse> updateApplication(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(applicationService.updateApplication(id, currentUser.getId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteApplication(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id) {
        applicationService.deleteApplication(id, currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "Application deleted successfully"));
    }
}
