package com.tracker.hrtracker.service;

import com.tracker.hrtracker.dto.ApplicationDtos.*;
import com.tracker.hrtracker.model.Application;
import com.tracker.hrtracker.repository.ApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplications(UUID userId) {
        return applicationRepository.findByUserIdAndDeletedAtIsNullOrderByAppliedDateDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ApplicationResponse createApplication(UUID userId, ApplicationRequest request) {
        Application application = new Application();
        application.setUserId(userId);
        updateFromRequest(application, request);

        Application saved = applicationRepository.save(application);
        return mapToResponse(saved);
    }

    @Transactional
    public ApplicationResponse updateApplication(UUID id, UUID userId, ApplicationRequest request) {
        Application application = applicationRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Application not found"));

        updateFromRequest(application, request);
        Application saved = applicationRepository.save(application);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteApplication(UUID id, UUID userId) {
        Application application = applicationRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Application not found"));

        application.setDeletedAt(LocalDateTime.now());
        applicationRepository.save(application);
    }

    private void updateFromRequest(Application app, ApplicationRequest req) {
        app.setCompany(req.company().trim());
        app.setRole(req.role().trim());
        app.setStage(req.stage() != null ? req.stage().trim() : "Applied");
        app.setJobUrl(req.jobUrl());
        app.setLocation(req.location());
        app.setSalaryExpectation(req.salaryExpectation());
        app.setAppliedDate(req.appliedDate());
        app.setNotes(req.notes());
    }

    private ApplicationResponse mapToResponse(Application app) {
        return new ApplicationResponse(
                app.getId(),
                app.getCompany(),
                app.getRole(),
                app.getStage(),
                app.getJobUrl(),
                app.getLocation(),
                app.getSalaryExpectation(),
                app.getAppliedDate(),
                app.getNotes(),
                app.getCreatedAt(),
                app.getUpdatedAt()
        );
    }
}
