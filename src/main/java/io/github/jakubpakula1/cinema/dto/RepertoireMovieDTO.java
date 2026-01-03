package io.github.jakubpakula1.cinema.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RepertoireMovieDTO {
    private Long movieId;
    private String title;
    private String genre;
    private Integer duration;
    private String posterFileName;

    private List<ScreeningTimeDTO> screenings;
}

