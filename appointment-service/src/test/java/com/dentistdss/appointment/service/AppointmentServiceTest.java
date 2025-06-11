package com.dentistdss.appointment.service;

import com.dentistdss.appointment.client.AuthServiceClient;
import com.dentistdss.appointment.client.ClinicServiceClient;
import com.dentistdss.appointment.client.NotificationClient;
import com.dentistdss.appointment.dto.AppointmentRequest;
import com.dentistdss.appointment.dto.AppointmentResponse;
import com.dentistdss.appointment.model.Appointment;
import com.dentistdss.appointment.model.AppointmentStatus;
import com.dentistdss.appointment.model.UrgencyLevel;
import com.dentistdss.appointment.repository.AppointmentRepository;
import com.dentistdss.appointment.repository.DentistAvailabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AppointmentService
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DentistAvailabilityRepository availabilityRepository;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private ClinicServiceClient clinicServiceClient;

    @InjectMocks
    private AppointmentService appointmentService;

    private AppointmentRequest appointmentRequest;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        appointmentRequest = AppointmentRequest.builder()
                .patientId(1L)
                .dentistId(2L)
                .clinicId(3L)
                .serviceId(4)
                .appointmentDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .reasonForVisit("Regular checkup")
                .urgencyLevel("ROUTINE")
                .createdBy(1L)
                .build();

        appointment = Appointment.builder()
                .id(1L)
                .patientId(1L)
                .dentistId(2L)
                .clinicId(3L)
                .serviceId(4)
                .appointmentDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .status(AppointmentStatus.REQUESTED)
                .reasonForVisit("Regular checkup")
                .urgency(UrgencyLevel.ROUTINE)
                .createdBy(1L)
                .build();
    }

    @Test
    void createAppointment_Success() {
        // Given
        when(appointmentRepository.findConflictingAppointments(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.saveWithCasting(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(appointment);
        when(authServiceClient.getUserFullName(any())).thenReturn("John Doe");

        // When
        AppointmentResponse response = appointmentService.createAppointment(appointmentRequest);

        // Then
        assertNotNull(response);
        assertEquals(appointment.getId(), response.getId());
        assertEquals(appointment.getPatientId(), response.getPatientId());
        verify(appointmentRepository).saveWithCasting(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void createAppointment_ConflictExists_ThrowsException() {
        // Given
        when(appointmentRepository.findConflictingAppointments(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(appointment));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            appointmentService.createAppointment(appointmentRequest);
        });
    }

    @Test
    void confirmAppointment_Success() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.updateStatusWithCasting(any(), any(), any())).thenReturn(appointment);
        when(authServiceClient.getUserFullName(any())).thenReturn("John Doe");

        // When
        AppointmentResponse response = appointmentService.confirmAppointment(1L, 2L);

        // Then
        assertNotNull(response);
        verify(appointmentRepository).updateStatusWithCasting(1L, AppointmentStatus.CONFIRMED.name(), 2L);
    }

    @Test
    void confirmAppointment_NotFound_ThrowsException() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.confirmAppointment(1L, 2L);
        });
    }

    @Test
    void cancelAppointment_Success() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.updateCancellationWithCasting(any(), any(), any(), any())).thenReturn(appointment);
        when(authServiceClient.getUserFullName(any())).thenReturn("John Doe");

        // When
        AppointmentResponse response = appointmentService.cancelAppointment(1L, "Patient request", 1L);

        // Then
        assertNotNull(response);
        verify(appointmentRepository).updateCancellationWithCasting(1L, AppointmentStatus.CANCELLED.name(), "Patient request", 1L);
    }
}
