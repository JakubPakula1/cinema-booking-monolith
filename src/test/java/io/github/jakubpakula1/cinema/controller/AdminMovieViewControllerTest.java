package io.github.jakubpakula1.cinema.controller;

import io.github.jakubpakula1.cinema.controller.view.admin.AdminMovieViewController;
import io.github.jakubpakula1.cinema.dto.MovieFormDTO;

import io.github.jakubpakula1.cinema.enums.MovieGenre;
import io.github.jakubpakula1.cinema.model.Movie;
import io.github.jakubpakula1.cinema.repository.projection.MovieListView;
import io.github.jakubpakula1.cinema.security.SecurityConfig;
import io.github.jakubpakula1.cinema.service.MovieService;
import io.github.jakubpakula1.cinema.service.RoomService;
import io.github.jakubpakula1.cinema.service.ScreeningService;
import io.github.jakubpakula1.cinema.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminMovieViewController.class)
@Import(SecurityConfig.class)
public class AdminMovieViewControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ScreeningService screeningService;

    @MockitoBean
    private RoomService roomService;

    @Test
    @DisplayName("Should forbid adding movie for normal user")
    @WithMockUser(roles = "USER")
    void shouldForbidAddMovieForUser() throws Exception {

        mockMvc.perform(post("/admin/movies/add")
                        .with(csrf())
                        .param("title", "Matrix"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow admin to add movie")
    @WithMockUser(roles = "ADMIN")
    void shouldAddMovieAsAdmin() throws Exception {

        mockMvc.perform(multipart("/admin/movies/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .file("posterImageFile", "test image content".getBytes())
                        .file("backdropImageFile", "test image content".getBytes())
                        .param("title", "Matrix")
                        .param("description", "A sci-fi thriller")
                        .param("genre", "ACTION")
                        .param("durationInMinutes", "120")
                        .param("director", "The Wachowskis")
                        .param("cast", "Keanu Reeves, Laurence Fishburne")
                        .param("releaseYear", "1999")
                        .param("productionCountry", "USA")
                        .param("ageRestriction", "12")
                        .param("trailerYoutubeUrl", "https://youtube.com/watch?v=example"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/movies"));

        verify(movieService).addMovie(any(MovieFormDTO.class));
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should display all movies list")
    void shouldDisplayAllMoviesList() throws Exception {
        // Given
        List<MovieListView> movies = List.of(
                createMovieListView(1L, "Inception", "Christopher Nolan"),
                createMovieListView(2L, "The Dark Knight", "Christopher Nolan")
        );
        Page<MovieListView> page = new PageImpl<>(movies);

        when(movieService.getAllMoviesProjected(0, 100)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/admin/movies"))
                .andExpect(status().isOk())
                .andExpect(view().name("movies/admin/movie-list"))
                .andExpect(model().attributeExists("movies"))
                .andExpect(model().attribute("movies", hasSize(2)));

        verify(movieService, times(1)).getAllMoviesProjected(0, 100);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should show add movie form")
    void shouldShowAddMovieForm() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/movies/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("movies/admin/add-movie"))
                .andExpect(model().attributeExists("movieFormDTO"));

        verifyNoInteractions(movieService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should add movie successfully with valid data")
    void shouldAddMovieSuccessfullyWithValidData() throws Exception {
        // Given
        when(movieService.addMovie(any(MovieFormDTO.class))).thenReturn(new Movie());

        // When & Then
        mockMvc.perform(multipart("/admin/movies/add")
                        .with(csrf())
                        .file("posterImageFile", "poster content".getBytes())
                        .file("backdropImageFile", "backdrop content".getBytes())
                        .param("title", "Inception")
                        .param("description", "A mind-bending thriller")
                        .param("genre", "THRILLER")
                        .param("durationInMinutes", "148")
                        .param("director", "Christopher Nolan")
                        .param("cast", "Leonardo DiCaprio, Marion Cotillard")
                        .param("releaseYear", "2010")
                        .param("productionCountry", "USA")
                        .param("ageRestriction", "12")
                        .param("trailerYoutubeUrl", "https://youtube.com/watch?v=example"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/movies"));

        verify(movieService, times(1)).addMovie(any(MovieFormDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should reject add movie when poster image is missing")
    void shouldRejectAddMovieWhenPosterImageMissing() throws Exception {
        // When & Then
        mockMvc.perform(multipart("/admin/movies/add")
                        .with(csrf())
                        .file("backdropImageFile", "backdrop content".getBytes())
                        .param("title", "Inception")
                        .param("description", "A mind-bending thriller")
                        .param("genre", "THRILLER")
                        .param("durationInMinutes", "148")
                        .param("director", "Christopher Nolan")
                        .param("cast", "Leonardo DiCaprio, Marion Cotillard")
                        .param("releaseYear", "2010")
                        .param("productionCountry", "USA")
                        .param("ageRestriction", "12"))
                .andExpect(status().isOk())
                .andExpect(view().name("movies/admin/add-movie"));

        verifyNoInteractions(movieService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should reject add movie when backdrop image is missing")
    void shouldRejectAddMovieWhenBackdropImageMissing() throws Exception {
        // When & Then
        mockMvc.perform(multipart("/admin/movies/add")
                        .with(csrf())
                        .file("posterImageFile", "poster content".getBytes())
                        .param("title", "Inception")
                        .param("description", "A mind-bending thriller")
                        .param("genre", "THRILLER")
                        .param("durationInMinutes", "148")
                        .param("director", "Christopher Nolan")
                        .param("cast", "Leonardo DiCaprio, Marion Cotillard")
                        .param("releaseYear", "2010")
                        .param("productionCountry", "USA")
                        .param("ageRestriction", "12"))
                .andExpect(status().isOk())
                .andExpect(view().name("movies/admin/add-movie"));

        verifyNoInteractions(movieService);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should reject add movie when validation fails")
    void shouldRejectAddMovieWhenValidationFails() throws Exception {
        // When & Then
        mockMvc.perform(multipart("/admin/movies/add")
                .with(csrf())
                .file("posterImageFile", "poster content".getBytes())
                .file("backdropImageFile", "backdrop content".getBytes())
                .param("title", "")
                .param("description", "")
                .param("genre", "THRILLER")
                .param("durationInMinutes", "0")
                .param("director", "")
                .param("cast", "")
                .param("releaseYear", "1000")
                .param("productionCountry", "")
                .param("ageRestriction", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("movies/admin/add-movie"));

        verifyNoInteractions(movieService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should show edit movie form")
    void shouldShowEditMovieForm() throws Exception {
        // Given
        Long movieId = 1L;
        MovieFormDTO movieFormDTO = createMovieFormDTO(movieId, "Inception");

        when(movieService.getMovieFormDTOById(movieId)).thenReturn(movieFormDTO);

        // When & Then
        mockMvc.perform(get("/admin/movies/edit/{id}", movieId))
                .andExpect(status().isOk())
                .andExpect(view().name("movies/admin/add-movie"))
                .andExpect(model().attributeExists("movieFormDTO"))
                .andExpect(model().attribute("movieFormDTO", movieFormDTO));

        verify(movieService, times(1)).getMovieFormDTOById(movieId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update movie successfully")
    void shouldUpdateMovieSuccessfully() throws Exception {
        // Given
        Long movieId = 1L;
        when(movieService.updateMovie(eq(movieId), any(MovieFormDTO.class))).thenReturn(new Movie());

        // When & Then
        mockMvc.perform(put("/admin/movies/edit/{id}", movieId)
                        .with(csrf())
                        .param("title", "Inception Updated")
                        .param("description", "An updated description")
                        .param("genre", "THRILLER")
                        .param("durationInMinutes", "150")
                        .param("director", "Christopher Nolan")
                        .param("cast", "Leonardo DiCaprio, Marion Cotillard")
                        .param("releaseYear", "2010")
                        .param("productionCountry", "USA")
                        .param("ageRestriction", "12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/movies"));

        verify(movieService, times(1)).updateMovie(eq(movieId), any(MovieFormDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should reject update movie when validation fails")
    void shouldRejectUpdateMovieWhenValidationFails() throws Exception {
        // Given
        Long movieId = 1L;

        // When & Then
        mockMvc.perform(put("/admin/movies/edit/{id}", movieId)
                .with(csrf())
                .param("title", "")
                .param("description", "")
                .param("genre", "THRILLER")
                .param("durationInMinutes", "0")
                .param("director", "")
                .param("cast", "")
                .param("releaseYear", "1000")
                .param("productionCountry", "")
                .param("ageRestriction", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("movies/admin/add-movie"));

        verifyNoInteractions(movieService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should delete movie successfully")
    void shouldDeleteMovieSuccessfully() throws Exception {
        // Given
        Long movieId = 1L;
        when(movieService.deleteMovie(movieId)).thenReturn(new Movie());

        // When & Then
        mockMvc.perform(post("/admin/movies/delete/{id}", movieId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/movies"));

        verify(movieService, times(1)).deleteMovie(movieId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should deny access to movies list for non-admin user")
    void shouldDenyAccessToMoviesListForNonAdminUser() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/movies"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(movieService);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should deny access to add movie form for non-admin user")
    void shouldDenyAccessToAddMovieFormForNonAdminUser() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/movies/add"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(movieService);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should deny access to edit movie form for non-admin user")
    void shouldDenyAccessToEditMovieFormForNonAdminUser() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/movies/edit/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(movieService);
    }

    @Test
    @DisplayName("Should redirect to login when accessing movies endpoints unauthenticated")
    void shouldRedirectToLoginWhenAccessingMoviesEndpointsUnauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/movies"))
                .andExpect(status().is3xxRedirection());

        verifyNoInteractions(movieService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should display empty movies list when no movies exist")
    void shouldDisplayEmptyMoviesListWhenNoMoviesExist() throws Exception {
        // Given
        Page<MovieListView> emptyPage = new PageImpl<>(List.of());
        when(movieService.getAllMoviesProjected(0, 100)).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/admin/movies"))
                .andExpect(status().isOk())
                .andExpect(view().name("movies/admin/movie-list"))
                .andExpect(model().attribute("movies", hasSize(0)));

        verify(movieService, times(1)).getAllMoviesProjected(0, 100);
    }

    // Helper methods
    private MovieListView createMovieListView(Long id, String title, String director) {
        MovieListView mockView = mock(MovieListView.class);
        when(mockView.getId()).thenReturn(id);
        when(mockView.getTitle()).thenReturn(title);
        when(mockView.getDirector()).thenReturn(director);
        when(mockView.getReleaseYear()).thenReturn(2024);
        when(mockView.getDurationInMinutes()).thenReturn(120);
        when(mockView.getPosterFileName()).thenReturn("poster.jpg");
        when(mockView.getGenre()).thenReturn("THRILLER");
        return mockView;
    }

    private MovieFormDTO createMovieFormDTO(Long id, String title) {
        MovieFormDTO dto = new MovieFormDTO();
        dto.setId(id);
        dto.setTitle(title);
        dto.setDescription("Test description");
        dto.setGenre(MovieGenre.THRILLER);
        dto.setDurationInMinutes(120);
        dto.setDirector("Test Director");
        dto.setCast("Test Actor");
        dto.setReleaseYear(2024);
        dto.setProductionCountry("USA");
        dto.setAgeRestriction(12);
        return dto;
    }
}
