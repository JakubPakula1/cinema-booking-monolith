package io.github.jakubpakula1.cinema.repository;

import io.github.jakubpakula1.cinema.model.Screening;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
}
