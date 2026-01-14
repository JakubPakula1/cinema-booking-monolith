package io.github.jakubpakula1.cinema.repository.projection;

import io.github.jakubpakula1.cinema.enums.MovieGenre;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MovieListViewDTO {
    private Long id;
    private String title;
    private MovieGenre genre;
    private String director;
    private Integer releaseYear;
    private Integer durationInMinutes;
    private String posterFileName;
}