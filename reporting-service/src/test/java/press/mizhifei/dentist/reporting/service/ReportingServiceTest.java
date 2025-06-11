package press.mizhifei.dentist.reporting.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import press.mizhifei.dentist.reporting.dto.ReportRequest;
import press.mizhifei.dentist.reporting.dto.ReportResult;
import press.mizhifei.dentist.reporting.generator.ReportGenerator;
import press.mizhifei.dentist.reporting.model.ReportExecution;
import press.mizhifei.dentist.reporting.model.ReportTemplate;
import press.mizhifei.dentist.reporting.repository.ReportExecutionRepository;
import press.mizhifei.dentist.reporting.repository.ReportTemplateRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportingService
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    private ReportGenerator reportGenerator;
    
    @Mock
    private ReportTemplateRepository templateRepository;
    
    @Mock
    private ReportExecutionRepository executionRepository;
    
    @Mock
    private EmailDeliveryService emailDeliveryService;
    
    @Mock
    private SecurityService securityService;

    private ReportingService reportingService;

    @BeforeEach
    void setUp() {
        reportingService = new ReportingService(
            reportGenerator, 
            templateRepository, 
            executionRepository, 
            emailDeliveryService, 
            securityService
        );
    }

    @Test
    void testGenerateReportAsync_Success() {
        // Given
        ReportRequest request = ReportRequest.builder()
                .templateCode("PATIENT_NO_SHOWS")
                .requestedBy(1L)
                .clinicId(1L)
                .parameters(Map.of("startDate", "2024-01-01", "endDate", "2024-01-31"))
                .requestedFormats(List.of(ReportTemplate.ReportFormat.PDF))
                .build();

        ReportTemplate template = ReportTemplate.builder()
                .templateCode("PATIENT_NO_SHOWS")
                .name("Patient No-Show Report")
                .active(true)
                .allowedRoles(List.of("DENTIST", "CLINIC_ADMIN"))
                .build();

        ReportExecution execution = ReportExecution.builder()
                .id("exec-123")
                .templateCode("PATIENT_NO_SHOWS")
                .status(ReportExecution.ExecutionStatus.PENDING)
                .build();

        ReportResult result = ReportResult.builder()
                .templateCode("PATIENT_NO_SHOWS")
                .executionId("exec-123")
                .status(ReportExecution.ExecutionStatus.COMPLETED)
                .build();

        // When
        when(securityService.canAccessTemplate(anyString(), any())).thenReturn(true);
        when(executionRepository.save(any())).thenReturn(execution);
        when(templateRepository.findByTemplateCodeAndActive(anyString(), any())).thenReturn(Optional.of(template));
        when(reportGenerator.generateReportAsync(any())).thenReturn(CompletableFuture.completedFuture(result));

        // Then
        CompletableFuture<ReportResult> future = reportingService.generateReportAsync(request);
        
        assertNotNull(future);
        assertTrue(future.isDone());
        assertEquals("PATIENT_NO_SHOWS", future.join().getTemplateCode());
        assertEquals(ReportExecution.ExecutionStatus.COMPLETED, future.join().getStatus());
    }

    @Test
    void testGenerateReportSync_Success() {
        // Given
        ReportRequest request = ReportRequest.builder()
                .templateCode("REVENUE_ANALYSIS")
                .requestedBy(1L)
                .clinicId(1L)
                .parameters(Map.of("month", "2024-01"))
                .requestedFormats(List.of(ReportTemplate.ReportFormat.EXCEL))
                .build();

        ReportTemplate template = ReportTemplate.builder()
                .templateCode("REVENUE_ANALYSIS")
                .name("Revenue Analysis Report")
                .active(true)
                .allowedRoles(List.of("CLINIC_ADMIN"))
                .build();

        ReportExecution execution = ReportExecution.builder()
                .id("exec-456")
                .templateCode("REVENUE_ANALYSIS")
                .status(ReportExecution.ExecutionStatus.PENDING)
                .build();

        ReportResult result = ReportResult.builder()
                .templateCode("REVENUE_ANALYSIS")
                .executionId("exec-456")
                .status(ReportExecution.ExecutionStatus.COMPLETED)
                .build();

        // When
        when(securityService.canAccessTemplate(anyString(), any())).thenReturn(true);
        when(executionRepository.save(any())).thenReturn(execution);
        when(templateRepository.findByTemplateCodeAndActive(anyString(), any())).thenReturn(Optional.of(template));
        when(reportGenerator.generateReport(any())).thenReturn(result);

        // Then
        ReportResult actualResult = reportingService.generateReport(request);
        
        assertNotNull(actualResult);
        assertEquals("REVENUE_ANALYSIS", actualResult.getTemplateCode());
        assertEquals(ReportExecution.ExecutionStatus.COMPLETED, actualResult.getStatus());
    }

    @Test
    void testGenerateReport_TemplateNotFound() {
        // Given
        ReportRequest request = ReportRequest.builder()
                .templateCode("NON_EXISTENT")
                .requestedBy(1L)
                .parameters(Map.of())
                .requestedFormats(List.of(ReportTemplate.ReportFormat.PDF))
                .build();

        ReportExecution execution = ReportExecution.builder()
                .id("exec-789")
                .templateCode("NON_EXISTENT")
                .status(ReportExecution.ExecutionStatus.PENDING)
                .build();

        // When
        when(securityService.canAccessTemplate(anyString(), any())).thenReturn(true);
        when(executionRepository.save(any())).thenReturn(execution);
        when(templateRepository.findByTemplateCodeAndActive(anyString(), any())).thenReturn(Optional.empty());

        // Then
        assertThrows(ReportingService.ReportTemplateNotFoundException.class, () -> {
            reportingService.generateReport(request);
        });
    }

    @Test
    void testGenerateReport_AccessDenied() {
        // Given
        ReportRequest request = ReportRequest.builder()
                .templateCode("ADMIN_ONLY_REPORT")
                .requestedBy(1L)
                .parameters(Map.of())
                .requestedFormats(List.of(ReportTemplate.ReportFormat.PDF))
                .build();

        // When
        when(securityService.canAccessTemplate(anyString(), any())).thenReturn(false);

        // Then
        assertThrows(SecurityException.class, () -> {
            reportingService.generateReport(request);
        });
    }

    @Test
    void testGetAvailableTemplates() {
        // Given
        List<String> userRoles = List.of("DENTIST", "CLINIC_ADMIN");
        List<ReportTemplate> templates = List.of(
            ReportTemplate.builder()
                .templateCode("TEMPLATE1")
                .name("Template 1")
                .allowedRoles(List.of("DENTIST"))
                .build(),
            ReportTemplate.builder()
                .templateCode("TEMPLATE2")
                .name("Template 2")
                .allowedRoles(List.of("CLINIC_ADMIN"))
                .build()
        );

        // When
        when(templateRepository.findByActiveAndAllowedRolesIn(true, userRoles)).thenReturn(templates);

        // Then
        List<ReportTemplate> result = reportingService.getAvailableTemplates(userRoles);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("TEMPLATE1", result.get(0).getTemplateCode());
        assertEquals("TEMPLATE2", result.get(1).getTemplateCode());
    }
}
