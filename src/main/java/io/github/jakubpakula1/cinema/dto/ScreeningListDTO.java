package io.github.jakubpakula1.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreeningListDTO {
    private Long id;
    private String movieTitle;
    private String roomName;
    private LocalDate screeningDate;
    private LocalTime startTime;
    private LocalTime endTime;
}