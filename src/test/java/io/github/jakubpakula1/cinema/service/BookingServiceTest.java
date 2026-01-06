package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.dto.booking.BookingRequestDTO;
import io.github.jakubpakula1.cinema.dto.booking.TicketSelectionDTO;
import io.github.jakubpakula1.cinema.enums.ReservationStatus;
import io.github.jakubpakula1.cinema.exception.ResourceNotFoundException;
import io.github.jakubpakula1.cinema.model.*;
import io.github.jakubpakula1.cinema.repository.OrderRepository;
import io.github.jakubpakula1.cinema.repository.TemporaryReservationRepository;
import io.github.jakubpakula1.cinema.repository.TicketTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Tests")
class BookingServiceTest {

    @Mock
    private TemporaryReservationRepository temporaryReservationRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PdfService pdfService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private BookingService bookingService;

    private User testUser;
    private Order testOrder;
    private Screening testScreening;
    private Seat testSeat;
    private TicketType testTicketType;
    private TemporaryReservation testReservation;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        // Setup test screening
        testScreening = new Screening();
        testScreening.setId(1L);

        // Setup test seat
        testSeat = new Seat();
        testSeat.setId(1L);

        // Setup test ticket type
        testTicketType = new TicketType();
        testTicketType.setId(1L);
        testTicketType.setPrice(new BigDecimal("25.00"));

        // Setup test order
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setStatus(ReservationStatus.PAID);

        // Setup test reservation
        testReservation = new TemporaryReservation();
        testReservation.setId(1L);
        testReservation.setUser(testUser);
        testReservation.setSeat(testSeat);
        testReservation.setScreening(testScreening);
        testReservation.setExpiresAt(LocalDateTime.now().plusHours(1));
    }

    @Test
    @DisplayName("Should get order summary successfully")
    void testGetOrderSummary_Success() throws AccessDeniedException {
        // given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // when
        Order result = bookingService.getOrderSummary(1L, "test@example.com");

        // then
        assertThat(result)
                .isNotNull()
                .extracting("id", "status")
                .containsExactly(1L, ReservationStatus.PAID);
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order not found")
    void testGetOrderSummary_OrderNotFound() {
        // given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookingService.getOrderSummary(999L, "test@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Nie znaleziono zamówienia");
        verify(orderRepository).findById(999L);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when user email doesn't match")
    void testGetOrderSummary_AccessDenied() {
        // given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // when & then
        assertThatThrownBy(() -> bookingService.getOrderSummary(1L, "wrong@example.com"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Nie masz uprawnień");
        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("Should finalize order successfully with single ticket")
    void testFinalizeOrder_Success() {
        // given
        BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
        TicketSelectionDTO ticketSelection = new TicketSelectionDTO();
        ticketSelection.setSeatId(1L);
        ticketSelection.setTypeId(1L);
        bookingRequestDTO.setTickets(List.of(ticketSelection));

        when(temporaryReservationRepository.findByUserAndSeatIdInAndExpiresAtAfter(eq(testUser),
                eq(List.of(1L)),
                any(LocalDateTime.class)))
                .thenReturn(List.of(testReservation));
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(testTicketType));
        when(pdfService.generateTicketPdf(any())).thenReturn(new byte[]{1, 2, 3});
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        // when
        Long orderId = bookingService.finalizeOrder(bookingRequestDTO, testUser);

        // then
        assertThat(orderId).isEqualTo(1L);
        verify(temporaryReservationRepository).findByUserAndSeatIdInAndExpiresAtAfter(eq(testUser), eq(List.of(1L)), any(LocalDateTime.class));
        verify(ticketTypeRepository).findById(1L);
        verify(pdfService).generateTicketPdf(any());
        verify(orderRepository).save(any(Order.class));
        verify(emailService).sendEmailWithAttachment(eq("test@example.com"), anyString(), anyString(), any(byte[].class), anyString());
        verify(temporaryReservationRepository).deleteAll(List.of(testReservation));
    }

    @Test
    @DisplayName("Should finalize order with multiple tickets")
    void testFinalizeOrder_MultipleTickets() {
        // given
        Seat seat2 = new Seat();
        seat2.setId(2L);

        TemporaryReservation reservation2 = new TemporaryReservation();
        reservation2.setId(2L);
        reservation2.setUser(testUser);
        reservation2.setSeat(seat2);
        reservation2.setScreening(testScreening);

        BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
        TicketSelectionDTO ticket1 = new TicketSelectionDTO();
        ticket1.setSeatId(1L);
        ticket1.setTypeId(1L);
        TicketSelectionDTO ticket2 = new TicketSelectionDTO();
        ticket2.setSeatId(2L);
        ticket2.setTypeId(1L);
        bookingRequestDTO.setTickets(List.of(ticket1, ticket2));

        when(temporaryReservationRepository.findByUserAndSeatIdInAndExpiresAtAfter(eq(testUser),
                eq(List.of(1L, 2L)),
                any(LocalDateTime.class)))
                .thenReturn(List.of(testReservation, reservation2));
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(testTicketType));
        when(pdfService.generateTicketPdf(any())).thenReturn(new byte[]{});
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(2L);
            return order;
        });

        // when
        Long orderId = bookingService.finalizeOrder(bookingRequestDTO, testUser);

        // then
        assertThat(orderId).isEqualTo(2L);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder)
                .extracting("totalCost", "status")
                .containsExactly(new BigDecimal("50.00"), ReservationStatus.PAID);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when reservation not found")
    void testFinalizeOrder_ReservationNotFound() {
        // given
        BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
        TicketSelectionDTO ticketSelection = new TicketSelectionDTO();
        ticketSelection.setSeatId(1L);
        bookingRequestDTO.setTickets(List.of(ticketSelection));

        when(temporaryReservationRepository.findByUserAndSeatIdInAndExpiresAtAfter(eq(testUser),
                eq(List.of(1L)),
                any(LocalDateTime.class)))
                .thenReturn(List.of());

        // when & then
        assertThatThrownBy(() -> bookingService.finalizeOrder(bookingRequestDTO, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Some seats are not reserved");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when ticket type not found")
    void testFinalizeOrder_TicketTypeNotFound() {
        // given
        BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
        TicketSelectionDTO ticketSelection = new TicketSelectionDTO();
        ticketSelection.setSeatId(1L);
        ticketSelection.setTypeId(999L);
        bookingRequestDTO.setTickets(List.of(ticketSelection));

        when(temporaryReservationRepository.findByUserAndSeatIdInAndExpiresAtAfter(
                eq(testUser),
                eq(List.of(1L)),
                any(LocalDateTime.class)))
                .thenReturn(List.of(testReservation));
        when(ticketTypeRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookingService.finalizeOrder(bookingRequestDTO, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ticket type not found");
    }


    @Test
    @DisplayName("Should get all orders for user")
    void testGetAllOrdersForUser() {
        // given
        Order order2 = new Order();
        order2.setId(2L);
        List<Order> orders = List.of(testOrder, order2);
        when(orderRepository.findAllByUserIdOrderByCreatedAtDesc(1L)).thenReturn(orders);

        // when
        List<Order> result = bookingService.getAllOrdersForUser(1L);

        // then
        assertThat(result)
                .hasSize(2)
                .extracting("id")
                .containsExactly(1L, 2L);
        verify(orderRepository).findAllByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("Should return empty list when user has no orders")
    void testGetAllOrdersForUser_EmptyList() {
        // given
        when(orderRepository.findAllByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        // when
        List<Order> result = bookingService.getAllOrdersForUser(1L);

        // then
        assertThat(result).isEmpty();
        verify(orderRepository).findAllByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("Should send email with PDF after successful order finalization")
    void testFinalizeOrder_SendsEmail() {
        // given
        BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
        TicketSelectionDTO ticketSelection = new TicketSelectionDTO();
        ticketSelection.setSeatId(1L);
        ticketSelection.setTypeId(1L);
        bookingRequestDTO.setTickets(List.of(ticketSelection));

        byte[] pdfBytes = {1, 2, 3, 4, 5};
        when(temporaryReservationRepository.findByUserAndSeatIdInAndExpiresAtAfter(eq(testUser),
                eq(List.of(1L)),
                any(LocalDateTime.class)))
                .thenReturn(List.of(testReservation));
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(testTicketType));
        when(pdfService.generateTicketPdf(any())).thenReturn(pdfBytes);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(5L);
            return order;
        });

        // when
        bookingService.finalizeOrder(bookingRequestDTO, testUser);

        // then
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<byte[]> pdfCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailService).sendEmailWithAttachment(
                emailCaptor.capture(),
                anyString(),
                anyString(),
                pdfCaptor.capture(),
                fileNameCaptor.capture()
        );

        assertThat(emailCaptor.getValue()).isEqualTo("test@example.com");
        assertThat(pdfCaptor.getValue()).isEqualTo(pdfBytes);
        assertThat(fileNameCaptor.getValue()).contains("tickets_order_5.pdf");
    }
}