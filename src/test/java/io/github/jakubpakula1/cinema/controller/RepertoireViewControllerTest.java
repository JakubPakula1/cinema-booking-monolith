package io.github.jakubpakula1.cinema.controller;

import io.github.jakubpakula1.cinema.controller.view.RepertoireViewController;
import io.github.jakubpakula1.cinema.dto.screening.RepertoireMovieDTO;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(RepertoireViewController.class)
@Import(SecurityConfig.class)
@DisplayName("Repertoire View Controller Tests")
public class RepertoireViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScreeningService screeningService;

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should show repertoire with default date (today)")
    public void shouldShowRepertoireWithDefaultDate() throws Exception {
        // Given
        LocalDate today = LocalDate.now();
        List<RepertoireMovieDTO> movies = Arrays.asList(
                RepertoireMovieDTO.builder()
                        .movieId(1L)
                        .title("Matrix")
                        .genre("Action")
                        .duration(120)
                        .build(),
                RepertoireMovieDTO.builder()
                        .movieId(2L)
                        .title("Inception")
                        .genre("Sci-Fi")
                        .duration(148)
                        .build()
        );
        when(screeningService.getRepertoireForDate(today)).thenReturn(movies);

        // When & Then
        mockMvc.perform(get("/repertoire"))
                .andExpect(status().isOk())
                .andExpect(view().name("repertoire"))
                .andExpect(model().attributeExists("movies"))
                .andExpect(model().attributeExists("selectedDate"))
                .andExpect(model().attributeExists("nextDays"))
                .andExpect(model().attribute("movies", hasSize(2)))
                .andExpect(model().attribute("selectedDate", today));

        verify(screeningService, times(1)).getRepertoireForDate(today);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should show repertoire with provided custom date")
    public void shouldShowRepertoireWithProvidedDate() throws Exception {
        // Given
        LocalDate selectedDate = LocalDate.now().plusDays(3);
        List<RepertoireMovieDTO> movies = Arrays.asList(
                RepertoireMovieDTO.builder()
                        .movieId(1L)
                        .title("Oppenheimer")
                        .genre("Drama")
                        .duration(180)
                        .build()
        );
        when(screeningService.getRepertoireForDate(selectedDate)).thenReturn(movies);

        // When & Then
        mockMvc.perform(get("/repertoire").param("date", selectedDate.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("repertoire"))
                .andExpect(model().attribute("selectedDate", selectedDate))
                .andExpect(model().attribute("movies", hasSize(1)));

        verify(screeningService, times(1)).getRepertoireForDate(selectedDate);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return empty movies list when no screenings available for date")
    public void shouldReturnEmptyMoviesListWhenNoScreeningsForDate() throws Exception {
        // Given
        LocalDate selectedDate = LocalDate.now().plusDays(5);
        when(screeningService.getRepertoireForDate(selectedDate)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/repertoire").param("date", selectedDate.toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("movies", hasSize(0)));

        verify(screeningService, times(1)).getRepertoireForDate(selectedDate);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should provide next seven days in model for date selection")
    public void shouldProvideNextSevenDaysInModel() throws Exception {
        // Given
        LocalDate today = LocalDate.now();
        when(screeningService.getRepertoireForDate(any(LocalDate.class))).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/repertoire"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("nextDays"))
                .andExpect(model().attribute("nextDays", hasSize(7)));
    }
}