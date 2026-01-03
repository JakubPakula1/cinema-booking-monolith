package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.dto.BookingSummaryDTO;
import io.github.jakubpakula1.cinema.dto.ReservationRequestDTO;
import io.github.jakubpakula1.cinema.model.TicketType;
import io.github.jakubpakula1.cinema.exception.EmptyCartException;
import io.github.jakubpakula1.cinema.exception.ResourceNotFoundException;
import io.github.jakubpakula1.cinema.model.*;
import io.github.jakubpakula1.cinema.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final ScreeningRepository screeningRepository;
    private final TemporaryReservationRepository temporaryReservationRepository;
    private final UserService userService;
    private final TicketTypeRepository ticketTypeRepository;
    @Value("${cinema.reservation-expiration-minutes}")
    private  int RESERVATION_TIME_MINUTES;

     @Transactional
    public TemporaryReservation createTemporaryReservation(ReservationRequestDTO request, User user) {
        Seat seat = seatRepository.findSeatWithLock(request.getSeatId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));

        boolean isTaken = temporaryReservationRepository.existsBySeatIdAndScreeningIdAndExpiresAtAfter(
                seat.getId(),
                request.getScreeningId(),
                LocalDateTime.now()
        );

        boolean isSold = ticketRepository.existsBySeatIdAndScreeningId(
                seat.getId(),
                request.getScreeningId()
        );
        if (isTaken || isSold) {
            throw new IllegalStateException("Seat is already reserved");
        }
        LocalDateTime newExpirationTime = LocalDateTime.now().plusMinutes(RESERVATION_TIME_MINUTES);
        TemporaryReservation tempReservation = new TemporaryReservation();
        tempReservation.setSeat(seat);
        tempReservation.setScreening(screeningRepository.getReferenceById(request.getScreeningId()));
        tempReservation.setUser(user);
        tempReservation.setExpiresAt(newExpirationTime);

        temporaryReservationRepository.save(tempReservation);

        List<TemporaryReservation> userReservations = temporaryReservationRepository
                .findAllByUserIdAndExpiresAtAfter(user.getId(), LocalDateTime.now());
        for (TemporaryReservation reservation : userReservations) {
            if(!reservation.getId().equals(tempReservation.getId())){
                reservation.setExpiresAt(newExpirationTime);
            }
        }
        return tempReservation;
    }

    @Transactional
    public void deleteTemporaryReservation(ReservationRequestDTO request, User user) {
        List<TemporaryReservation> reservations = temporaryReservationRepository.findByUserIdAndSeatIdAndScreeningId(
                user.getId(),
                request.getSeatId(),
                request.getScreeningId()
        );
        if (reservations.isEmpty()) {
            throw new ResourceNotFoundException("Temporary reservation not found");
        }
        temporaryReservationRepository.deleteAll(reservations);
    }

    @Transactional
    public BookingSummaryDTO prepareSummary(String userEmail) {
        User user = userService.getUserByEmail(userEmail);

        List<TemporaryReservation> reservations = temporaryReservationRepository.findAllByUserIdAndExpiresAtAfter(user.getId(), LocalDateTime.now());
        LocalDateTime newExpirationTime = LocalDateTime.now().plusMinutes(RESERVATION_TIME_MINUTES);

        if (reservations.isEmpty()) {
            throw new EmptyCartException("Your cart is empty!");
        }

        for (TemporaryReservation reservation : reservations) {
            reservation.setExpiresAt(newExpirationTime);
        }

        Screening screening = reservations.getFirst().getScreening();
        Movie movie = screening.getMovie();

        List<TicketType> ticketTypes = ticketTypeRepository.findAll();

        return BookingSummaryDTO.builder()
                .reservations(reservations)
                .screening(screening)
                .movie(movie)
                .ticketTypes(ticketTypes)
                .user(user)
                .expirationTime(newExpirationTime)
                .build();
    }
}
