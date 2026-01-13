package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.dto.MovieFormDTO;
import io.github.jakubpakula1.cinema.enums.MovieGenre;
import io.github.jakubpakula1.cinema.exception.ResourceNotFoundException;
import io.github.jakubpakula1.cinema.model.Movie;
import io.github.jakubpakula1.cinema.repository.MovieRepository;
import io.github.jakubpakula1.cinema.repository.projection.MovieCarouselDTO;
import io.github.jakubpakula1.cinema.repository.projection.MovieListView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService Unit Tests")
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    private MovieService movieService;

    @TempDir
    private Path tempDir;

    private Movie testMovie;
    private MovieFormDTO movieFormDTO;
    private MultipartFile mockPosterFile;
    private MultipartFile mockBackdropFile;

    @BeforeEach
    void setUp() {
        movieService = new MovieService(movieRepository, tempDir.toString());

        testMovie = Movie.builder()
                .id(1L)
                .title("Test Movie")
                .description("Test Description")
                .durationInMinutes(120)
                .director("Test Director")
                .cast("Test Cast")
                .releaseYear(2024)
                .productionCountry("USA")
                .ageRestriction(13)
                .posterFileName("poster_123.jpg")
                .backdropFileName("backdrop_123.jpg")
                .trailerYoutubeUrl("https://youtube.com/test")
                .galleryImageNames(List.of())
                .build();

        movieFormDTO = new MovieFormDTO();
        movieFormDTO.setTitle("New Movie");
        movieFormDTO.setDescription("New Description");
        movieFormDTO.setDurationInMinutes(150);
        movieFormDTO.setDirector("New Director");
        movieFormDTO.setCast("New Cast");
        movieFormDTO.setReleaseYear(2025);
        movieFormDTO.setProductionCountry("UK");
        movieFormDTO.setAgeRestriction(18);
        movieFormDTO.setTrailerYoutubeUrl("https://youtube.com/new");

        mockPosterFile = mock(MultipartFile.class);
        mockBackdropFile = mock(MultipartFile.class);
    }

    @Test
    @DisplayName("Should get all movies")
    void testGetAllMovies() {
        when(movieRepository.findAll()).thenReturn(List.of(testMovie));
        List<Movie> movies = movieService.getAllMovies();
        assertThat(movies).isNotEmpty().hasSize(1).contains(testMovie);
        verify(movieRepository).findAll();
    }

    @Test
    @DisplayName("Should get all movies projected")
    void testGetAllMoviesProjected() {
        MovieListView mockView = mock(MovieListView.class);
        Page<MovieListView> mockPage = new PageImpl<>(List.of(mockView), PageRequest.of(0, 10), 1);
        when(movieRepository.findAllProjectedBy(any(PageRequest.class))).thenReturn(mockPage);
        Page<MovieListView> result = movieService.getAllMoviesProjected(0, 10);
        assertThat(result).isNotNull().hasSize(1);
        verify(movieRepository).findAllProjectedBy(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should get movie by id")
    void testGetMovieById() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        Movie movie = movieService.getMovieById(1L);
        assertThat(movie).isNotNull().extracting("id", "title").containsExactly(1L, "Test Movie");
        verify(movieRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when movie not found")
    void testGetMovieById_NotFound() {
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> movieService.getMovieById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Movie not found");
        verify(movieRepository).findById(999L);
    }

    @Test
    @DisplayName("Should get latest movies for carousel")
    void testGetLatestMovies() {
        MovieCarouselDTO carouselDTO = new MovieCarouselDTO() {};
        when(movieRepository.findLatestMoviesForCarousel(any(PageRequest.class)))
                .thenReturn(List.of(carouselDTO));
        List<MovieCarouselDTO> result = movieService.getLatestMovies(1);
        assertThat(result).isNotEmpty().hasSize(1);
        verify(movieRepository).findLatestMoviesForCarousel(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should add movie with images")
    void testAddMovie_Success() throws IOException {
        movieFormDTO.setPosterImageFile(mockPosterFile);
        movieFormDTO.setBackdropImageFile(mockBackdropFile);
        movieFormDTO.setGalleryImages(List.of());

        when(mockPosterFile.isEmpty()).thenReturn(false);
        when(mockPosterFile.getOriginalFilename()).thenReturn("poster.jpg");
        when(mockPosterFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));

        when(mockBackdropFile.isEmpty()).thenReturn(false);
        when(mockBackdropFile.getOriginalFilename()).thenReturn("backdrop.jpg");
        when(mockBackdropFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));

        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie movie = invocation.getArgument(0);
            movie.setId(1L);
            return movie;
        });

        Movie result = movieService.addMovie(movieFormDTO);

        assertThat(result).isNotNull().extracting("id", "title", "durationInMinutes")
                .containsExactly(1L, "New Movie", 150);

        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(movieCaptor.capture());
        Movie savedMovie = movieCaptor.getValue();
        assertThat(savedMovie).extracting("title", "director", "releaseYear")
                .containsExactly("New Movie", "New Director", 2025);
    }

    @Test
    @DisplayName("Should add movie with null images")
    void testAddMovie_NullImages() throws IOException {
        movieFormDTO.setPosterImageFile(null);
        movieFormDTO.setBackdropImageFile(null);
        movieFormDTO.setGalleryImages(List.of());

        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie movie = invocation.getArgument(0);
            movie.setId(2L);
            return movie;
        });

        Movie result = movieService.addMovie(movieFormDTO);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getPosterFileName()).isNull();
        assertThat(result.getBackdropFileName()).isNull();
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    @DisplayName("Should update movie successfully")
    void testUpdateMovie_Success() throws IOException {
        movieFormDTO.setPosterImageFile(null);
        movieFormDTO.setBackdropImageFile(null);
        movieFormDTO.setGalleryImages(List.of());

        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Movie result = movieService.updateMovie(1L, movieFormDTO);

        assertThat(result).extracting("title", "director").containsExactly("New Movie", "New Director");
        verify(movieRepository).findById(1L);
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent movie")
    void testUpdateMovie_NotFound() throws IOException {
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> movieService.updateMovie(999L, movieFormDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Movie not found");
        verify(movieRepository).findById(999L);
    }

    @Test
    @DisplayName("Should delete movie successfully")
    void testDeleteMovie_Success() throws IOException {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        Movie result = movieService.deleteMovie(1L);
        assertThat(result).isEqualTo(testMovie);
        verify(movieRepository).findById(1L);
        verify(movieRepository).delete(testMovie);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent movie")
    void testDeleteMovie_NotFound() throws IOException {
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> movieService.deleteMovie(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Movie not found");
        verify(movieRepository).findById(999L);
        verify(movieRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should delete movie with null image filenames")
    void testDeleteMovie_NullImageNames() throws IOException {
        Movie movieWithoutImages = Movie.builder()
                .id(1L)
                .title("Test Movie")
                .posterFileName(null)
                .backdropFileName(null)
                .galleryImageNames(List.of())
                .build();
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movieWithoutImages));
        Movie result = movieService.deleteMovie(1L);
        assertThat(result).isNotNull();
        verify(movieRepository).delete(movieWithoutImages);
    }

    @Test
    @DisplayName("Should verify correct page request parameters")
    void testGetAllMoviesProjected_CorrectPageRequest() {
        Page<MovieListView> emptyPage = new PageImpl<>(List.of(), PageRequest.of(2, 5), 0);
        when(movieRepository.findAllProjectedBy(any(PageRequest.class))).thenReturn(emptyPage);
        movieService.getAllMoviesProjected(2, 5);
        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(movieRepository).findAllProjectedBy(pageCaptor.capture());
        PageRequest capturedPage = pageCaptor.getValue();
        assertThat(capturedPage.getPageNumber()).isEqualTo(2);
        assertThat(capturedPage.getPageSize()).isEqualTo(5);
    }
    @Test
    @DisplayName("Should convert movie to form DTO")
    void testConvertMovieToFormDTO() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));

        MovieFormDTO result = movieService.getMovieFormDTOById(1L);

        assertThat(result)
                .isNotNull()
                .extracting("id", "title", "description", "durationInMinutes",
                        "director", "cast", "releaseYear", "productionCountry",
                        "ageRestriction", "trailerYoutubeUrl", "posterFileName", "backdropFileName")
                .containsExactly(1L, "Test Movie", "Test Description", 120,
                        "Test Director", "Test Cast", 2024, "USA", 13,
                        "https://youtube.com/test", "poster_123.jpg", "backdrop_123.jpg");

        verify(movieRepository).findById(1L);
    }

    @Test
    @DisplayName("Should convert movie to form DTO with null file names")
    void testConvertMovieToFormDTO_NullFileNames() {
        Movie movieWithoutImages = Movie.builder()
                .id(3L)
                .title("No Images Movie")
                .description("Movie without images")
                .genre(MovieGenre.valueOf("COMEDY"))
                .durationInMinutes(90)
                .director("Test Director")
                .cast("Test Cast")
                .releaseYear(2021)
                .productionCountry("UK")
                .ageRestriction(6)
                .trailerYoutubeUrl("https://youtube.com/test")
                .posterFileName(null)
                .backdropFileName(null)
                .build();

        when(movieRepository.findById(3L)).thenReturn(Optional.of(movieWithoutImages));

        MovieFormDTO result = movieService.getMovieFormDTOById(3L);

        assertThat(result)
                .isNotNull()
                .extracting("id", "title", "posterFileName", "backdropFileName")
                .containsExactly(3L, "No Images Movie", null, null);

        verify(movieRepository).findById(3L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when converting non-existent movie to DTO")
    void testConvertMovieToFormDTO_NotFound() {
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.getMovieFormDTOById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Movie not found");

        verify(movieRepository).findById(999L);
    }

}