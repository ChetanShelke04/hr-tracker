package com.tracker.hrtracker.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ApplicationDtos {

    public record ApplicationRequest(
            @NotBlank(message = "Company is required") String company,
            @NotBlank(message = "Role is required") String role,
            String stage,
            String jobUrl,
            String location,
            String salaryExpectation,
            LocalDate appliedDate,
            String notes
    ) {}

    public record ApplicationResponse(
            UUID id,
            String company,
            String role,
            String stage,
            String jobUrl,
            String location,
            String salaryExpectation,
            LocalDate appliedDate,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
