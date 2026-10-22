package com.tracker.hrtracker.repository;

import com.tracker.hrtracker.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    List<Contact> findByUserIdAndDeletedAtIsNullOrderByNameAsc(UUID userId);

    Optional<Contact> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    Optional<Contact> findByUserIdAndPhoneAndDeletedAtIsNull(UUID userId, String phone);

    long countByUserIdAndDeletedAtIsNull(UUID userId);

    @Query("SELECT c FROM Contact c WHERE c.userId = :userId AND c.deletedAt IS NULL AND c.followupDate <= :targetDate ORDER BY c.followupDate ASC")
    List<Contact> findDueFollowups(@Param("userId") UUID userId, @Param("targetDate") LocalDate targetDate);

    @Query("SELECT c FROM Contact c WHERE c.userId = :userId AND c.deletedAt IS NULL AND (" +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.company) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.status) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Contact> searchContacts(@Param("userId") UUID userId, @Param("query") String query);
}
