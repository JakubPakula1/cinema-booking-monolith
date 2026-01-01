package io.github.jakubpakula1.cinema.enums;

import lombok.Getter;

@Getter
public enum MovieGenre {
    ACTION("Action"),
    ADVENTURE("Adventure"),
    ANIMATION("Animation"),
    COMEDY("Comedy"),
    DOCUMENTARY("Documentary"),
    DRAMA("Drama"),
    FAMILY("Family"),
    FANTASY("Fantasy"),
    HORROR("Horror"),
    ROMANCE("Romance"),
    SCI_FI("Sci-Fi"),
    THRILLER("Thriller"),
    OTHER("Other");

    private final String displayName;

    MovieGenre(String displayName) {
        this.displayName = displayName;
    }
}
