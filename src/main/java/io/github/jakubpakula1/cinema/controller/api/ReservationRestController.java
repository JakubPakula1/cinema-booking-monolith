package io.github.jakubpakula1.cinema.controller.api;

import io.github.jakubpakula1.cinema.dto.reservation.ReservationRequestDTO;
import io.github.jakubpakula1.cinema.dto.reservation.ReservationResponseDTO;
import io.github.jakubpakula1.cinema.model.TemporaryReservation;
import io.github.jakubpakula1.cinema.model.User;
import io.github.jakubpakula1.cinema.service.ReservationService;
import io.github.jakubpakula1.cinema.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationRestController {
    private final UserService userService;
    private final ReservationService reservationService;

    @PostMapping("/lock")
    public ResponseEntity<ReservationResponseDTO> createReservation(@RequestBody ReservationRequestDTO request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.getUserByEmail(principal.getName());

        TemporaryReservation tempRes = reservationService.createTemporaryReservation(request, user);

        ReservationResponseDTO response = ReservationResponseDTO.builder()
                .id(tempRes.getId())
                .seatId(tempRes.getSeat().getId())
                .screeningId(tempRes.getScreening().getId())
                .expiresAt(tempRes.getExpiresAt())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/lock")
    public ResponseEntity<?> cancelReservation(@RequestBody ReservationRequestDTO request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.getUserByEmail(principal.getName());

        reservationService.deleteTemporaryReservation(request, user);

        return ResponseEntity.ok().build();
    }
}
