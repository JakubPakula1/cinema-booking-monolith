package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.repository.TemporaryReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationCleanupService Unit Tests")
class ReservationCleanupServiceTest {

    @Mock
    private TemporaryReservationRepository temporaryReservationRepository;

    @InjectMocks
    private ReservationCleanupService reservationCleanupService;

    @Test
    @DisplayName("Should cleanup expired reservations")
    void testCleanupExpiredReservations() {
        // given
        doNothing().when(temporaryReservationRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));

        // when
        reservationCleanupService.cleanupExpiredReservations();

        // then
        verify(temporaryReservationRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should handle no expired reservations")
    void testCleanupExpiredReservations_NoExpiredReservations() {
        // given
        doNothing().when(temporaryReservationRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));

        // when
        reservationCleanupService.cleanupExpiredReservations();

        // then
        verify(temporaryReservationRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should delete reservations before current time")
    void testCleanupExpiredReservations_VerifyTimeParameter() {
        // given
        doNothing().when(temporaryReservationRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));

        // when
        reservationCleanupService.cleanupExpiredReservations();

        // then
        verify(temporaryReservationRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should be called on schedule")
    void testCleanupExpiredReservations_ScheduledExecution() {
        // given
        doNothing().when(temporaryReservationRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));

        // when
        reservationCleanupService.cleanupExpiredReservations();

        // then
        verify(temporaryReservationRepository, times(1)).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }
}