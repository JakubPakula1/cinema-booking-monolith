package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.dto.MovieFormDTO;
import io.github.jakubpakula1.cinema.exception.ResourceNotFoundException;
import io.github.jakubpakula1.cinema.model.Movie;
import io.github.jakubpakula1.cinema.repository.MovieRepository;
import io.github.jakubpakula1.cinema.repository.projection.MovieCarouselDTO;
import io.github.jakubpakula1.cinema.repository.projection.MovieListViewDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final String uploadDir;

    public MovieService(MovieRepository movieRepository, @Value("${cinema.upload-dir}") String uploadDir) {
        this.movieRepository = movieRepository;
        this.uploadDir = uploadDir;
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Page<MovieListViewDTO> getAllMoviesProjected(int page, int size) {
        return movieRepository.findAllProjectedBy(PageRequest.of(page, size));
    }

    public Movie getMovieById(Long id) {
        return movieRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Movie not found"));
    }

    public MovieFormDTO getMovieFormDTOById(Long id) {
        return convertMovieToFormDTO(getMovieById(id));
    }
    public List<MovieCarouselDTO> getLatestMovies(int count) {
        return movieRepository.findLatestMoviesForCarousel(PageRequest.of(0, count));

    }
    public Movie addMovie(MovieFormDTO movieDTO) throws IOException{
        String savedPosterFileName = saveImage(movieDTO.getPosterImageFile());
        String savedBackdropFileName = saveImage(movieDTO.getBackdropImageFile());


        Movie newMovie = Movie.builder()
                .title(movieDTO.getTitle())
                .description(movieDTO.getDescription())
                .genre(movieDTO.getGenre())
                .durationInMinutes(movieDTO.getDurationInMinutes())
                .director(movieDTO.getDirector())
                .cast(movieDTO.getCast())
                .releaseYear(movieDTO.getReleaseYear())
                .productionCountry(movieDTO.getProductionCountry())
                .ageRestriction(movieDTO.getAgeRestriction())
                .posterFileName(savedPosterFileName)
                .backdropFileName(savedBackdropFileName)
                .trailerYoutubeUrl(movieDTO.getTrailerYoutubeUrl())
                .galleryImageNames(new ArrayList<>())
                .build();

        saveGalleryImages(newMovie, movieDTO.getGalleryImages());

        return movieRepository.save(newMovie);
    }

    public Movie updateMovie(Long id, MovieFormDTO movieDTO) throws IOException {
        Movie existingMovie = getMovieById(id);

        existingMovie.setTitle(movieDTO.getTitle());
        existingMovie.setDescription(movieDTO.getDescription());
        existingMovie.setGenre(movieDTO.getGenre());
        existingMovie.setDurationInMinutes(movieDTO.getDurationInMinutes());
        existingMovie.setDirector(movieDTO.getDirector());
        existingMovie.setCast(movieDTO.getCast());
        existingMovie.setReleaseYear(movieDTO.getReleaseYear());
        existingMovie.setProductionCountry(movieDTO.getProductionCountry());
        existingMovie.setAgeRestriction(movieDTO.getAgeRestriction());
        existingMovie.setTrailerYoutubeUrl(movieDTO.getTrailerYoutubeUrl());

        String savedPosterFileName = saveImage(movieDTO.getPosterImageFile());
        if (savedPosterFileName != null) {
            if(existingMovie.getPosterFileName() != null){
                Files.deleteIfExists(Paths.get("uploads", existingMovie.getPosterFileName()));
            }
            existingMovie.setPosterFileName(savedPosterFileName);
        }

        String savedBackdropFileName = saveImage(movieDTO.getBackdropImageFile());
        if (savedBackdropFileName != null) {
            if(existingMovie.getBackdropFileName() != null){
                Files.deleteIfExists(Paths.get("uploads", existingMovie.getBackdropFileName()));
            }
            existingMovie.setBackdropFileName(savedBackdropFileName);
        }

        saveGalleryImages(existingMovie, movieDTO.getGalleryImages());

        return movieRepository.save(existingMovie);
    }

    public Movie deleteMovie(Long id) throws IOException {
        Movie movieToDelete = getMovieById(id);

        // Delete poster image
        if (movieToDelete.getPosterFileName() != null) {
            Files.deleteIfExists(Paths.get(uploadDir, movieToDelete.getPosterFileName()));
        }

        // Delete backdrop image
        if (movieToDelete.getBackdropFileName() != null) {
            Files.deleteIfExists(Paths.get(uploadDir, movieToDelete.getBackdropFileName()));
        }

        // Delete gallery images
        for (String galleryImageName : movieToDelete.getGalleryImageNames()) {
            Files.deleteIfExists(Paths.get(uploadDir, galleryImageName));
        }

        movieRepository.delete(movieToDelete);
        return movieToDelete;
    }

    private String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)){
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    private void saveGalleryImages(Movie movie, List<MultipartFile> galleryImages) throws IOException {
        if (galleryImages != null) {
            for (MultipartFile galleryImage : galleryImages) {
                String savedGalleryImageFileName = saveImage(galleryImage);
                if (savedGalleryImageFileName != null) {
                    movie.getGalleryImageNames().add(savedGalleryImageFileName);
                }
            }
        }
    }

    private MovieFormDTO convertMovieToFormDTO(Movie movie) {
        MovieFormDTO movieFormDTO = new MovieFormDTO();
        movieFormDTO.setId(movie.getId());
        movieFormDTO.setTitle(movie.getTitle());
        movieFormDTO.setDescription(movie.getDescription());
        movieFormDTO.setGenre(movie.getGenre());
        movieFormDTO.setDurationInMinutes(movie.getDurationInMinutes());
        movieFormDTO.setDirector(movie.getDirector());
        movieFormDTO.setCast(movie.getCast());
        movieFormDTO.setReleaseYear(movie.getReleaseYear());
        movieFormDTO.setProductionCountry(movie.getProductionCountry());
        movieFormDTO.setAgeRestriction(movie.getAgeRestriction());
        movieFormDTO.setTrailerYoutubeUrl(movie.getTrailerYoutubeUrl());
        movieFormDTO.setPosterFileName(movie.getPosterFileName());
        movieFormDTO.setBackdropFileName(movie.getBackdropFileName());
        return movieFormDTO;
    }


}
