package io.github.jakubpakula1.cinema.controller;

import io.github.jakubpakula1.cinema.controller.view.AuthViewController;
import io.github.jakubpakula1.cinema.dto.UserDTO;
import io.github.jakubpakula1.cinema.model.Order;
import io.github.jakubpakula1.cinema.model.User;
import io.github.jakubpakula1.cinema.security.SecurityConfig;
import io.github.jakubpakula1.cinema.service.BookingService;
import io.github.jakubpakula1.cinema.service.UserService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthViewController.class)
@Import(SecurityConfig.class)
@DisplayName("Auth View Controller Tests")
public class AuthViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private BookingService bookingService;

    @Test
    @DisplayName("Should show login page")
    public void shouldShowLoginPage() throws Exception {
        // When & Then
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("Should show register page with empty user DTO")
    public void shouldShowRegisterPage() throws Exception {
        // When & Then
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("Should handle user registration successfully")
    public void shouldHandleRegistrationSuccessfully() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password123");

        doNothing().when(userService).registerUser(any(UserDTO.class));

        // When & Then
        mockMvc.perform(post("/register")
                .with(csrf())
                .flashAttr("user", userDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService, times(1)).registerUser(any(UserDTO.class));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should show user profile with orders")
    public void shouldShowUserProfileWithOrders() throws Exception {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        Order order1 = new Order();
        order1.setId(1L);
        order1.setCreatedAt(LocalDateTime.now());

        Order order2 = new Order();
        order2.setId(2L);
        order2.setCreatedAt(LocalDateTime.now());

        List<Order> orders = List.of(order1, order2);

        when(userService.getUserByEmail("user@example.com")).thenReturn(user);
        when(bookingService.getAllOrdersForUser(1L)).thenReturn(orders);

        // When & Then
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attribute("user", user));

        verify(userService, times(1)).getUserByEmail("user@example.com");
        verify(bookingService, times(1)).getAllOrdersForUser(1L);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should show user profile with empty orders list")
    public void shouldShowUserProfileWithNoOrders() throws Exception {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        when(userService.getUserByEmail("user@example.com")).thenReturn(user);
        when(bookingService.getAllOrdersForUser(1L)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("orders", List.of()));

        verify(userService, times(1)).getUserByEmail("user@example.com");
        verify(bookingService, times(1)).getAllOrdersForUser(1L);
    }

    @Test
    @DisplayName("Should redirect to login when accessing profile without authentication")
    public void shouldRedirectToLoginWhenAccessingProfileUnauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login**"));
    }
}