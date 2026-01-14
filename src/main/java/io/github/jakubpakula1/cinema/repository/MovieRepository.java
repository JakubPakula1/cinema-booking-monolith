package io.github.jakubpakula1.cinema.repository;

import io.github.jakubpakula1.cinema.model.Movie;
import io.github.jakubpakula1.cinema.repository.projection.MovieCarouselDTO;

import io.github.jakubpakula1.cinema.repository.projection.MovieListViewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>{

    Page<MovieListViewDTO> findAllProjectedBy(Pageable pageable);


    @Query("SELECT new io.github.jakubpakula1.cinema.repository.projection.MovieCarouselDTO(" +
            "m.id, " +
            "m.title, " +
            "CAST(m.genre AS string), " +
            "m.backdropFileName, " +
            "substring(m.description, 1, 150)) " +
            "FROM Movie m " +
            "ORDER BY m.id DESC")
    List<MovieCarouselDTO> findLatestMoviesForCarousel(Pageable pageable);
}
