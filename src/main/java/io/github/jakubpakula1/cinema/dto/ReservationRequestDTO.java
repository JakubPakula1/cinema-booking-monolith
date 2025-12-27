package io.github.jakubpakula1.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDTO {
    private Long seatId;
    private Long screeningId;
}