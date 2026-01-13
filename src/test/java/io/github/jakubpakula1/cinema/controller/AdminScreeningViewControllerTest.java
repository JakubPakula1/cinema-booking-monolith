package io.github.jakubpakula1.cinema.controller;

import io.github.jakubpakula1.cinema.controller.view.admin.AdminScreeningViewController;
import io.github.jakubpakula1.cinema.dto.screening.ScreeningDTO;
import io.github.jakubpakula1.cinema.dto.screening.ScreeningListDTO;
import io.github.jakubpakula1.cinema.exception.ScreeningDateInPastException;
import io.github.jakubpakula1.cinema.exception.ScreeningOverlapException;
import io.github.jakubpakula1.cinema.model.Movie;
import io.github.jakubpakula1.cinema.model.Room;
import io.github.jakubpakula1.cinema.security.SecurityConfig;
import io.github.jakubpakula1.cinema.service.MovieService;
import io.github.jakubpakula1.cinema.service.RoomService;
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
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminScreeningViewController.class)
@Import(SecurityConfig.class)
@DisplayName("Admin Screening View Controller Tests")
public class AdminScreeningViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScreeningService screeningService;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private RoomService roomService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should display all screenings list")
    void shouldDisplayAllScreeningsList() throws Exception {
        // Given
        List<ScreeningListDTO> screenings = List.of(
                ScreeningListDTO.builder()
                        .id(1L)
                        .movieTitle("Inception")
                        .roomName("Room 1")
                        .startTime(LocalTime.of(18, 0, 0))
                        .build(),
                ScreeningListDTO.builder()
                        .id(2L)
                        .movieTitle("The Dark Knight")
                        .roomName("Room 2")
                        .startTime(LocalTime.of(20, 0, 0))
                        .build()
        );

        when(screeningService.getAllScreeningsForList()).thenReturn(screenings);

        // When & Then
        mockMvc.perform(get("/admin/screenings"))
                .andExpect(status().isOk())
                .andExpect(view().name("screening/admin/list"))
                .andExpect(model().attributeExists("screenings"))
                .andExpect(model().attribute("screenings", hasSize(2)))
                .andExpect(model().attribute("screenings", screenings));

        verify(screeningService, times(1)).getAllScreeningsForList();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return empty screenings list when no screenings exist")
    void shouldReturnEmptyScreeningsListWhenNoScreeningsExist() throws Exception {
        // Given
        when(screeningService.getAllScreeningsForList()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/admin/screenings"))
                .andExpect(status().isOk())
                .andExpect(view().name("screening/admin/list"))
                .andExpect(model().attributeExists("screenings"))
                .andExpect(model().attribute("screenings", hasSize(0)));

        verify(screeningService, times(1)).getAllScreeningsForList();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should show screening form for adding new screening")
    void shouldShowScreeningFormForAddingNewScreening() throws Exception {
        // Given
        List<Movie> movies = List.of(
                Movie.builder().id(1L).title("Inception").build(),
                Movie.builder().id(2L).title("The Dark Knight").build()
        );
        Room room1 = new Room();
        room1.setId(1L);
        room1.setName("Room 1");

        Room room2 = new Room();
        room2.setId(2L);
        room2.setName("Room 2");
        List<Room> rooms = List.of(room1, room2);

        when(movieService.getAllMovies()).thenReturn(movies);
        when(roomService.getAllRooms()).thenReturn(rooms);

        // When & Then
        mockMvc.perform(get("/admin/screenings/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("screening/admin/screening-form"))
                .andExpect(model().attributeExists("action", "screening", "movies", "rooms"))
                .andExpect(model().attribute("action", "add"))
                .andExpect(model().attribute("movies", hasSize(2)))
                .andExpect(model().attribute("rooms", hasSize(2)));

        verify(movieService, times(1)).getAllMovies();
        verify(roomService, times(1)).getAllRooms();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should add screening successfully")
    void shouldAddScreeningSuccessfully() throws Exception {
        // Given
        ScreeningDTO screeningDTO = ScreeningDTO.builder()
                .movieId(1L)
                .roomId(1L)
                .screeningTime(LocalDateTime.of(2024, 12, 31, 18, 0, 0))
                .build();

        doNothing().when(screeningService).createScreening(any(ScreeningDTO.class));

        // When & Then
        mockMvc.perform(post("/admin/screenings/add")
                .with(csrf())
                .param("movieId", "1")
                .param("roomId", "1")
                .param("screeningTime", "2024-12-31T18:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/screenings"));

        verify(screeningService, times(1)).createScreening(any(ScreeningDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle screening overlap exception when adding")
    void shouldHandleScreeningOverlapExceptionWhenAdding() throws Exception {
        // Given
        ScreeningDTO screeningDTO = ScreeningDTO.builder()
                .movieId(1L)
                .roomId(1L)
                .screeningTime(LocalDateTime.of(2024, 12, 31, 18, 0, 0))
                .build();

        Room room1 = new Room();
        room1.setId(1L);
        room1.setName("Room 1");

        List<Movie> movies = List.of(Movie.builder().id(1L).title("Inception").build());
        List<Room> rooms = List.of(room1);

        doThrow(new ScreeningOverlapException("Screening time overlaps with existing screening"))
                .when(screeningService).createScreening(any(ScreeningDTO.class));
        when(movieService.getAllMovies()).thenReturn(movies);
        when(roomService.getAllRooms()).thenReturn(rooms);

        // When & Then
        mockMvc.perform(post("/admin/screenings/add")
                .with(csrf())
                .param("movieId", "1")
                .param("roomId", "1")
                .param("screeningTime", "2024-12-31T18:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("screening/admin/screening-form"))
                .andExpect(model().attributeExists("errorMessage", "movies", "rooms", "screening"))
                .andExpect(model().attribute("errorMessage", "Screening time overlaps with existing screening"));

        verify(screeningService, times(1)).createScreening(any(ScreeningDTO.class));
        verify(movieService, times(1)).getAllMovies();
        verify(roomService, times(1)).getAllRooms();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle screening date in past exception when adding")
    void shouldHandleScreeningDateInPastExceptionWhenAdding() throws Exception {
        // Given
        Room room1 = new Room();
        room1.setId(1L);
        room1.setName("Room 1");

        List<Movie> movies = List.of(Movie.builder().id(1L).title("Inception").build());
        List<Room> rooms = List.of(room1);

        doThrow(new ScreeningDateInPastException())
                .when(screeningService).createScreening(any(ScreeningDTO.class));
        when(movieService.getAllMovies()).thenReturn(movies);
        when(roomService.getAllRooms()).thenReturn(rooms);

        // When & Then
        mockMvc.perform(post("/admin/screenings/add")
                .with(csrf())
                .param("movieId", "1")
                .param("roomId", "1")
                .param("screeningTime", "2020-01-01T18:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("screening/admin/screening-form"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "Screening date cannot be in the past."));

        verify(screeningService, times(1)).createScreening(any(ScreeningDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should show edit form for existing screening")
    void shouldShowEditFormForExistingScreening() throws Exception {
        // Given
        Long screeningId = 1L;
        ScreeningDTO screening = ScreeningDTO.builder()
                .id(screeningId)
                .movieId(1L)
                .roomId(1L)
                .screeningTime(LocalDateTime.of(2024, 12, 31, 18, 0, 0))
                .build();

        Room room1 = new Room();
        room1.setId(1L);
        room1.setName("Room 1");

        List<Movie> movies = List.of(Movie.builder().id(1L).title("Inception").build());
        List<Room> rooms = List.of(room1);

        when(screeningService.getScreeningById(screeningId)).thenReturn(screening);
        when(movieService.getAllMovies()).thenReturn(movies);
        when(roomService.getAllRooms()).thenReturn(rooms);

        // When & Then
        mockMvc.perform(get("/admin/screenings/edit/{id}", screeningId))
                .andExpect(status().isOk())
                .andExpect(view().name("screening/admin/screening-form"))
                .andExpect(model().attributeExists("action", "screening", "movies", "rooms"))
                .andExpect(model().attribute("action", "edit"))
                .andExpect(model().attribute("screening", screening));

        verify(screeningService, times(1)).getScreeningById(screeningId);
        verify(movieService, times(1)).getAllMovies();
        verify(roomService, times(1)).getAllRooms();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update screening successfully")
    void shouldUpdateScreeningSuccessfully() throws Exception {
        // Given
        Long screeningId = 1L;
        ScreeningDTO screeningDTO = ScreeningDTO.builder()
                .movieId(1L)
                .roomId(1L)
                .screeningTime(LocalDateTime.of(2024, 12, 31, 19, 0, 0))
                .build();

        doNothing().when(screeningService).updateScreening(eq(screeningId), any(ScreeningDTO.class));

        // When & Then
        mockMvc.perform(put("/admin/screenings/edit/{id}", screeningId)
                .with(csrf())
                .param("movieId", "1")
                .param("roomId", "1")
                .param("screeningTime", "2024-12-31T19:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/screenings"));

        verify(screeningService, times(1)).updateScreening(eq(screeningId), any(ScreeningDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle screening overlap exception when editing")
    void shouldHandleScreeningOverlapExceptionWhenEditing() throws Exception {
        // Given
        Long screeningId = 1L;
        ScreeningDTO screening = ScreeningDTO.builder()
                .id(screeningId)
                .movieId(1L)
                .roomId(1L)
                .screeningTime(LocalDateTime.of(2024, 12, 31, 18, 0, 0))
                .build();

        Room room1 = new Room();
        room1.setId(1L);
        room1.setName("Room 1");

        List<Movie> movies = List.of(Movie.builder().id(1L).title("Inception").build());
        List<Room> rooms = List.of(room1);

        doThrow(new ScreeningOverlapException("Screening time overlaps with existing screening"))
                .when(screeningService).updateScreening(eq(screeningId), any(ScreeningDTO.class));
        when(screeningService.getScreeningById(screeningId)).thenReturn(screening);
        when(movieService.getAllMovies()).thenReturn(movies);
        when(roomService.getAllRooms()).thenReturn(rooms);

        // When & Then
        mockMvc.perform(put("/admin/screenings/edit/{id}", screeningId)
                .with(csrf())
                .param("movieId", "1")
                .param("roomId", "1")
                .param("screeningTime", "2024-12-31T18:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("screening/admin/screening-form"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "Screening time overlaps with existing screening"));

        verify(screeningService, times(1)).updateScreening(eq(screeningId), any(ScreeningDTO.class));
        verify(screeningService, times(1)).getScreeningById(screeningId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle screening date in past exception when editing")
    void shouldHandleScreeningDateInPastExceptionWhenEditing() throws Exception {
        // Given
        Long screeningId = 1L;
        ScreeningDTO screening = ScreeningDTO.builder()
                .id(screeningId)
                .movieId(1L)
                .roomId(1L)
                .screeningTime(LocalDateTime.of(2024, 12, 31, 18, 0, 0))
                .build();

        Room room1 = new Room();
        room1.setId(1L);
        room1.setName("Room 1");

        List<Movie> movies = List.of(Movie.builder().id(1L).title("Inception").build());
        List<Room> rooms = List.of(room1);

        doThrow(new ScreeningDateInPastException())
                .when(screeningService).updateScreening(eq(screeningId), any(ScreeningDTO.class));
        when(screeningService.getScreeningById(screeningId)).thenReturn(screening);
        when(movieService.getAllMovies()).thenReturn(movies);
        when(roomService.getAllRooms()).thenReturn(rooms);

        // When & Then
        mockMvc.perform(put("/admin/screenings/edit/{id}", screeningId)
                .with(csrf())
                .param("movieId", "1")
                .param("roomId", "1")
                .param("screeningTime", "2020-01-01T18:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("screening/admin/screening-form"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(screeningService, times(1)).updateScreening(eq(screeningId), any(ScreeningDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should delete screening successfully")
    void shouldDeleteScreeningSuccessfully() throws Exception {
        // Given
        Long screeningId = 1L;
        doNothing().when(screeningService).deleteScreening(screeningId);

        // When & Then
        mockMvc.perform(delete("/admin/screenings/delete/{id}", screeningId)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/screenings"));

        verify(screeningService, times(1)).deleteScreening(screeningId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should deny access to screening list for non-admin user")
    void shouldDenyAccessToScreeningListForNonAdminUser() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/screenings"))
                .andExpect(status().is4xxClientError());

        verifyNoInteractions(screeningService);
    }

    @Test
    @DisplayName("Should redirect to login when accessing screening endpoints without authentication")
    void shouldRedirectToLoginWhenAccessingScreeningEndpointsUnauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/screenings"))
                .andExpect(status().is3xxRedirection());

        verifyNoInteractions(screeningService);
    }
}