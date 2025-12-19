package io.github.jakubpakula1.cinema.repository;

import io.github.jakubpakula1.cinema.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long>{
}
