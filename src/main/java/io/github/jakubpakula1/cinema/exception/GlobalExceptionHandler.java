package io.github.jakubpakula1.cinema.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException e, Model model) {
        System.err.println("Error 404: " + e.getMessage());

        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("errorCode", "404");

        return "error/error-page";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralError(Exception e, Model model) {
        e.printStackTrace();

        model.addAttribute("errorMessage", "Unexpected error occurred.");
        model.addAttribute("errorCode", "500");
        model.addAttribute("technicalDetails", e.getMessage());

        return "error/error-page";
    }
}
