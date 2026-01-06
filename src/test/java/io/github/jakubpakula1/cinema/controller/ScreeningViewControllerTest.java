package io.github.jakubpakula1.cinema.controller;

import io.github.jakubpakula1.cinema.controller.view.ScreeningViewController;
import io.github.jakubpakula1.cinema.dto.booking.BookingRequestDTO;
import io.github.jakubpakula1.cinema.dto.booking.BookingSummaryDTO;
import io.github.jakubpakula1.cinema.dto.screening.ScreeningListDTO;
import io.github.jakubpakula1.cinema.dto.seat.SeatStatusDTO;
import io.github.jakubpakula1.cinema.enums.ReservationStatus;
import io.github.jakubpakula1.cinema.exception.EmptyCartException;
import io.github.jakubpakula1.cinema.exception.ReservationExpiredException;
import io.github.jakubpakula1.cinema.model.*;
import io.github.jakubpakula1.cinema.service.BookingService;
import io.github.jakubpakula1.cinema.service.ReservationService;
import io.github.jakubpakula1.cinema.service.ScreeningService;
import io.github.jakubpakula1.cinema.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScreeningViewController.class)
public class ScreeningViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScreeningService screeningService;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private UserService userService;
    private User testUser;
    private Movie testMovie;
    private Screening testScreening;
    private Order testOrder;
    private BookingSummaryDTO testBookingSummary;

    @BeforeEach
    void setUp() {
        // Initialize test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@test.com");
        testUser.setPassword("password123");
        testUser.setRole("ROLE_USER");

        // Initialize test movie
        testMovie = new Movie();
        testMovie.setId(1L);
        testMovie.setTitle("Matrix");
        testMovie.setDescription("A sci-fi thriller");
        testMovie.setDurationInMinutes(136);
        testMovie.setReleaseYear(1999);

        // Initialize test screening
        Room testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setName("Room 1");
        testRoom.setSeats(List.of(new Seat()));

        testScreening = new Screening();
        testScreening.setId(1L);
        testScreening.setMovie(testMovie);
        testScreening.setStartTime(LocalDateTime.now().plusDays(1));
        testScreening.setRoom(testRoom);
        // Initialize test order
        Ticket ticket1 = new Ticket();
        ticket1.setId(1L);
        ticket1.setPrice(BigDecimal.valueOf(50.0));
        ticket1.setTicketType(new TicketType(1L, "Normal", BigDecimal.valueOf(50.0)));
        ticket1.setScreening(testScreening);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setTotalCost(BigDecimal.valueOf(100.0));
        testOrder.setStatus(ReservationStatus.PAID);
        testOrder.setUser(testUser);
        testOrder.getTickets().add(ticket1);

        // Initialize test booking summary
        testBookingSummary = BookingSummaryDTO.builder()
                .reservations(List.of())
                .screening(testScreening)
                .movie(testMovie)
                .ticketTypes(List.of())
                .user(testUser)
                .expirationTime(LocalDateTime.now().plusMinutes(15))
                .build();
    }
    @Test
    @DisplayName("Should show all screenings list")
    @WithMockUser(roles = "USER")
    void shouldShowAllScreenings() throws Exception {
        // given
        ScreeningListDTO screening1 = new ScreeningListDTO();
        ScreeningListDTO screening2 = new ScreeningListDTO();
        List<ScreeningListDTO> screenings = List.of(screening1, screening2);

        when(screeningService.getAllScreeningsForList()).thenReturn(screenings);

        // when & then
        mockMvc.perform(get("/screenings"))
                .andExpect(status().isOk())
                .andExpect(view().name("screening/admin/list"))
                .andExpect(model().attributeExists("screenings"))
                .andExpect(model().attribute("screenings", screenings));
    }

    @Test
    @DisplayName("Should show seat view for screening booking")
    @WithMockUser(roles = "USER")
    void shouldShowSeatView() throws Exception {
        // given
        Long screeningId = 1L;
        Movie movie = new Movie();
        movie.setTitle("Matrix");

        Screening screening = new Screening();
        screening.setId(screeningId);
        screening.setMovie(movie);
        screening.setStartTime(LocalDateTime.now().plusDays(1));

        List<SeatStatusDTO> seatsStatus = List.of(
                new SeatStatusDTO(1L, 1, 2, null, true, false, null),
                new SeatStatusDTO(2L, 1, 2,null, true, false, null)
        );

        when(screeningService.getScreeningEntityById(screeningId)).thenReturn(screening);
        when(screeningService.getSeatsWithStatus(screeningId)).thenReturn(seatsStatus);

        // when & then
        mockMvc.perform(get("/screenings/booking/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("screening/seat-view"))
                .andExpect(model().attributeExists("seats", "movie", "screeningTime", "screeningId"))
                .andExpect(model().attribute("movie", movie))
                .andExpect(model().attribute("screeningId", screeningId));
    }

    @Test
    @DisplayName("Should show booking summary when cart is not empty")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void shouldShowBookingSummary() throws Exception {
        // given


        when(reservationService.prepareSummary("user@test.com")).thenReturn(testBookingSummary);

        // when & then
        mockMvc.perform(get("/screenings/booking/summary"))
                .andExpect(status().isOk())
                .andExpect(view().name("screening/booking-summary"))
                .andExpect(model().attribute("summary", testBookingSummary));
    }

    @Test
    @DisplayName("Should redirect to repertoire when booking summary cart is empty")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void shouldRedirectWhenCartEmpty() throws Exception {
        // given
        when(reservationService.prepareSummary("user@test.com"))
                .thenThrow(new EmptyCartException("Cart is empty"));

        // when & then
        mockMvc.perform(get("/screenings/booking/summary"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/repertoire"));
    }

    @Test
    @DisplayName("Should successfully process booking and show success page")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void shouldProcessBookingSuccessfully() throws Exception {
        // given
        when(userService.getUserByEmail("user@test.com")).thenReturn(testUser);
        when(bookingService.finalizeOrder(any(BookingRequestDTO.class), any(User.class)))
                .thenReturn(1L);

        // when & then
        mockMvc.perform(post("/screenings/booking/process")
                .with(csrf())
                .param("seatIds", "1", "2")
                .param("screeningId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/screenings/booking/success/1"));
    }

    @Test
    @DisplayName("Should handle reservation expired exception during booking")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void shouldHandleReservationExpired() throws Exception {
        // given
        when(userService.getUserByEmail("user@test.com")).thenReturn(testUser);
        when(bookingService.finalizeOrder(any(BookingRequestDTO.class), any(User.class)))
                .thenThrow(new ReservationExpiredException("Reservation expired"));

        // when & then
        mockMvc.perform(post("/screenings/booking/process")
                .with(csrf())
                .param("seatIds", "1")
                .param("screeningId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/repertoire"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("Should show booking success page")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void shouldShowSuccessPage() throws Exception {
        // given
        when(bookingService.getOrderSummary(1L, "user@test.com")).thenReturn(testOrder);

        // when & then
        mockMvc.perform(get("/screenings/booking/success/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("screening/booking-success"))
                .andExpect(model().attribute("order", testOrder));
    }
}