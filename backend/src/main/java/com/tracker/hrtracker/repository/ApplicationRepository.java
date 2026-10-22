package com.tracker.hrtracker.repository;

import com.tracker.hrtracker.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    List<Application> findByUserIdAndDeletedAtIsNullOrderByAppliedDateDesc(UUID userId);

    Optional<Application> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    long countByUserIdAndDeletedAtIsNull(UUID userId);
}
