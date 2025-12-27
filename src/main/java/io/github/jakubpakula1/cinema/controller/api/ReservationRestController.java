package io.github.jakubpakula1.cinema.controller.api;

import io.github.jakubpakula1.cinema.dto.ReservationRequestDTO;
import io.github.jakubpakula1.cinema.model.User;
import io.github.jakubpakula1.cinema.service.ReservationService;
import io.github.jakubpakula1.cinema.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationRestController {
    private final UserService userService;
    private final ReservationService reservationService;

    @PostMapping("/lock")
    public ResponseEntity<?> createReservation(@RequestBody ReservationRequestDTO request, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // userDetails ma w sobie email/login, ale nie ma ID z bazy.
        // Musimy pobrać pełną encję User z bazy na podstawie emaila.
        User user = userService.getUserByEmail(userDetails.getUsername());

        // Przekazujemy Usera do serwisu
        reservationService.createTemporaryReservation(request, user);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/lock")
    public ResponseEntity<?> cancelReservation(@RequestBody ReservationRequestDTO request, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.getUserByEmail(userDetails.getUsername());

        reservationService.deleteTemporaryReservation(request, user);

        return ResponseEntity.ok().build();
    }
}
