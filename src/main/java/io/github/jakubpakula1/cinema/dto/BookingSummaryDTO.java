package io.github.jakubpakula1.cinema.dto;

import io.github.jakubpakula1.cinema.model.TicketType;
import io.github.jakubpakula1.cinema.model.Movie;
import io.github.jakubpakula1.cinema.model.Screening;
import io.github.jakubpakula1.cinema.model.TemporaryReservation;
import io.github.jakubpakula1.cinema.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingSummaryDTO {
    private List<TemporaryReservation> reservations;
    private Screening screening;
    private Movie movie;
    private List<TicketType> ticketTypes;
    private User user;
    private LocalDateTime expirationTime;
}
