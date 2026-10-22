package com.tracker.hrtracker.controller;

import com.tracker.hrtracker.dto.ImportExportDtos.*;
import com.tracker.hrtracker.security.UserPrincipal;
import com.tracker.hrtracker.service.ImportExportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/data")
public class ImportExportController {

    private final ImportExportService importExportService;

    public ImportExportController(ImportExportService importExportService) {
        this.importExportService = importExportService;
    }

    @GetMapping("/export")
    public ResponseEntity<BackupPayload> exportData(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(importExportService.exportUserData(currentUser.getId()));
    }

    @PostMapping("/import")
    public ResponseEntity<ImportSummaryResponse> importData(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody BackupPayload payload) {
        return ResponseEntity.ok(importExportService.importUserData(currentUser.getId(), payload));
    }
}
