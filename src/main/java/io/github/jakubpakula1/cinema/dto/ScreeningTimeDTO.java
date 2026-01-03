package io.github.jakubpakula1.cinema.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class ScreeningTimeDTO {
    private Long screeningId;
    private LocalTime time;
    private boolean isAvailable;
    private String roomName;
}
