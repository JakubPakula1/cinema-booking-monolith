package io.github.jakubpakula1.cinema.controller.api;

import io.github.jakubpakula1.cinema.dto.screening.CollisionDTO;
import io.github.jakubpakula1.cinema.service.ScreeningService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/screenings")
@RequiredArgsConstructor
public class ScreeningRestController {
    private final ScreeningService screeningService;

    @GetMapping("/collisions")
    public ResponseEntity<List<CollisionDTO>> checkAvailability(
            @RequestParam Long movieId,
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime
    ) {

        List<CollisionDTO> collisions = screeningService.getCollidingScreeningsByMovie(roomId, movieId, startTime);

        return ResponseEntity.ok(collisions);
    }

    // /api/v1/screenings/check?roomId=1&from=...&to=...
    @GetMapping("/check")
    public ResponseEntity<List<CollisionDTO>> checkAvailability(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        List<CollisionDTO> collisions = screeningService.getCollidingScreenings(roomId, from, to);

        return ResponseEntity.ok(collisions);
    }
}
