package io.github.jakubpakula1.cinema.repository;

import io.github.jakubpakula1.cinema.model.Movie;
import io.github.jakubpakula1.cinema.repository.projection.MovieListView;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>{

    Page<MovieListView> findAllProjectedBy(Pageable pageable);
}
