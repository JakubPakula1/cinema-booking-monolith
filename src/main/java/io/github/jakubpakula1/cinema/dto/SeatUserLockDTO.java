package io.github.jakubpakula1.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SeatUserLockDTO {
    Long userId;
    Long seatId;
    private LocalDateTime expiresAt;
}
