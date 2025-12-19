package io.github.jakubpakula1.cinema.repository;

import io.github.jakubpakula1.cinema.model.TemporaryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemporaryReservationRepository extends JpaRepository<TemporaryReservation, Long> {
}
