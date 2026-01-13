package io.github.jakubpakula1.cinema.controller;

import io.github.jakubpakula1.cinema.controller.view.HomeViewController;
import io.github.jakubpakula1.cinema.repository.projection.MovieCarouselDTO;
import io.github.jakubpakula1.cinema.security.SecurityConfig;
import io.github.jakubpakula1.cinema.service.MovieService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeViewController.class)
@Import(SecurityConfig.class)
@DisplayName("Home View Controller Tests")
public class HomeViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @Test
    @DisplayName("Should show home page with latest movies")
    public void shouldShowHomePageWithLatestMovies() throws Exception {
        // Given
        List<MovieCarouselDTO> latestMovies = List.of(
                new MovieCarouselDTO(1L, "Movie 1", "Action", "backdrop1.jpg", "Short description 1"),
                new MovieCarouselDTO(2L, "Movie 2", "Drama", "backdrop2.jpg", "Short description 2"),
                new MovieCarouselDTO(3L, "Movie 3", "Comedy", "backdrop3.jpg", "Short description 3")
        );

        when(movieService.getLatestMovies(3)).thenReturn(latestMovies);

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("latestMovies"))
                .andExpect(model().attribute("latestMovies", hasSize(3)));

        verify(movieService, times(1)).getLatestMovies(3);
    }

    @Test
    @DisplayName("Should show home page with empty movies list")
    public void shouldShowHomePageWithEmptyMoviesList() throws Exception {
        // Given
        when(movieService.getLatestMovies(3)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("latestMovies"))
                .andExpect(model().attribute("latestMovies", hasSize(0)));

        verify(movieService, times(1)).getLatestMovies(3);
    }

    @Test
    @DisplayName("Should call MovieService with correct limit parameter")
    public void shouldCallMovieServiceWithCorrectLimitParameter() throws Exception {
        // Given
        List<MovieCarouselDTO> movies = List.of(
                new MovieCarouselDTO(1L, "Movie 1", "Action", "backdrop1.jpg", "Description 1")
        );

        when(movieService.getLatestMovies(3)).thenReturn(movies);

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        verify(movieService, times(1)).getLatestMovies(3);
        verifyNoMoreInteractions(movieService);
    }

    @Test
    @DisplayName("Should display movie details in model")
    public void shouldDisplayMovieDetailsInModel() throws Exception {
        // Given
        MovieCarouselDTO movie = new MovieCarouselDTO(
                1L,
                "Inception",
                "Sci-Fi",
                "inception-backdrop.jpg",
                "A thief who steals corporate secrets"
        );

        when(movieService.getLatestMovies(3)).thenReturn(List.of(movie));

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("latestMovies"))
                .andExpect(model().attribute("latestMovies", hasSize(1)));

        verify(movieService, times(1)).getLatestMovies(3);
    }
}