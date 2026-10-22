package com.tracker.hrtracker.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "call_logs", indexes = {
    @Index(name = "idx_call_logs_contact_id", columnList = "contact_id"),
    @Index(name = "idx_call_logs_user_id", columnList = "user_id")
})
public class CallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "contact_id", nullable = false)
    private UUID contactId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private String outcome; // e.g. "Resume Requested", "Follow Up Needed", "No Answer", "Interview Fixed", "Rejected"

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private LocalDateTime calledAt;

    private LocalDate nextFollowupDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CallLog() {}

    @PrePersist
    protected void onCreate() {
        if (this.calledAt == null) {
            this.calledAt = LocalDateTime.now();
        }
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getContactId() { return contactId; }
    public void setContactId(UUID contactId) { this.contactId = contactId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCalledAt() { return calledAt; }
    public void setCalledAt(LocalDateTime calledAt) { this.calledAt = calledAt; }

    public LocalDate getNextFollowupDate() { return nextFollowupDate; }
    public void setNextFollowupDate(LocalDate nextFollowupDate) { this.nextFollowupDate = nextFollowupDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
