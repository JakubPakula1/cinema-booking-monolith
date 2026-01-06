package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.exception.ResourceNotFoundException;
import io.github.jakubpakula1.cinema.model.Order;
import io.github.jakubpakula1.cinema.model.Ticket;
import io.github.jakubpakula1.cinema.model.User;
import io.github.jakubpakula1.cinema.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.AccessDeniedException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TicketService ticketService;

    private Order testOrder;
    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
    }

    @Test
    @DisplayName("Should retrieve all tickets for a given order ID")
    void testGetTicketsByOrderId_Success() throws AccessDeniedException {
        Long orderId = 1L;
        String email = "user@test.com";

        Ticket ticket1 = new Ticket();
        ticket1.setId(1L);
        ticket1.setOrder(testOrder);

        Ticket ticket2 = new Ticket();
        ticket2.setId(2L);
        ticket2.setOrder(testOrder);

        List<Ticket> expectedTickets = List.of(ticket1, ticket2);
        when(ticketRepository.findAllByOrderId(orderId)).thenReturn(expectedTickets);
        User testUser = new User();
        testUser.setId(1L);
        testOrder.setUser(testUser);

        when(userService.getUserByEmail(email)).thenReturn(testUser);

        List<Ticket> result = ticketService.getTicketsByOrderId(orderId, email);

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .extracting("id")
                .containsExactly(1L, 2L);

        verify(ticketRepository).findAllByOrderId(orderId);
    }

    @Test
    @DisplayName("Should return empty list when no tickets exist for order ID")
    void testGetTicketsByOrderId_EmptyList() {
        Long orderId = 999L;
        String email = "user@test.com";
        when(ticketRepository.findAllByOrderId(orderId)).thenReturn(List.of());

        assertThatThrownBy(() -> ticketService.getTicketsByOrderId(orderId, email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found");

        verify(ticketRepository).findAllByOrderId(orderId);
    }
    @Test
    @DisplayName("Should throw AccessDeniedException when different user tries to access tickets")
    void testGetTicketsByOrderId_AccessDenied() {
        Long orderId = 1L;
        String email = "other@test.com";

        Ticket ticket1 = new Ticket();
        ticket1.setId(1L);
        ticket1.setOrder(testOrder);

        User ticketOwner = new User();
        ticketOwner.setId(1L);
        testOrder.setUser(ticketOwner);

        User otherUser = new User();
        otherUser.setId(2L);

        when(ticketRepository.findAllByOrderId(orderId)).thenReturn(List.of(ticket1));
        when(userService.getUserByEmail(email)).thenReturn(otherUser);

        assertThatThrownBy(() -> ticketService.getTicketsByOrderId(orderId, email))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You don't have permission to access this order");

        verify(ticketRepository).findAllByOrderId(orderId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user is null")
    void testGetTicketsByOrderId_UserNotFound() {
        Long orderId = 1L;
        String email = "nonexistent@test.com";

        Ticket ticket1 = new Ticket();
        ticket1.setId(1L);
        ticket1.setOrder(testOrder);

        when(ticketRepository.findAllByOrderId(orderId)).thenReturn(List.of(ticket1));

        assertThatThrownBy(() -> ticketService.getTicketsByOrderId(orderId, email))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(ticketRepository).findAllByOrderId(orderId);
    }

}