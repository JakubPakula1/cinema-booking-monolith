package io.github.jakubpakula1.cinema.controller;

import io.github.jakubpakula1.cinema.controller.api.TicketRestController;
import io.github.jakubpakula1.cinema.security.SecurityConfig;
import io.github.jakubpakula1.cinema.service.PdfService;
import io.github.jakubpakula1.cinema.service.TicketService;
import io.github.jakubpakula1.cinema.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketRestController.class)
@Import(SecurityConfig.class)
class TicketApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private PdfService pdfService;

    @MockitoBean
    private UserService userService;

    // --- Pobranie PDF (Sukces) ---
    @Test
    @DisplayName("Should return PDF when user is authorized")
    @WithMockUser(username = "jan@test.pl")
    void shouldReturnPdfForOwner() throws Exception {
        byte[] fakePdfContent = new byte[]{1, 2, 3};

        when(pdfService.generateTicketPdf(anyList())).thenReturn(fakePdfContent);

        mockMvc.perform(get("/api/v1/tickets/1/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(fakePdfContent));
    }

    // --- Pobranie PDF (Brak logowania - 401/302) ---
    @Test
    @DisplayName("Should redirect to login when user is anonymous")
    void shouldRedirectToLoginWhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/tickets/1/pdf"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}