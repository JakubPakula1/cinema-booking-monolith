package io.github.jakubpakula1.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeatUserLockDTO {
    Long userId;
    Long seatId;
}
