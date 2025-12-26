package io.github.jakubpakula1.cinema.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private String genre;
    @Column(name = "duration_minutes")
    private int durationInMinutes;

    private String posterUrl;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Screening> screenings = new ArrayList<>();

    public Movie() {
    }

    public Movie(String title, String description, String genre, int durationMinutes, String posterUrl) {
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.durationInMinutes = durationMinutes;
        this.posterUrl = posterUrl;
    }

}
