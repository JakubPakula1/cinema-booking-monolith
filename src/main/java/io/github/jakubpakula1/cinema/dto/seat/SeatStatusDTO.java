package io.github.jakubpakula1.cinema.dto.seat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class SeatStatusDTO {
    private Long seatId;
    private int rowNumber;
    private  int seatNumber;
    private Long userId; // Nullable, null if seat is available
    private boolean isAvailable;

    private LocalDateTime expiresAt; // Nullable, null if seat is available
}
