package com.tracker.hrtracker.controller;

import com.tracker.hrtracker.dto.ContactDtos.*;
import com.tracker.hrtracker.security.UserPrincipal;
import com.tracker.hrtracker.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<List<ContactResponse>> getAllContacts(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(contactService.getAllContacts(currentUser.getId(), q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactResponse> getContactById(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(contactService.getContactById(id, currentUser.getId()));
    }

    @PostMapping
    public ResponseEntity<ContactResponse> createContact(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ContactRequest request) {
        return ResponseEntity.ok(contactService.createContact(currentUser.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> updateContact(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody ContactRequest request) {
        return ResponseEntity.ok(contactService.updateContact(id, currentUser.getId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteContact(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id) {
        contactService.deleteContact(id, currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "Contact deleted successfully"));
    }

    @GetMapping("/followups/due")
    public ResponseEntity<List<ContactResponse>> getDueFollowups(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(contactService.getDueFollowups(currentUser.getId()));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(contactService.getDashboardStats(currentUser.getId()));
    }
}
