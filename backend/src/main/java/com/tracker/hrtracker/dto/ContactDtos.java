package com.tracker.hrtracker.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ContactDtos {

    public record ContactRequest(
            @NotBlank(message = "Contact name is required") String name,
            String company,
            String phone,
            String email,
            String status,
            String notes,
            LocalDate followupDate,
            LocalDateTime interviewAt,
            String nextAction
    ) {}

    public record ContactResponse(
            UUID id,
            String name,
            String company,
            String phone,
            String email,
            String status,
            String notes,
            LocalDate followupDate,
            LocalDateTime interviewAt,
            String nextAction,
            LocalDateTime lastCallDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
