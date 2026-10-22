package com.tracker.hrtracker;

import com.tracker.hrtracker.dto.AuthDtos.RegisterRequest;
import com.tracker.hrtracker.dto.CallLogDtos.CallLogRequest;
import com.tracker.hrtracker.dto.ContactDtos.ContactRequest;
import com.tracker.hrtracker.dto.ContactDtos.ContactResponse;
import com.tracker.hrtracker.dto.ImportExportDtos.BackupPayload;
import com.tracker.hrtracker.dto.ImportExportDtos.ExportContactItem;
import com.tracker.hrtracker.dto.ImportExportDtos.ImportSummaryResponse;
import com.tracker.hrtracker.service.AuthService;
import com.tracker.hrtracker.service.CallLogService;
import com.tracker.hrtracker.service.ContactService;
import com.tracker.hrtracker.service.ImportExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
public class DataLayerTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private ContactService contactService;

    @Autowired
    private CallLogService callLogService;

    @Autowired
    private ImportExportService importExportService;

    private UUID userAId;
    private UUID userBId;

    @BeforeEach
    void setUp() {
        var userARes = authService.register(new RegisterRequest("chetan", "chetan@example.com", "password123", "Chetan"));
        userAId = userARes.userId();

        var userBRes = authService.register(new RegisterRequest("friend_rohit", "rohit@example.com", "password123", "Rohit"));
        userBId = userBRes.userId();
    }

    @Test
    @DisplayName("Verify User Isolation: User A cannot see or access User B's contacts")
    void testUserIsolation() {
        // User A creates a contact
        var contactA = contactService.createContact(userAId, new ContactRequest(
                "Priya Sharma", "Infosys", "9876543210", "priya@infosys.com", "New", "Talked about Java", LocalDate.now().plusDays(2), null, "Send resume"
        ));

        // User B creates a contact
        var contactB = contactService.createContact(userBId, new ContactRequest(
                "Amit Patel", "TCS", "9811111111", "amit@tcs.com", "New", "Followup next week", LocalDate.now().plusDays(5), null, "Call again"
        ));

        // User A queries all contacts -> must only see Priya Sharma
        List<ContactResponse> contactsA = contactService.getAllContacts(userAId, null);
        assertEquals(1, contactsA.size());
        assertEquals("Priya Sharma", contactsA.get(0).name());

        // User B queries all contacts -> must only see Amit Patel
        List<ContactResponse> contactsB = contactService.getAllContacts(userBId, null);
        assertEquals(1, contactsB.size());
        assertEquals("Amit Patel", contactsB.get(0).name());

        // User A attempting to query User B's contact directly by ID must fail with NoSuchElementException
        assertThrows(NoSuchElementException.class, () -> contactService.getContactById(contactB.id(), userAId));
    }

    @Test
    @DisplayName("Verify Call Logging Atomicity: Log call updates contact status and follow-up")
    void testCallLoggingAtomicity() {
        var contact = contactService.createContact(userAId, new ContactRequest(
                "Rahul Verma", "Google", "9999888877", "rahul@google.com", "New", "Initial pitch", null, null, null
        ));

        LocalDate nextFollowup = LocalDate.now().plusDays(3);
        callLogService.logCall(userAId, new CallLogRequest(
                contact.id(), "Resume Requested", "Asked for updated Java resume", null, nextFollowup
        ));

        // Verify contact was updated
        var updatedContact = contactService.getContactById(contact.id(), userAId);
        assertEquals("Resume Requested", updatedContact.status());
        assertEquals(nextFollowup, updatedContact.followupDate());
        assertNotNull(updatedContact.lastCallDate());
    }

    @Test
    @DisplayName("Verify JSON Import and Deduplication: duplicate phone numbers are skipped")
    void testImportWithDeduplication() {
        // User A imports 2 contacts
        var contactsToImport = List.of(
                new ExportContactItem("c-1", "Neha Rao", "Wipro", "9822334455", "neha@wipro.com", "New", "Notes 1", null, null, null),
                new ExportContactItem("c-2", "Kiran More", "Accenture", "9833445566", "kiran@accenture.com", "New", "Notes 2", null, null, null)
        );

        BackupPayload payload = new BackupPayload(1, "2026-10-23T00:00:00", "chetan", contactsToImport, List.of(), List.of());
        ImportSummaryResponse summary1 = importExportService.importUserData(userAId, payload);

        assertEquals(2, summary1.totalContactsInFile());
        assertEquals(2, summary1.importedContacts());
        assertEquals(0, summary1.skippedDuplicates());

        // User A re-imports the same payload with one new contact
        var newBatch = List.of(
                new ExportContactItem("c-1", "Neha Rao Duplicate", "Wipro", "9822334455", "neha@wipro.com", "New", "Notes 1", null, null, null),
                new ExportContactItem("c-3", "Suresh Raina", "Cognizant", "9844556677", "suresh@cognizant.com", "New", "Notes 3", null, null, null)
        );

        BackupPayload payload2 = new BackupPayload(1, "2026-10-23T00:00:00", "chetan", newBatch, List.of(), List.of());
        ImportSummaryResponse summary2 = importExportService.importUserData(userAId, payload2);

        assertEquals(2, summary2.totalContactsInFile());
        assertEquals(1, summary2.importedContacts());
        assertEquals(1, summary2.skippedDuplicates()); // 9822334455 was skipped!

        // Total contacts for User A should now be 3 (Neha, Kiran, Suresh)
        List<ContactResponse> allContacts = contactService.getAllContacts(userAId, null);
        assertEquals(3, allContacts.size());
    }

    @Test
    @DisplayName("Verify Soft Delete: deleted contacts are excluded from lists")
    void testSoftDelete() {
        var contact = contactService.createContact(userAId, new ContactRequest(
                "Tanvi Shah", "Capgemini", "9777666555", "tanvi@cap.com", "New", null, null, null, null
        ));

        assertEquals(1, contactService.getAllContacts(userAId, null).size());

        contactService.deleteContact(contact.id(), userAId);

        assertEquals(0, contactService.getAllContacts(userAId, null).size());
        assertThrows(NoSuchElementException.class, () -> contactService.getContactById(contact.id(), userAId));
    }
}
