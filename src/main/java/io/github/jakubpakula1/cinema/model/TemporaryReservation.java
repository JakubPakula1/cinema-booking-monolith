package io.github.jakubpakula1.cinema.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "temporary_reservations")
public class TemporaryReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long seatId;

    @Column(nullable = false)
    private Long screeningId;

    private String sessionId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public TemporaryReservation() {
    }

}
