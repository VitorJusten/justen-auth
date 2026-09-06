package com.justen.auth.domain.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.justen.auth.core.enums.SecurityEventType;
import com.justen.auth.domain.model.SecurityEvent;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, UUID> {

    Page<SecurityEvent> findByUserId(UUID userId, Pageable pageable);

    List<SecurityEvent> findByUserIdAndEventTypeAndCreatedAtAfter(UUID userId, SecurityEventType eventType, OffsetDateTime after);

    long countByIpAddressAndEventTypeAndCreatedAtAfter(String ipAddress, SecurityEventType eventType, OffsetDateTime after);
}
