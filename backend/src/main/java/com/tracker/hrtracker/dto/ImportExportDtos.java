package com.tracker.hrtracker.dto;

import java.util.List;

public class ImportExportDtos {

    public record ExportContactItem(
            String id,
            String name,
            String company,
            String phone,
            String email,
            String status,
            String notes,
            String followup,
            String interviewAt,
            String nextAction
    ) {}

    public record ExportCallItem(
            String id,
            String contactId,
            String outcome,
            String dateTime,
            String notes,
            String followup
    ) {}

    public record ExportActivityItem(
            String id,
            String type,
            String description,
            String scheduledAt,
            Boolean completed
    ) {}

    public record BackupPayload(
            int schemaVersion,
            String exportedAt,
            String user,
            List<ExportContactItem> contacts,
            List<ExportCallItem> calls,
            List<ExportActivityItem> activities
    ) {}

    public record ImportSummaryResponse(
            int totalContactsInFile,
            int importedContacts,
            int skippedDuplicates,
            int totalCallsInFile,
            int importedCalls,
            String message
    ) {}
}
