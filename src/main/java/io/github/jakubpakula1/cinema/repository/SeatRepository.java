package io.github.jakubpakula1.cinema.repository;

import io.github.jakubpakula1.cinema.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
}
