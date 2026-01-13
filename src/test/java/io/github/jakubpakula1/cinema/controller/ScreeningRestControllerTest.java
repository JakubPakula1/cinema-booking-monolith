package io.github.jakubpakula1.cinema.controller;

import io.github.jakubpakula1.cinema.controller.api.ScreeningRestController;
import io.github.jakubpakula1.cinema.dto.screening.CollisionDTO;
import io.github.jakubpakula1.cinema.security.SecurityConfig;
import io.github.jakubpakula1.cinema.service.ScreeningService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScreeningRestController.class)
@Import(SecurityConfig.class)
@DisplayName("Screening Rest Controller Tests")
public class ScreeningRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScreeningService screeningService;

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should check collisions by movie, room and start time")
    void shouldCheckCollisionsByMovieRoomAndStartTime() throws Exception {
        // Given
        Long movieId = 1L;
        Long roomId = 1L;
        LocalDateTime startTime = LocalDateTime.of(2024, 12, 31, 18, 0, 0);

        CollisionDTO collision = CollisionDTO.builder()
                .movieTitle("Inception")
                .roomName("Room 1")
                .screeningTime(LocalDateTime.of(2024, 12, 31, 18, 0, 0))
                .endTime(LocalDateTime.of(2024, 12, 31, 20, 28, 0))
                .cleaningDurationMinutes(15L)
                .build();

        when(screeningService.getCollidingScreeningsByMovie(roomId, movieId, startTime))
                .thenReturn(List.of(collision));

        // When & Then
        mockMvc.perform(get("/api/v1/screenings/collisions")
                .param("movieId", "1")
                .param("roomId", "1")
                .param("startTime", "2024-12-31T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].movieTitle").value("Inception"))
                .andExpect(jsonPath("$[0].roomName").value("Room 1"))
                .andExpect(jsonPath("$[0].screeningTime").exists())
                .andExpect(jsonPath("$[0].endTime").exists())
                .andExpect(jsonPath("$[0].cleaningDurationMinutes").value(15));

        verify(screeningService, times(1)).getCollidingScreeningsByMovie(roomId, movieId, startTime);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return empty list when no collisions found")
    void shouldReturnEmptyListWhenNoCollisionsFound() throws Exception {
        // Given
        Long movieId = 1L;
        Long roomId = 1L;
        LocalDateTime startTime = LocalDateTime.of(2024, 12, 31, 10, 0, 0);

        when(screeningService.getCollidingScreeningsByMovie(roomId, movieId, startTime))
                .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/v1/screenings/collisions")
                .param("movieId", "1")
                .param("roomId", "1")
                .param("startTime", "2024-12-31T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(screeningService, times(1)).getCollidingScreeningsByMovie(roomId, movieId, startTime);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return multiple collisions")
    void shouldReturnMultipleCollisions() throws Exception {
        // Given
        Long movieId = 1L;
        Long roomId = 1L;
        LocalDateTime startTime = LocalDateTime.of(2024, 12, 31, 18, 0, 0);

        List<CollisionDTO> collisions = List.of(
                CollisionDTO.builder()
                        .movieTitle("Inception")
                        .roomName("Room 1")
                        .screeningTime(LocalDateTime.of(2024, 12, 31, 18, 0, 0))
                        .endTime(LocalDateTime.of(2024, 12, 31, 20, 28, 0))
                        .cleaningDurationMinutes(15L)
                        .build(),
                CollisionDTO.builder()
                        .movieTitle("The Dark Knight")
                        .roomName("Room 1")
                        .screeningTime(LocalDateTime.of(2024, 12, 31, 20, 45, 0))
                        .endTime(LocalDateTime.of(2024, 12, 31, 23, 17, 0))
                        .cleaningDurationMinutes(15L)
                        .build()
        );

        when(screeningService.getCollidingScreeningsByMovie(roomId, movieId, startTime))
                .thenReturn(collisions);

        // When & Then
        mockMvc.perform(get("/api/v1/screenings/collisions")
                .param("movieId", "1")
                .param("roomId", "1")
                .param("startTime", "2024-12-31T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].movieTitle").value("Inception"))
                .andExpect(jsonPath("$[1].movieTitle").value("The Dark Knight"));

        verify(screeningService, times(1)).getCollidingScreeningsByMovie(roomId, movieId, startTime);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should check availability by room and time range")
    void shouldCheckAvailabilityByRoomAndTimeRange() throws Exception {
        // Given
        Long roomId = 1L;
        LocalDateTime from = LocalDateTime.of(2024, 12, 31, 10, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 12, 31, 22, 0, 0);

        CollisionDTO collision = CollisionDTO.builder()
                .movieTitle("Avatar")
                .roomName("Room 2")
                .screeningTime(LocalDateTime.of(2024, 12, 31, 15, 0, 0))
                .endTime(LocalDateTime.of(2024, 12, 31, 17, 50, 0))
                .cleaningDurationMinutes(20L)
                .build();

        when(screeningService.getCollidingScreenings(roomId, from, to))
                .thenReturn(List.of(collision));

        // When & Then
        mockMvc.perform(get("/api/v1/screenings/check")
                .param("roomId", "1")
                .param("from", "2024-12-31T10:00:00")
                .param("to", "2024-12-31T22:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].movieTitle").value("Avatar"))
                .andExpect(jsonPath("$[0].roomName").value("Room 2"))
                .andExpect(jsonPath("$[0].cleaningDurationMinutes").value(20));

        verify(screeningService, times(1)).getCollidingScreenings(roomId, from, to);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return empty list when no screenings in time range")
    void shouldReturnEmptyListWhenNoScreeningsInTimeRange() throws Exception {
        // Given
        Long roomId = 1L;
        LocalDateTime from = LocalDateTime.of(2024, 12, 31, 8, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 12, 31, 10, 0, 0);

        when(screeningService.getCollidingScreenings(roomId, from, to))
                .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/v1/screenings/check")
                .param("roomId", "1")
                .param("from", "2024-12-31T08:00:00")
                .param("to", "2024-12-31T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(screeningService, times(1)).getCollidingScreenings(roomId, from, to);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should pass correct parameters to service for collisions endpoint")
    void shouldPassCorrectParametersToServiceForCollisionsEndpoint() throws Exception {
        // Given
        Long movieId = 5L;
        Long roomId = 3L;
        LocalDateTime startTime = LocalDateTime.of(2024, 12, 25, 14, 30, 0);

        when(screeningService.getCollidingScreeningsByMovie(roomId, movieId, startTime))
                .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/v1/screenings/collisions")
                .param("movieId", "5")
                .param("roomId", "3")
                .param("startTime", "2024-12-25T14:30:00"))
                .andExpect(status().isOk());

        verify(screeningService, times(1)).getCollidingScreeningsByMovie(3L, 5L, startTime);
        verifyNoMoreInteractions(screeningService);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should pass correct parameters to service for check endpoint")
    void shouldPassCorrectParametersToServiceForCheckEndpoint() throws Exception {
        // Given
        Long roomId = 2L;
        LocalDateTime from = LocalDateTime.of(2024, 12, 25, 9, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 12, 25, 21, 0, 0);

        when(screeningService.getCollidingScreenings(roomId, from, to))
                .thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/v1/screenings/check")
                .param("roomId", "2")
                .param("from", "2024-12-25T09:00:00")
                .param("to", "2024-12-25T21:00:00"))
                .andExpect(status().isOk());

        verify(screeningService, times(1)).getCollidingScreenings(2L, from, to);
        verifyNoMoreInteractions(screeningService);
    }
}