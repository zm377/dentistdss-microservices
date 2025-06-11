package press.mizhifei.dentist.system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import press.mizhifei.dentist.system.dto.RateLimitConfigRequest;
import press.mizhifei.dentist.system.dto.RateLimitConfigResponse;
import press.mizhifei.dentist.system.model.RateLimitConfig;
import press.mizhifei.dentist.system.repository.RateLimitConfigRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RateLimitConfigService
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@ExtendWith(MockitoExtension.class)
class RateLimitConfigServiceTest {

    @Mock
    private RateLimitConfigRepository repository;

    private RateLimitConfigService service;

    @BeforeEach
    void setUp() {
        service = new RateLimitConfigService(repository);
    }

    @Test
    void testCreateOrUpdate_NewConfig_ShouldCreateNew() {
        // Given
        RateLimitConfigRequest request = RateLimitConfigRequest.builder()
                .configName("test-config")
                .serviceName("test-service")
                .endpointPattern("/api/test/**")
                .maxRequests(1000L)
                .timeWindowSeconds(3600L)
                .limitType(RateLimitConfig.RateLimitType.REQUEST_COUNT)
                .build();

        RateLimitConfig savedConfig = RateLimitConfig.builder()
                .id(1L)
                .configName("test-config")
                .serviceName("test-service")
                .endpointPattern("/api/test/**")
                .maxRequests(1000L)
                .timeWindowSeconds(3600L)
                .limitType(RateLimitConfig.RateLimitType.REQUEST_COUNT)
                .build();

        when(repository.findByConfigName(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(RateLimitConfig.class))).thenReturn(savedConfig);

        // When
        RateLimitConfigResponse response = service.createOrUpdate(request);

        // Then
        assertNotNull(response);
        assertEquals("test-config", response.getConfigName());
        assertEquals("test-service", response.getServiceName());
        assertEquals("/api/test/**", response.getEndpointPattern());
        assertEquals(1000L, response.getMaxRequests());
        assertEquals(3600L, response.getTimeWindowSeconds());
        assertEquals(RateLimitConfig.RateLimitType.REQUEST_COUNT, response.getLimitType());

        verify(repository).findByConfigName("test-config");
        verify(repository).save(any(RateLimitConfig.class));
    }

    @Test
    void testGetActiveConfigurations() {
        // Given
        List<RateLimitConfig> activeConfigs = List.of(
                RateLimitConfig.builder()
                        .id(1L)
                        .configName("config1")
                        .serviceName("service1")
                        .active(true)
                        .build(),
                RateLimitConfig.builder()
                        .id(2L)
                        .configName("config2")
                        .serviceName("service2")
                        .active(true)
                        .build()
        );

        when(repository.findByActiveTrue()).thenReturn(activeConfigs);

        // When
        List<RateLimitConfigResponse> responses = service.getActiveConfigurations();

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("config1", responses.get(0).getConfigName());
        assertEquals("config2", responses.get(1).getConfigName());

        verify(repository).findByActiveTrue();
    }
}
