package io.github.jakubpakula1.cinema.dto;

import io.github.jakubpakula1.cinema.enums.MovieGenre;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Getter @Setter
public class MovieFormDTO {

    private Long id;

    //Basic movie details
    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotNull(message = "Genre is required")
    private MovieGenre genre;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Movie duration must be at least 1 minute")
    private Integer durationInMinutes;

    //Meta information
    @NotBlank(message = "Director is required")
    private String director;
    @NotBlank(message = "Cast is required")
    private String cast; // Comma-separated list of main actors
    @NotNull(message = "Release year is required")
    @Min(value = 1800, message = "Release year must be valid")
    private Integer releaseYear;
    @NotBlank(message = "Production country is required")
    private String productionCountry;
    @NotNull(message = "Age restriction is required")
    @Min(value = 0, message = "Age restriction cannot be negative")
    private Integer ageRestriction;

    //Files
    private MultipartFile posterImageFile;
    private MultipartFile backdropImageFile;

    private List<MultipartFile> galleryImages;

    //External URLs
    private String trailerYoutubeUrl;

    private String posterFileName;
    private String backdropFileName;
}
