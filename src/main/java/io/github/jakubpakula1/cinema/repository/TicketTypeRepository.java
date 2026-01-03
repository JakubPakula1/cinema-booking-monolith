package io.github.jakubpakula1.cinema.repository;

import io.github.jakubpakula1.cinema.model.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
}