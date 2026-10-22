package com.tracker.hrtracker.service;

import com.tracker.hrtracker.dto.CallLogDtos.*;
import com.tracker.hrtracker.model.CallLog;
import com.tracker.hrtracker.model.Contact;
import com.tracker.hrtracker.repository.CallLogRepository;
import com.tracker.hrtracker.repository.ContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class CallLogService {

    private final CallLogRepository callLogRepository;
    private final ContactRepository contactRepository;

    public CallLogService(CallLogRepository callLogRepository, ContactRepository contactRepository) {
        this.callLogRepository = callLogRepository;
        this.contactRepository = contactRepository;
    }

    /**
     * Atomically logs a call and synchronizes the parent contact's status and follow-up date.
     */
    @Transactional
    public CallLogResponse logCall(UUID userId, CallLogRequest request) {
        Contact contact = contactRepository.findByIdAndUserIdAndDeletedAtIsNull(request.contactId(), userId)
                .orElseThrow(() -> new NoSuchElementException("Contact not found for ID: " + request.contactId()));

        LocalDateTime callTime = request.calledAt() != null ? request.calledAt() : LocalDateTime.now();

        CallLog callLog = new CallLog();
        callLog.setUserId(userId);
        callLog.setContactId(contact.getId());
        callLog.setOutcome(request.outcome());
        callLog.setNotes(request.notes());
        callLog.setCalledAt(callTime);
        callLog.setNextFollowupDate(request.nextFollowupDate());

        CallLog savedLog = callLogRepository.save(callLog);

        // Atomic update of contact's metadata
        contact.setLastCallDate(callTime);
        if (request.outcome() != null && !request.outcome().trim().isEmpty()) {
            contact.setStatus(request.outcome().trim());
        }
        if (request.nextFollowupDate() != null) {
            contact.setFollowupDate(request.nextFollowupDate());
        }
        contactRepository.save(contact);

        return mapToResponse(savedLog, contact.getName(), contact.getCompany());
    }

    @Transactional(readOnly = true)
    public List<CallLogResponse> getCallsForContact(UUID contactId, UUID userId) {
        Contact contact = contactRepository.findByIdAndUserIdAndDeletedAtIsNull(contactId, userId)
                .orElseThrow(() -> new NoSuchElementException("Contact not found"));

        return callLogRepository.findByContactIdAndUserIdOrderByCalledAtDesc(contactId, userId)
                .stream()
                .map(call -> mapToResponse(call, contact.getName(), contact.getCompany()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CallLogResponse> getAllRecentCalls(UUID userId) {
        return callLogRepository.findByUserIdOrderByCalledAtDesc(userId)
                .stream()
                .map(call -> {
                    String contactName = "Unknown";
                    String contactCompany = "";
                    Optional<Contact> c = contactRepository.findById(call.getContactId());
                    if (c.isPresent()) {
                        contactName = c.get().getName();
                        contactCompany = c.get().getCompany();
                    }
                    return mapToResponse(call, contactName, contactCompany);
                })
                .toList();
    }

    private CallLogResponse mapToResponse(CallLog call, String contactName, String contactCompany) {
        return new CallLogResponse(
                call.getId(),
                call.getContactId(),
                contactName,
                contactCompany,
                call.getOutcome(),
                call.getNotes(),
                call.getCalledAt(),
                call.getNextFollowupDate(),
                call.getCreatedAt()
        );
    }
}
