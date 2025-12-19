package io.github.jakubpakula1.cinema.repository;

import io.github.jakubpakula1.cinema.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
