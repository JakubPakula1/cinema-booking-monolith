package io.github.jakubpakula1.cinema.model;

import io.github.jakubpakula1.cinema.enums.MovieGenre;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Basic movie details
    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private MovieGenre genre;

    @Column(name = "duration_minutes")
    private int durationInMinutes;

    //Meta information
    private String director;
    @Column(name = "cast_actors")
    private String cast; // Comma-separated list of main actors
    private int releaseYear;
    private String productionCountry;
    private int ageRestriction;

    //Files
    private String posterFileName;
    private String backdropFileName;

    //External URLs
    private String trailerYoutubeUrl;

    //Relationships
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Screening> screenings = new ArrayList<>();

    //Gallery
    @ElementCollection
    @CollectionTable(name = "movie_images", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "file_name")
    private List<String> galleryImageNames = new ArrayList<>();

}
