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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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
        auditEntryRequest = AuditEntryRequest.builder()
                .userId(1L)
                .action("CREATE_APPOINTMENT")
                .resource("appointments")
                .resourceId("123")
                .details("Created new appointment for patient")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .build();

        auditEntry = AuditEntry.builder()
                .id(1L)
                .userId(1L)
                .action("CREATE_APPOINTMENT")
                .resource("appointments")
                .resourceId("123")
                .details("Created new appointment for patient")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
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
        assertEquals(auditEntry.getUserId(), response.getUserId());
        assertEquals(auditEntry.getAction(), response.getAction());
        assertEquals(auditEntry.getResource(), response.getResource());
        assertEquals(auditEntry.getResourceId(), response.getResourceId());
        assertEquals(auditEntry.getDetails(), response.getDetails());
        assertEquals(auditEntry.getIpAddress(), response.getIpAddress());
        assertEquals(auditEntry.getUserAgent(), response.getUserAgent());
        assertNotNull(response.getTimestamp());

        verify(auditEntryRepository).save(any(AuditEntry.class));
    }

    @Test
    void record_NullRequest_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.record(null);
        });
    }

    @Test
    void getAuditEntries_ValidParameters_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditEntry> auditEntries = Arrays.asList(auditEntry);
        Page<AuditEntry> auditPage = new PageImpl<>(auditEntries, pageable, 1);
        
        when(auditEntryRepository.findAll(pageable)).thenReturn(auditPage);

        // When
        Page<AuditEntryResponse> response = auditService.getAuditEntries(pageable);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        
        AuditEntryResponse firstEntry = response.getContent().get(0);
        assertEquals(auditEntry.getId(), firstEntry.getId());
        assertEquals(auditEntry.getUserId(), firstEntry.getUserId());
        assertEquals(auditEntry.getAction(), firstEntry.getAction());

        verify(auditEntryRepository).findAll(pageable);
    }

    @Test
    void getAuditEntriesByUser_ValidUserId_Success() {
        // Given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditEntry> auditEntries = Arrays.asList(auditEntry);
        Page<AuditEntry> auditPage = new PageImpl<>(auditEntries, pageable, 1);
        
        when(auditEntryRepository.findByUserId(userId, pageable)).thenReturn(auditPage);

        // When
        Page<AuditEntryResponse> response = auditService.getAuditEntriesByUser(userId, pageable);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        
        AuditEntryResponse firstEntry = response.getContent().get(0);
        assertEquals(userId, firstEntry.getUserId());

        verify(auditEntryRepository).findByUserId(userId, pageable);
    }

    @Test
    void getAuditEntriesByAction_ValidAction_Success() {
        // Given
        String action = "CREATE_APPOINTMENT";
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditEntry> auditEntries = Arrays.asList(auditEntry);
        Page<AuditEntry> auditPage = new PageImpl<>(auditEntries, pageable, 1);
        
        when(auditEntryRepository.findByAction(action, pageable)).thenReturn(auditPage);

        // When
        Page<AuditEntryResponse> response = auditService.getAuditEntriesByAction(action, pageable);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        
        AuditEntryResponse firstEntry = response.getContent().get(0);
        assertEquals(action, firstEntry.getAction());

        verify(auditEntryRepository).findByAction(action, pageable);
    }

    @Test
    void getAuditEntriesByResource_ValidResource_Success() {
        // Given
        String resource = "appointments";
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditEntry> auditEntries = Arrays.asList(auditEntry);
        Page<AuditEntry> auditPage = new PageImpl<>(auditEntries, pageable, 1);
        
        when(auditEntryRepository.findByResource(resource, pageable)).thenReturn(auditPage);

        // When
        Page<AuditEntryResponse> response = auditService.getAuditEntriesByResource(resource, pageable);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        
        AuditEntryResponse firstEntry = response.getContent().get(0);
        assertEquals(resource, firstEntry.getResource());

        verify(auditEntryRepository).findByResource(resource, pageable);
    }

    @Test
    void getAuditEntriesByDateRange_ValidDateRange_Success() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditEntry> auditEntries = Arrays.asList(auditEntry);
        Page<AuditEntry> auditPage = new PageImpl<>(auditEntries, pageable, 1);
        
        when(auditEntryRepository.findByTimestampBetween(startDate, endDate, pageable)).thenReturn(auditPage);

        // When
        Page<AuditEntryResponse> response = auditService.getAuditEntriesByDateRange(startDate, endDate, pageable);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());

        verify(auditEntryRepository).findByTimestampBetween(startDate, endDate, pageable);
    }

    @Test
    void convertToResponse_ValidAuditEntry_Success() {
        // When
        AuditEntryResponse response = auditService.convertToResponse(auditEntry);

        // Then
        assertNotNull(response);
        assertEquals(auditEntry.getId(), response.getId());
        assertEquals(auditEntry.getUserId(), response.getUserId());
        assertEquals(auditEntry.getAction(), response.getAction());
        assertEquals(auditEntry.getResource(), response.getResource());
        assertEquals(auditEntry.getResourceId(), response.getResourceId());
        assertEquals(auditEntry.getDetails(), response.getDetails());
        assertEquals(auditEntry.getIpAddress(), response.getIpAddress());
        assertEquals(auditEntry.getUserAgent(), response.getUserAgent());
        assertEquals(auditEntry.getTimestamp(), response.getTimestamp());
    }
}
