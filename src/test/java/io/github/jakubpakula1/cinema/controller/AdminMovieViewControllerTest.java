package io.github.jakubpakula1.cinema.controller;

import io.github.jakubpakula1.cinema.controller.view.admin.AdminMovieViewController;
import io.github.jakubpakula1.cinema.dto.MovieFormDTO;
import io.github.jakubpakula1.cinema.security.SecurityConfig;
import io.github.jakubpakula1.cinema.service.MovieService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminMovieViewController.class)
@Import(SecurityConfig.class)
public class AdminMovieViewControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private UserService userService;

    // --- Dodawanie Filmu (Blokada dla zwykłego Usera) ---
    @Test
    @DisplayName("Should forbid adding movie for normal user")
    @WithMockUser(roles = "USER")
    void shouldForbidAddMovieForUser() throws Exception {

        mockMvc.perform(post("/admin/movies/add")
                        .with(csrf())
                        .param("title", "Matrix"))
                .andExpect(status().isForbidden()); // 403 Forbidden
    }

    // --- Dodawanie Filmu (Sukces Admina) ---
    @Test
    @DisplayName("Should allow admin to add movie")
    @WithMockUser(roles = "ADMIN") // Admin
    void shouldAddMovieAsAdmin() throws Exception {

        mockMvc.perform(post("/admin/movies/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
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
}
