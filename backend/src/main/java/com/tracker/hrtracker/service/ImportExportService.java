package com.tracker.hrtracker.service;

import com.tracker.hrtracker.dto.ImportExportDtos.*;
import com.tracker.hrtracker.model.CallLog;
import com.tracker.hrtracker.model.Contact;
import com.tracker.hrtracker.model.User;
import com.tracker.hrtracker.repository.CallLogRepository;
import com.tracker.hrtracker.repository.ContactRepository;
import com.tracker.hrtracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ImportExportService {

    private final ContactRepository contactRepository;
    private final CallLogRepository callLogRepository;
    private final UserRepository userRepository;

    public ImportExportService(ContactRepository contactRepository,
                               CallLogRepository callLogRepository,
                               UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.callLogRepository = callLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public BackupPayload exportUserData(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<Contact> contacts = contactRepository.findByUserIdAndDeletedAtIsNullOrderByNameAsc(userId);
        List<CallLog> calls = callLogRepository.findByUserIdOrderByCalledAtDesc(userId);

        List<ExportContactItem> contactItems = contacts.stream().map(c -> new ExportContactItem(
                c.getId().toString(),
                c.getName(),
                c.getCompany(),
                c.getPhone(),
                c.getEmail(),
                c.getStatus(),
                c.getNotes(),
                c.getFollowupDate() != null ? c.getFollowupDate().toString() : null,
                c.getInterviewAt() != null ? c.getInterviewAt().toString() : null,
                c.getNextAction()
        )).toList();

        List<ExportCallItem> callItems = calls.stream().map(k -> new ExportCallItem(
                k.getId().toString(),
                k.getContactId().toString(),
                k.getOutcome(),
                k.getCalledAt() != null ? k.getCalledAt().toString() : null,
                k.getNotes(),
                k.getNextFollowupDate() != null ? k.getNextFollowupDate().toString() : null
        )).toList();

        return new BackupPayload(
                1,
                LocalDateTime.now().toString(),
                user.getUsername(),
                contactItems,
                callItems,
                Collections.emptyList()
        );
    }

    @Transactional
    public ImportSummaryResponse importUserData(UUID userId, BackupPayload payload) {
        if (payload == null || payload.contacts() == null) {
            return new ImportSummaryResponse(0, 0, 0, 0, 0, "No contacts provided in file");
        }

        int totalContacts = payload.contacts().size();
        int importedContacts = 0;
        int skippedDuplicates = 0;
        int totalCalls = payload.calls() != null ? payload.calls().size() : 0;
        int importedCalls = 0;

        // Map old string ID (like "c-101") to newly created Contact UUID
        Map<String, UUID> idMapping = new HashMap<>();

        for (ExportContactItem item : payload.contacts()) {
            if (item.name() == null || item.name().trim().isEmpty()) {
                continue;
            }

            // De-duplicate by phone number if present
            if (item.phone() != null && !item.phone().trim().isEmpty()) {
                Optional<Contact> existing = contactRepository.findByUserIdAndPhoneAndDeletedAtIsNull(userId, item.phone().trim());
                if (existing.isPresent()) {
                    skippedDuplicates++;
                    if (item.id() != null) {
                        idMapping.put(item.id(), existing.get().getId());
                    }
                    continue;
                }
            }

            Contact contact = new Contact();
            contact.setUserId(userId);
            contact.setName(item.name().trim());
            contact.setCompany(item.company() != null ? item.company().trim() : null);
            contact.setPhone(item.phone() != null ? item.phone().trim() : null);
            contact.setEmail(item.email() != null ? item.email().trim() : null);
            contact.setStatus(item.status() != null ? item.status().trim() : "New");
            contact.setNotes(item.notes());
            contact.setNextAction(item.nextAction());

            if (item.followup() != null && !item.followup().isBlank()) {
                try {
                    contact.setFollowupDate(LocalDate.parse(item.followup().substring(0, 10)));
                } catch (Exception ignored) {}
            }
            if (item.interviewAt() != null && !item.interviewAt().isBlank()) {
                try {
                    contact.setInterviewAt(LocalDateTime.parse(item.interviewAt()));
                } catch (Exception ignored) {}
            }

            Contact saved = contactRepository.save(contact);
            importedContacts++;

            if (item.id() != null) {
                idMapping.put(item.id(), saved.getId());
            }
        }

        // Import calls if present
        if (payload.calls() != null) {
            for (ExportCallItem callItem : payload.calls()) {
                UUID contactUuid = idMapping.get(callItem.contactId());
                if (contactUuid == null) {
                    try {
                        contactUuid = UUID.fromString(callItem.contactId());
                    } catch (Exception ignored) {}
                }

                if (contactUuid == null) {
                    continue; // Contact not found for this call
                }

                CallLog callLog = new CallLog();
                callLog.setUserId(userId);
                callLog.setContactId(contactUuid);
                callLog.setOutcome(callItem.outcome());
                callLog.setNotes(callItem.notes());

                if (callItem.dateTime() != null && !callItem.dateTime().isBlank()) {
                    try {
                        callLog.setCalledAt(LocalDateTime.parse(callItem.dateTime()));
                    } catch (Exception ignored) {
                        callLog.setCalledAt(LocalDateTime.now());
                    }
                } else {
                    callLog.setCalledAt(LocalDateTime.now());
                }

                if (callItem.followup() != null && !callItem.followup().isBlank()) {
                    try {
                        callLog.setNextFollowupDate(LocalDate.parse(callItem.followup().substring(0, 10)));
                    } catch (Exception ignored) {}
                }

                callLogRepository.save(callLog);
                importedCalls++;
            }
        }

        return new ImportSummaryResponse(
                totalContacts,
                importedContacts,
                skippedDuplicates,
                totalCalls,
                importedCalls,
                "Import completed successfully. " + importedContacts + " contacts imported, " + skippedDuplicates + " skipped as duplicates."
        );
    }
}
