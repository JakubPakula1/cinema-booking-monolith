package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.model.Ticket;
import io.github.jakubpakula1.cinema.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;

    public List<Ticket> getTicketsByOrderId(Long orderId) {
        return ticketRepository.findAllByOrderId(orderId);
    }
}
