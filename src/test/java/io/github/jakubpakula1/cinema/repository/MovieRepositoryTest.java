package io.github.jakubpakula1.cinema.repository;

import io.github.jakubpakula1.cinema.model.Movie;
import io.github.jakubpakula1.cinema.repository.projection.MovieCarouselDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MovieRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should save a movie and generate ID")
    void testSaveMovie() {
        // given
        Movie movie = new Movie();
        movie.setTitle("Inception");
        movie.setDescription("Dream within a dream");
        movie.setDurationInMinutes(148);

        // when
        Movie savedMovie = movieRepository.save(movie);

        // then
        assertThat(savedMovie)
                .isNotNull()
                .extracting("id", "title")
                .containsExactly(savedMovie.getId(), "Inception");
    }

    @Test
    @DisplayName("Should find movie by ID")
    void testFindMovieById() {
        // given
        Movie movie = new Movie();
        movie.setTitle("Avatar");
        Movie persisted = entityManager.persistAndFlush(movie);

        // when
        Optional<Movie> found = movieRepository.findById(persisted.getId());

        // then
        assertThat(found)
                .isPresent()
                .get()
                .extracting("title")
                .isEqualTo("Avatar");
    }

    @Test
    @DisplayName("Should update movie title")
    void testUpdateMovie() {
        // given
        Movie movie = new Movie();
        movie.setTitle("Old Title");
        Movie persisted = entityManager.persistAndFlush(movie);

        // when
        persisted.setTitle("New Title");
        Movie updated = movieRepository.save(persisted);

        // then
        assertThat(updated.getTitle()).isEqualTo("New Title");
        // Sprawdź czy w bazie faktycznie się zmieniło
        Movie fromDb = entityManager.find(Movie.class, persisted.getId());
        assertThat(fromDb)
                .extracting("title")
                .isEqualTo("New Title");

    }

    @Test
    @DisplayName("Should delete movie")
    void testDeleteMovie() {
        // given
        Movie movie = new Movie();
        movie.setTitle("To Delete");
        Movie persisted = entityManager.persistAndFlush(movie);

        // when
        movieRepository.delete(persisted);

        // then
        Optional<Movie> found = movieRepository.findById(persisted.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return MovieCarouselDTO list with limited description")
    void testFindLatestMoviesForCarousel() {
        // given
        Movie m1 = new Movie();
        m1.setTitle("Movie A");
        m1.setDescription("A very long description that should be truncated by the query logic in repository...");
        m1.setPosterFileName("a.jpg");
        entityManager.persist(m1);

        Movie m2 = new Movie();
        m2.setTitle("Movie B");
        m2.setDescription("Short desc");
        entityManager.persist(m2);

        entityManager.flush();

        // when
        List<MovieCarouselDTO> result = movieRepository.findLatestMoviesForCarousel(PageRequest.of(0, 5));

        // then
        assertThat(result)
                .hasSize(2)
                .extracting("title")
                .containsExactly("Movie B", "Movie A");
        assertThat(result.getFirst()).isInstanceOf(MovieCarouselDTO.class);
    }

    @Test
    void testFindByIdWhenNotExists() {
        Optional<Movie> found = movieRepository.findById(999L);
        assertThat(found).isEmpty();
    }
}