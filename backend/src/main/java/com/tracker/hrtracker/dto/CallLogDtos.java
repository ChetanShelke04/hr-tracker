package com.tracker.hrtracker.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class CallLogDtos {

    public record CallLogRequest(
            @NotNull(message = "Contact ID is required") UUID contactId,
            String outcome,
            String notes,
            LocalDateTime calledAt,
            LocalDate nextFollowupDate
    ) {}

    public record CallLogResponse(
            UUID id,
            UUID contactId,
            String contactName,
            String contactCompany,
            String outcome,
            String notes,
            LocalDateTime calledAt,
            LocalDate nextFollowupDate,
            LocalDateTime createdAt
    ) {}
}
