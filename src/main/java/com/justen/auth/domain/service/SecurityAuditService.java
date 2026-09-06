package com.justen.auth.domain.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.justen.auth.core.enums.SecurityEventType;
import com.justen.auth.domain.model.SecurityEvent;
import com.justen.auth.domain.repository.SecurityEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityAuditService {

    private final SecurityEventRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordEvent(UUID userId, SecurityEventType eventType, String ipAddress, String userAgent, String details) {
        try {
            SecurityEvent event = SecurityEvent.builder()
                    .userId(userId)
                    .eventType(eventType)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .details(sanitizeDetails(details))
                    .createdAt(OffsetDateTime.now())
                    .build();

            repository.save(event);
            log.info("SECURITY EVENT [{}]: userId={}, ip={}", eventType, userId, ipAddress);
        } catch (Exception ex) {
            log.error("Failed to record security audit event: {}", eventType, ex);
        }
    }

    private String sanitizeDetails(String details) {
        if (details == null) {
            return null;
        }
        return details
                .replaceAll("(?i)(password|secret|token)=[^,;&\\s]+", "$1=***REDACTED***")
                .replaceAll("(?i)(\"password\"\\s*:\\s*\")[^\"]+(\")", "$1***REDACTED***$2")
                .replaceAll("(?i)(\"clientSecret\"\\s*:\\s*\")[^\"]+(\")", "$1***REDACTED***$2");
    }
}
