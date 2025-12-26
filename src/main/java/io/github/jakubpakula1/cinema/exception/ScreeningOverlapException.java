package io.github.jakubpakula1.cinema.exception;

public class ScreeningOverlapException extends RuntimeException {
    public ScreeningOverlapException(String message) {
        super(message);
    }
}
