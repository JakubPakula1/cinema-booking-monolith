package io.github.jakubpakula1.cinema.controller;

import io.github.jakubpakula1.cinema.controller.view.MovieViewController;
import io.github.jakubpakula1.cinema.repository.projection.MovieListView;
import io.github.jakubpakula1.cinema.security.SecurityConfig;
import io.github.jakubpakula1.cinema.service.MovieService;
import io.github.jakubpakula1.cinema.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieViewController.class)
@Import(SecurityConfig.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private UserService userService;

    // --- Lista Filmów ---
    @Test
    @DisplayName("Should show movies list to everyone")
    @WithMockUser(roles = "USER")
    void shouldShowMovieList() throws Exception {
        // given
        MovieListView movie1 = mock(MovieListView.class);
        MovieListView movie2 = mock(MovieListView.class);
        Page<MovieListView> movies = new PageImpl<>(List.of(movie1, movie2));

        when(movieService.getAllMoviesProjected(0,12)).thenReturn(movies);

        // when & then
        mockMvc.perform(get("/movies"))
                .andExpect(status().isOk())
                .andExpect(view().name("movies/movie-list")) // Czy zwrócił dobry plik HTML
                .andExpect(model().attribute("moviePage", movies)); // Czy w modelu są 2 filmy

        verify(movieService).getAllMoviesProjected(0,12);
    }
}