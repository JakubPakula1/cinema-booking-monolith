package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.exception.ResourceNotFoundException;
import io.github.jakubpakula1.cinema.model.Ticket;
import io.github.jakubpakula1.cinema.model.User;
import io.github.jakubpakula1.cinema.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserService userService;
    public List<Ticket> getTicketsByOrderId(Long orderId, String email) throws AccessDeniedException {
        List<Ticket> tickets = ticketRepository.findAllByOrderId(orderId);

        if (tickets.isEmpty()) {
            throw new ResourceNotFoundException("Order not found");
        }
        User user = userService.getUserByEmail(email);

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        if (!tickets.getFirst().getOrder().getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to access this order");
        }
        return tickets;
    }
}
