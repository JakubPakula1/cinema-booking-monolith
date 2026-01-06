package io.github.jakubpakula1.cinema.dto.seat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatUserLockDTO {
    Long userId;
    Long seatId;
    private LocalDateTime expiresAt;
}
