package io.github.jakubpakula1.cinema.exception;

public class ScreeningDateInPastException extends RuntimeException {
    public ScreeningDateInPastException() {
        super("Screening date cannot be in the past.");
    }
}
