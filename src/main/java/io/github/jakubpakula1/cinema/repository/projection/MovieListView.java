package io.github.jakubpakula1.cinema.repository.projection;

public interface MovieListView {
    Long getId();
    String getTitle();
    String getGenre();
    String getDirector();
    Integer getReleaseYear();
    Integer getDurationInMinutes();
    String getPosterFileName();
}
