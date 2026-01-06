package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.model.Order;
import io.github.jakubpakula1.cinema.model.Ticket;
import io.github.jakubpakula1.cinema.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

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
    void testGetTicketsByOrderId_Success() {
        Long orderId = 1L;
        Ticket ticket1 = new Ticket();
        ticket1.setId(1L);
        ticket1.setOrder(testOrder);

        Ticket ticket2 = new Ticket();
        ticket2.setId(2L);
        ticket2.setOrder(testOrder);

        List<Ticket> expectedTickets = List.of(ticket1, ticket2);
        when(ticketRepository.findAllByOrderId(orderId)).thenReturn(expectedTickets);

        List<Ticket> result = ticketService.getTicketsByOrderId(orderId);

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
        when(ticketRepository.findAllByOrderId(orderId)).thenReturn(List.of());

        List<Ticket> result = ticketService.getTicketsByOrderId(orderId);

        assertThat(result)
                .isNotNull()
                .isEmpty();

        verify(ticketRepository).findAllByOrderId(orderId);
    }
}