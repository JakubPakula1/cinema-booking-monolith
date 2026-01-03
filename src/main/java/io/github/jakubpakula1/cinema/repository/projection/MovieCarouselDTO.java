package io.github.jakubpakula1.cinema.repository.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieCarouselDTO {
    private Long id;
    private String title;
    private String genre;
    private String backdropFileName;
    private String shortDescription;
}