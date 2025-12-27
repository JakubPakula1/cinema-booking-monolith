package io.github.jakubpakula1.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SeatStatusDTO {
    private Long seatId;
    private int rowNumber;
    private  int seatNumber;
    private Long userId; // Nullable, null if seat is available
    private boolean isAvailable;
}
