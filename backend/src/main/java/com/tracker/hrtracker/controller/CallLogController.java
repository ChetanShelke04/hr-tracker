package com.tracker.hrtracker.controller;

import com.tracker.hrtracker.dto.CallLogDtos.*;
import com.tracker.hrtracker.security.UserPrincipal;
import com.tracker.hrtracker.service.CallLogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/calls")
public class CallLogController {

    private final CallLogService callLogService;

    public CallLogController(CallLogService callLogService) {
        this.callLogService = callLogService;
    }

    @PostMapping
    public ResponseEntity<CallLogResponse> logCall(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CallLogRequest request) {
        return ResponseEntity.ok(callLogService.logCall(currentUser.getId(), request));
    }

    @GetMapping("/contact/{contactId}")
    public ResponseEntity<List<CallLogResponse>> getCallsForContact(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID contactId) {
        return ResponseEntity.ok(callLogService.getCallsForContact(contactId, currentUser.getId()));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<CallLogResponse>> getAllRecentCalls(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(callLogService.getAllRecentCalls(currentUser.getId()));
    }
}
