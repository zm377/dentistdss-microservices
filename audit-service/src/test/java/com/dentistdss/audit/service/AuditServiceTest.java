package com.dentistdss.audit.service;

import com.dentistdss.audit.dto.AuditEntryRequest;
import com.dentistdss.audit.dto.AuditEntryResponse;
import com.dentistdss.audit.model.AuditEntry;
import com.dentistdss.audit.repository.AuditEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditService
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditEntryRepository auditEntryRepository;

    @InjectMocks
    private AuditService auditService;

    private AuditEntryRequest auditEntryRequest;
    private AuditEntry auditEntry;

    @BeforeEach
    void setUp() {
        Map<String, Object> context = Map.of(
                "details", "Created new appointment for patient",
                "ipAddress", "192.168.1.1",
                "userAgent", "Mozilla/5.0"
        );

        auditEntryRequest = AuditEntryRequest.builder()
                .actor("user:1")
                .action("CREATE_APPOINTMENT")
                .target("appointment:123")
                .context(context)
                .build();

        auditEntry = AuditEntry.builder()
                .id("1")
                .actor("user:1")
                .action("CREATE_APPOINTMENT")
                .target("appointment:123")
                .context(context)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void record_ValidRequest_Success() {
        // Given
        when(auditEntryRepository.save(any(AuditEntry.class))).thenReturn(auditEntry);

        // When
        AuditEntryResponse response = auditService.record(auditEntryRequest);

        // Then
        assertNotNull(response);
        assertEquals(auditEntry.getId(), response.getId());
        assertEquals(auditEntry.getActor(), response.getActor());
        assertEquals(auditEntry.getAction(), response.getAction());
        assertEquals(auditEntry.getTarget(), response.getTarget());
        assertEquals(auditEntry.getContext(), response.getContext());
        assertNotNull(response.getTimestamp());

        verify(auditEntryRepository).save(any(AuditEntry.class));
    }

    @Test
    void record_NullRequest_ThrowsException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            auditService.record(null);
        });
    }

    @Test
    void listAll_Success() {
        // Given
        List<AuditEntry> auditEntries = Arrays.asList(auditEntry);
        when(auditEntryRepository.findAll()).thenReturn(auditEntries);

        // When
        List<AuditEntryResponse> response = auditService.listAll();

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());

        AuditEntryResponse firstEntry = response.get(0);
        assertEquals(auditEntry.getId(), firstEntry.getId());
        assertEquals(auditEntry.getActor(), firstEntry.getActor());
        assertEquals(auditEntry.getAction(), firstEntry.getAction());
        assertEquals(auditEntry.getTarget(), firstEntry.getTarget());
        assertEquals(auditEntry.getContext(), firstEntry.getContext());

        verify(auditEntryRepository).findAll();
    }

}
