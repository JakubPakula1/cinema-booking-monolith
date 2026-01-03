package io.github.jakubpakula1.cinema.dto.screening;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CollisionDTO {
    private String movieTitle;
    private String roomName;
    private LocalDateTime screeningTime;
    private LocalDateTime endTime;
    private Long cleaningDurationMinutes;
}
