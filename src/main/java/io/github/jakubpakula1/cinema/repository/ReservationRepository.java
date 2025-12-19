package io.github.jakubpakula1.cinema.repository;

import io.github.jakubpakula1.cinema.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
