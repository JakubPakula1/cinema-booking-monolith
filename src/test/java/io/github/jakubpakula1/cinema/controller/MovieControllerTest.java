package io.github.jakubpakula1.cinema.controller;

import io.github.jakubpakula1.cinema.controller.view.MovieViewController;
import io.github.jakubpakula1.cinema.enums.MovieGenre;
import io.github.jakubpakula1.cinema.model.Movie;
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
                .andExpect(view().name("movies/movie-list"))
                .andExpect(model().attribute("moviePage", movies));

        verify(movieService).getAllMoviesProjected(0,12);
    }
    @Test
    @DisplayName("Should show movie details page for valid movie ID")
    @WithMockUser(roles = "USER")
    void shouldShowMovieDetailsForValidMovieId() throws Exception {
        // Given
        Long movieId = 1L;
        Movie movie = Movie.builder()
                .id(movieId)
                .title("Inception")
                .description("A thief who steals corporate secrets")
                .genre(MovieGenre.SCI_FI)
                .durationInMinutes(148)
                .director("Christopher Nolan")
                .cast("Leonardo DiCaprio, Ellen Page")
                .releaseYear(2010)
                .productionCountry("USA")
                .ageRestriction(12)
                .posterFileName("inception-poster.jpg")
                .backdropFileName("inception-backdrop.jpg")
                .trailerYoutubeUrl("https://youtube.com/watch?v=YoHD_XwrzKE")
                .build();

        when(movieService.getMovieById(movieId)).thenReturn(movie);

        // When & Then
        mockMvc.perform(get("/movies/{movieId}", movieId))
                .andExpect(status().isOk())
                .andExpect(view().name("movies/movie-details"))
                .andExpect(model().attributeExists("movie"))
                .andExpect(model().attribute("movie", movie));

        verify(movieService, times(1)).getMovieById(movieId);
    }

    @Test
    @DisplayName("Should pass correct movie ID to service")
    @WithMockUser(roles = "USER")
    void shouldPassCorrectMovieIdToService() throws Exception {
        // Given
        Long movieId = 5L;
        Movie movie = Movie.builder()
                .id(movieId)
                .title("Test Movie")
                .build();

        when(movieService.getMovieById(movieId)).thenReturn(movie);

        // When & Then
        mockMvc.perform(get("/movies/{movieId}", movieId))
                .andExpect(status().isOk());

        verify(movieService, times(1)).getMovieById(5L);
        verifyNoMoreInteractions(movieService);
    }

    @Test
    @DisplayName("Should handle movie with all details populated")
    @WithMockUser(roles = "USER")
    void shouldHandleMovieWithAllDetailsPopulated() throws Exception {
        // Given
        Long movieId = 1L;
        Movie movie = Movie.builder()
                .id(movieId)
                .title("The Dark Knight")
                .description("When the menace known as the Joker wreaks havoc")
                .genre(MovieGenre.ACTION)
                .durationInMinutes(152)
                .director("Christopher Nolan")
                .cast("Christian Bale, Heath Ledger, Aaron Eckhart")
                .releaseYear(2008)
                .productionCountry("USA")
                .ageRestriction(12)
                .posterFileName("dark-knight-poster.jpg")
                .backdropFileName("dark-knight-backdrop.jpg")
                .trailerYoutubeUrl("https://youtube.com/watch?v=EXeTwQWrcwY")
                .galleryImageNames(List.of("image1.jpg", "image2.jpg"))
                .build();

        when(movieService.getMovieById(movieId)).thenReturn(movie);

        // When & Then
        mockMvc.perform(get("/movies/{movieId}", movieId))
                .andExpect(status().isOk())
                .andExpect(view().name("movies/movie-details"))
                .andExpect(model().attributeExists("movie"));

        verify(movieService, times(1)).getMovieById(movieId);
    }

    @Test
    @DisplayName("Should handle movie with minimal details")
    @WithMockUser(roles = "USER")
    void shouldHandleMovieWithMinimalDetails() throws Exception {
        // Given
        Long movieId = 1L;
        Movie movie = Movie.builder()
                .id(movieId)
                .title("Movie Title")
                .build();

        when(movieService.getMovieById(movieId)).thenReturn(movie);

        // When & Then
        mockMvc.perform(get("/movies/{movieId}", movieId))
                .andExpect(status().isOk())
                .andExpect(view().name("movies/movie-details"))
                .andExpect(model().attributeExists("movie"));

        verify(movieService, times(1)).getMovieById(movieId);
    }

    @Test
    @DisplayName("Should redirect to login when accessing movie details without authentication")
    void shouldRedirectToLoginWhenAccessingMovieDetailsUnauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/movies/{movieId}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(movieService);
    }
}