package com.tracker.hrtracker.service;

import com.tracker.hrtracker.dto.ContactDtos.*;
import com.tracker.hrtracker.model.Contact;
import com.tracker.hrtracker.repository.CallLogRepository;
import com.tracker.hrtracker.repository.ContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final CallLogRepository callLogRepository;

    public ContactService(ContactRepository contactRepository, CallLogRepository callLogRepository) {
        this.contactRepository = contactRepository;
        this.callLogRepository = callLogRepository;
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> getAllContacts(UUID userId, String query) {
        List<Contact> contacts;
        if (query != null && !query.trim().isEmpty()) {
            contacts = contactRepository.searchContacts(userId, query.trim());
        } else {
            contacts = contactRepository.findByUserIdAndDeletedAtIsNullOrderByNameAsc(userId);
        }
        return contacts.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public ContactResponse getContactById(UUID id, UUID userId) {
        Contact contact = contactRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Contact not found"));
        return mapToResponse(contact);
    }

    @Transactional
    public ContactResponse createContact(UUID userId, ContactRequest request) {
        Contact contact = new Contact();
        contact.setUserId(userId);
        updateContactFromRequest(contact, request);

        Contact saved = contactRepository.save(contact);
        return mapToResponse(saved);
    }

    @Transactional
    public ContactResponse updateContact(UUID id, UUID userId, ContactRequest request) {
        Contact contact = contactRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Contact not found"));

        updateContactFromRequest(contact, request);
        Contact saved = contactRepository.save(contact);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteContact(UUID id, UUID userId) {
        Contact contact = contactRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Contact not found"));

        // Soft delete for safety
        contact.setDeletedAt(LocalDateTime.now());
        contactRepository.save(contact);
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> getDueFollowups(UUID userId) {
        List<Contact> due = contactRepository.findDueFollowups(userId, LocalDate.now());
        return due.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats(UUID userId) {
        long totalContacts = contactRepository.countByUserIdAndDeletedAtIsNull(userId);
        long totalCalls = callLogRepository.countByUserId(userId);
        List<Contact> dueFollowups = contactRepository.findDueFollowups(userId, LocalDate.now());

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalContacts", totalContacts);
        stats.put("totalCalls", totalCalls);
        stats.put("dueFollowupsCount", dueFollowups.size());
        stats.put("dueFollowups", dueFollowups.stream().map(this::mapToResponse).toList());
        return stats;
    }

    private void updateContactFromRequest(Contact contact, ContactRequest request) {
        contact.setName(request.name().trim());
        contact.setCompany(request.company() != null ? request.company().trim() : null);
        contact.setPhone(request.phone() != null ? request.phone().trim() : null);
        contact.setEmail(request.email() != null ? request.email().trim() : null);
        contact.setStatus(request.status() != null ? request.status().trim() : "New");
        contact.setNotes(request.notes());
        contact.setFollowupDate(request.followupDate());
        contact.setInterviewAt(request.interviewAt());
        contact.setNextAction(request.nextAction());
    }

    public ContactResponse mapToResponse(Contact c) {
        return new ContactResponse(
                c.getId(),
                c.getName(),
                c.getCompany(),
                c.getPhone(),
                c.getEmail(),
                c.getStatus(),
                c.getNotes(),
                c.getFollowupDate(),
                c.getInterviewAt(),
                c.getNextAction(),
                c.getLastCallDate(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
