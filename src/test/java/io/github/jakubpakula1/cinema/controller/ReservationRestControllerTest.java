package io.github.jakubpakula1.cinema.controller;

import io.github.jakubpakula1.cinema.controller.api.ReservationRestController;
import io.github.jakubpakula1.cinema.dto.reservation.ReservationRequestDTO;
import io.github.jakubpakula1.cinema.model.Screening;
import io.github.jakubpakula1.cinema.model.Seat;
import io.github.jakubpakula1.cinema.model.TemporaryReservation;
import io.github.jakubpakula1.cinema.model.User;
import io.github.jakubpakula1.cinema.security.SecurityConfig;
import io.github.jakubpakula1.cinema.service.ReservationService;
import io.github.jakubpakula1.cinema.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationRestController.class)
@Import(SecurityConfig.class)
@DisplayName("Reservation Rest Controller Tests")
public class ReservationRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should create reservation successfully")
    void shouldCreateReservationSuccessfully() throws Exception {
        // Given
        Long seatId = 1L;
        Long screeningId = 1L;
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setSeatId(seatId);
        request.setScreeningId(screeningId);

        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        Seat seat = new Seat();
        seat.setId(seatId);

        Screening screening = new Screening();
        screening.setId(screeningId);

        TemporaryReservation tempRes = new TemporaryReservation();
        tempRes.setId(1L);
        tempRes.setSeat(seat);
        tempRes.setScreening(screening);
        tempRes.setExpiresAt(expiresAt);

        when(userService.getUserByEmail("user@example.com")).thenReturn(user);
        when(reservationService.createTemporaryReservation(any(ReservationRequestDTO.class), eq(user))).thenReturn(tempRes);

        // When & Then
        mockMvc.perform(post("/api/v1/reservations/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"seatId\": 1, \"screeningId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.seatId").value(seatId))
                .andExpect(jsonPath("$.screeningId").value(screeningId))
                .andExpect(jsonPath("$.expiresAt").exists());

        verify(userService, times(1)).getUserByEmail("user@example.com");
        verify(reservationService, times(1)).createTemporaryReservation(any(ReservationRequestDTO.class), eq(user));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should include expiration time in response")
    void shouldIncludeExpirationTimeInResponse() throws Exception {
        // Given
        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setSeatId(1L);
        request.setScreeningId(1L);

        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        LocalDateTime expiresAt = LocalDateTime.of(2024, 12, 31, 15, 30, 0);

        Screening screening = new Screening();
        screening.setId(1L);

        Seat seat = new Seat();
        seat.setId(1L);

        TemporaryReservation tempRes = new TemporaryReservation();
        tempRes.setId(1L);
        tempRes.setSeat(seat);
        tempRes.setScreening(screening);
        tempRes.setExpiresAt(expiresAt);

        when(userService.getUserByEmail("user@example.com")).thenReturn(user);
        when(reservationService.createTemporaryReservation(any(ReservationRequestDTO.class), eq(user))).thenReturn(tempRes);

        // When & Then
        mockMvc.perform(post("/api/v1/reservations/lock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatId\": 1, \"screeningId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresAt").exists());

        verify(reservationService, times(1)).createTemporaryReservation(any(ReservationRequestDTO.class), eq(user));
    }

    @Test
    @DisplayName("Should return 302 redirect when creating reservation without authentication")
    void shouldReturn302RedirectWhenCreatingReservationUnauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/reservations/lock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatId\": 1, \"screeningId\": 1}"))
                .andExpect(status().is3xxRedirection())
                .andExpect(status().isFound());

        verifyNoInteractions(userService);
        verifyNoInteractions(reservationService);
    }

    @Test
    @DisplayName("Should return 302 redirect when canceling reservation without authentication")
    void shouldReturn302RedirectWhenCancelingReservationUnauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/reservations/lock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatId\": 1, \"screeningId\": 1}"))
                .andExpect(status().is3xxRedirection())
                .andExpect(status().isFound());

        verifyNoInteractions(userService);
        verifyNoInteractions(reservationService);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should cancel reservation successfully")
    void shouldCancelReservationSuccessfully() throws Exception {
        // Given
        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setSeatId(1L);
        request.setScreeningId(1L);

        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        when(userService.getUserByEmail("user@example.com")).thenReturn(user);
        doNothing().when(reservationService).deleteTemporaryReservation(any(ReservationRequestDTO.class), eq(user));

        // When & Then
        mockMvc.perform(delete("/api/v1/reservations/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"seatId\": 1, \"screeningId\": 1}"))
                .andExpect(status().isOk());

        verify(userService, times(1)).getUserByEmail("user@example.com");
        verify(reservationService, times(1)).deleteTemporaryReservation(any(ReservationRequestDTO.class), eq(user));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should pass correct user to reservation service")
    void shouldPassCorrectUserToReservationService() throws Exception {
        // Given
        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setSeatId(1L);
        request.setScreeningId(1L);

        User user = new User();
        user.setId(2L);
        user.setEmail("user@example.com");

        TemporaryReservation tempRes = new TemporaryReservation();
        tempRes.setId(1L);
        tempRes.setSeat(new Seat());
        tempRes.setScreening(new Screening());
        tempRes.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        when(userService.getUserByEmail("user@example.com")).thenReturn(user);
        when(reservationService.createTemporaryReservation(any(ReservationRequestDTO.class), eq(user))).thenReturn(tempRes);

        // When & Then
        mockMvc.perform(post("/api/v1/reservations/lock")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"seatId\": 1, \"screeningId\": 1}"))
                .andExpect(status().isOk());

        verify(userService, times(1)).getUserByEmail("user@example.com");
        verify(reservationService, times(1)).createTemporaryReservation(any(ReservationRequestDTO.class), eq(user));
    }
}