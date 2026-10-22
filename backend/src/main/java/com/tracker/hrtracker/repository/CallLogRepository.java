package com.tracker.hrtracker.repository;

import com.tracker.hrtracker.model.CallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CallLogRepository extends JpaRepository<CallLog, UUID> {

    List<CallLog> findByContactIdAndUserIdOrderByCalledAtDesc(UUID contactId, UUID userId);

    List<CallLog> findByUserIdOrderByCalledAtDesc(UUID userId);

    long countByUserId(UUID userId);
}
