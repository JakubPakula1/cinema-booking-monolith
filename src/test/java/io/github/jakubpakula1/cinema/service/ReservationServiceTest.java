package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.dto.booking.BookingSummaryDTO;
import io.github.jakubpakula1.cinema.dto.reservation.ReservationRequestDTO;
import io.github.jakubpakula1.cinema.exception.EmptyCartException;
import io.github.jakubpakula1.cinema.exception.ResourceNotFoundException;
import io.github.jakubpakula1.cinema.model.*;
import io.github.jakubpakula1.cinema.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService Unit Tests")
class ReservationServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ScreeningRepository screeningRepository;

    @Mock
    private TemporaryReservationRepository temporaryReservationRepository;

    @Mock
    private UserService userService;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User testUser;
    private Seat testSeat;
    private Screening testScreening;
    private Movie testMovie;
    private ReservationRequestDTO reservationRequest;
    private TemporaryReservation testReservation;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@test.com");
        testUser.setPassword("pass");

        testSeat = new Seat();
        testSeat.setId(1L);
        testSeat.setSeatNumber(5);
        testSeat.setRowNumber(3);


        testMovie = Movie.builder()
                .id(1L)
                .title("Test Movie")
                .build();

        testScreening = new Screening();
        testScreening.setId(1L);
        testScreening.setStartTime(LocalDateTime.now().plusDays(1));
        testScreening.setEndTime(testScreening.getStartTime().plusMinutes(120));

        reservationRequest = new ReservationRequestDTO();
        reservationRequest.setSeatId(1L);
        reservationRequest.setScreeningId(1L);

        testReservation = new TemporaryReservation();
        testReservation.setId(1L);
        testReservation.setSeat(testSeat);
        testReservation.setScreening(testScreening);
        testReservation.setUser(testUser);
        testReservation.setExpiresAt(LocalDateTime.now().plusMinutes(10));
    }

    @Test
    @DisplayName("Should create temporary reservation successfully")
    void testCreateTemporaryReservation_Success() {
        // given
        when(seatRepository.findSeatWithLock(1L)).thenReturn(Optional.of(testSeat));
        when(temporaryReservationRepository.existsBySeatIdAndScreeningIdAndExpiresAtAfter(eq(1L), eq(1L), any(LocalDateTime.class)))
                .thenReturn(false);
        when(ticketRepository.existsBySeatIdAndScreeningId(1L, 1L))
                .thenReturn(false);
        when(screeningRepository.getReferenceById(1L)).thenReturn(testScreening);
        when(temporaryReservationRepository.save(any(TemporaryReservation.class))).thenReturn(testReservation);
        when(temporaryReservationRepository.findAllByUserIdAndExpiresAtAfter(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(testReservation));

        // when
        TemporaryReservation result = reservationService.createTemporaryReservation(reservationRequest, testUser);

        // then
        assertThat(result)
                .isNotNull()
                .extracting( "seat", "screening", "user")
                .containsExactly(testSeat, testScreening, testUser);

        verify(seatRepository).findSeatWithLock(1L);
        verify(temporaryReservationRepository).existsBySeatIdAndScreeningIdAndExpiresAtAfter(eq(1L), eq(1L), any(LocalDateTime.class));
        verify(ticketRepository).existsBySeatIdAndScreeningId(1L, 1L);
        verify(temporaryReservationRepository).save(any(TemporaryReservation.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when seat not found")
    void testCreateTemporaryReservation_SeatNotFound() {
        // given
        when(seatRepository.findSeatWithLock(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.createTemporaryReservation(reservationRequest, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Seat not found");

        verify(seatRepository).findSeatWithLock(1L);
        verify(temporaryReservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when seat is already reserved")
    void testCreateTemporaryReservation_SeatAlreadyReserved() {
        // given
        when(seatRepository.findSeatWithLock(1L)).thenReturn(Optional.of(testSeat));
        when(temporaryReservationRepository.existsBySeatIdAndScreeningIdAndExpiresAtAfter(eq(1L), eq(1L), any(LocalDateTime.class)))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationService.createTemporaryReservation(reservationRequest, testUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Seat is already reserved");

        verify(seatRepository).findSeatWithLock(1L);
        verify(temporaryReservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when seat is already sold")
    void testCreateTemporaryReservation_SeatAlreadySold() {
        // given
        when(seatRepository.findSeatWithLock(1L)).thenReturn(Optional.of(testSeat));
        when(temporaryReservationRepository.existsBySeatIdAndScreeningIdAndExpiresAtAfter(eq(1L), eq(1L), any(LocalDateTime.class)))
                .thenReturn(false);
        when(ticketRepository.existsBySeatIdAndScreeningId(1L, 1L)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationService.createTemporaryReservation(reservationRequest, testUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Seat is already reserved");

        verify(temporaryReservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete temporary reservation successfully")
    void testDeleteTemporaryReservation_Success() {
        // given
        when(temporaryReservationRepository.findByUserIdAndSeatIdAndScreeningId(1L, 1L, 1L))
                .thenReturn(List.of(testReservation));

        // when
        reservationService.deleteTemporaryReservation(reservationRequest, testUser);

        // then
        verify(temporaryReservationRepository).findByUserIdAndSeatIdAndScreeningId(1L, 1L, 1L);
        verify(temporaryReservationRepository).deleteAll(List.of(testReservation));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when reservation not found for deletion")
    void testDeleteTemporaryReservation_NotFound() {
        // given
        when(temporaryReservationRepository.findByUserIdAndSeatIdAndScreeningId(1L, 1L, 1L))
                .thenReturn(List.of());

        // when & then
        assertThatThrownBy(() -> reservationService.deleteTemporaryReservation(reservationRequest, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Temporary reservation not found");

        verify(temporaryReservationRepository, never()).deleteAll(any());
    }

    @Test
    @DisplayName("Should prepare booking summary successfully")
    void testPrepareSummary_Success() {
        // given
        TicketType ticketType = new TicketType();
        ticketType.setId(1L);
        ticketType.setName("Adult");

         testScreening.setMovie(testMovie);

        when(userService.getUserByEmail("test@test.com")).thenReturn(testUser);
        when(temporaryReservationRepository.findAllByUserIdAndExpiresAtAfter(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(testReservation));
        when(ticketTypeRepository.findAll()).thenReturn(List.of(ticketType));

        // when
        BookingSummaryDTO result = reservationService.prepareSummary("test@test.com");

        // then
        assertThat(result)
                .isNotNull()
                .extracting("user", "screening", "movie")
                .containsExactly(testUser, testScreening, testMovie);

        assertThat(result.getReservations()).hasSize(1).contains(testReservation);
        assertThat(result.getTicketTypes()).hasSize(1).contains(ticketType);

        verify(userService).getUserByEmail("test@test.com");
        verify(temporaryReservationRepository).findAllByUserIdAndExpiresAtAfter(eq(1L), any(LocalDateTime.class));
        verify(ticketTypeRepository).findAll();
    }

    @Test
    @DisplayName("Should throw EmptyCartException when no reservations found")
    void testPrepareSummary_EmptyCart() {
        // given
        when(userService.getUserByEmail("test@test.com")).thenReturn(testUser);
        when(temporaryReservationRepository.findAllByUserIdAndExpiresAtAfter(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // when & then
        assertThatThrownBy(() -> reservationService.prepareSummary("test@test.com"))
                .isInstanceOf(EmptyCartException.class)
                .hasMessageContaining("Your cart is empty!");

        verify(ticketTypeRepository, never()).findAll();
    }

    @Test
    @DisplayName("Should update expiration time for all user reservations")
    void testPrepareSummary_UpdatesExpirationTime() {
        // given
        TicketType ticketType = new TicketType();
        ticketType.setId(1L);
        ticketType.setName("Adult");

        TemporaryReservation reservation2 = new TemporaryReservation();
        reservation2.setId(2L);
        reservation2.setSeat(testSeat);
        reservation2.setScreening(testScreening);
        reservation2.setUser(testUser);

        when(userService.getUserByEmail("test@test.com")).thenReturn(testUser);
        when(temporaryReservationRepository.findAllByUserIdAndExpiresAtAfter(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(testReservation, reservation2));
        when(ticketTypeRepository.findAll()).thenReturn(List.of(ticketType));

        // when
        BookingSummaryDTO result = reservationService.prepareSummary("test@test.com");

        // then
        assertThat(result.getReservations()).hasSize(2);

        verify(temporaryReservationRepository).findAllByUserIdAndExpiresAtAfter(eq(1L), any(LocalDateTime.class));
    }
}