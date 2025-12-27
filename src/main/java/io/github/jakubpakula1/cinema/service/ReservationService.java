package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.dto.ReservationRequestDTO;
import io.github.jakubpakula1.cinema.exception.ResourceNotFoundException;
import io.github.jakubpakula1.cinema.model.Seat;
import io.github.jakubpakula1.cinema.model.TemporaryReservation;
import io.github.jakubpakula1.cinema.model.User;
import io.github.jakubpakula1.cinema.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final ScreeningRepository screeningRepository;
    private final TemporaryReservationRepository temporaryReservationRepository;

    @Transactional
    public void createTemporaryReservation(ReservationRequestDTO request, User user) {
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

        TemporaryReservation tempReservation = new TemporaryReservation();
        tempReservation.setSeat(seat);
        tempReservation.setScreening(screeningRepository.getReferenceById(request.getScreeningId()));
        tempReservation.setUser(user);
        tempReservation.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        temporaryReservationRepository.save(tempReservation);
    }

    @Transactional
    public void deleteTemporaryReservation(ReservationRequestDTO request, User user) {
        TemporaryReservation tempReservation = temporaryReservationRepository
                .deleteTemporaryReservationByScreeningIdAndSeatId(request.getScreeningId(), request.getSeatId());
        if (tempReservation == null || !tempReservation.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Temporary reservation not found");
        }
    }
}
