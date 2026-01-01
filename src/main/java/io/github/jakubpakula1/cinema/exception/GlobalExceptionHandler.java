package io.github.jakubpakula1.cinema.exception;

import io.github.jakubpakula1.cinema.dto.ScreeningDTO;
import io.github.jakubpakula1.cinema.service.MovieService;
import io.github.jakubpakula1.cinema.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final MovieService movieService;
    private final RoomService roomService;

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException e, Model model) {
        System.err.println("Error 404: " + e.getMessage());

        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("errorCode", "404");

        return "error/error-page";
    }

    @ExceptionHandler(ScreeningOverlapException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleOverlapException(ScreeningOverlapException e, Model model) {
        System.err.println("Conflict: " + e.getMessage());

        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("errorCode", "409");

        // Add dropdowns for form
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("screening", new ScreeningDTO());
        return "screening/admin/screening-form";
    }

    @ExceptionHandler(ScreeningDateInPastException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleScreeningDateInPastException(ScreeningDateInPastException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("errorCode", "400");

        // Add dropdowns for form
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("screening", new ScreeningDTO());
        return "screening/admin/screening-form";
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
