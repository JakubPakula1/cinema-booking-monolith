package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.dto.*;
import io.github.jakubpakula1.cinema.exception.ResourceNotFoundException;
import io.github.jakubpakula1.cinema.exception.ScreeningDateInPastException;
import io.github.jakubpakula1.cinema.exception.ScreeningOverlapException;
import io.github.jakubpakula1.cinema.model.*;
import io.github.jakubpakula1.cinema.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ScreeningService {
    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final TicketRepository ticketRepository;
    private final TemporaryReservationRepository temporaryReservationRepository;

    private final long cleaningDurationInMinutes;
    private final SeatRepository seatRepository;

    public  ScreeningService(ScreeningRepository screeningRepository, MovieRepository movieRepository, RoomRepository roomRepository, TicketRepository ticketRepository, TemporaryReservationRepository temporaryReservationRepository, @Value("${cinema.cleaning-duration-minutes}") long cleaningDurationInMinutes, SeatRepository seatRepository) {
        this.screeningRepository = screeningRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.cleaningDurationInMinutes = cleaningDurationInMinutes;
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
        this.temporaryReservationRepository = temporaryReservationRepository;
    }

    @Transactional(readOnly = true)
    public List<Screening> getAllScreenings() {
        return screeningRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ScreeningListDTO> getAllScreeningsForList() {
        return screeningRepository.findAll().stream()
                .map(screening -> ScreeningListDTO.builder()
                        .id(screening.getId())
                        .movieTitle(screening.getMovie().getTitle())
                        .roomName(screening.getRoom().getName())
                        .screeningDate(screening.getStartTime().toLocalDate())
                        .startTime(screening.getStartTime().toLocalTime())
                        .endTime(screening.getEndTime().toLocalTime())
                        .build())
                .toList();
    }

    @Transactional
    public void createScreening(ScreeningDTO screeningDTO) {

        if (screeningDTO.getScreeningTime().isBefore(LocalDateTime.now())) {
            throw new ScreeningDateInPastException();
        }

        Room room = roomRepository.findById(screeningDTO.getRoomId()).orElseThrow(() -> new ResourceNotFoundException("Room Not Found"));

        Movie movie = movieRepository.findById(screeningDTO.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid movie ID: " + screeningDTO.getMovieId()));

        LocalDateTime endTime = screeningDTO.getScreeningTime().plusMinutes(movie.getDurationInMinutes());

        boolean isOverlapping = screeningRepository.existsByRoomIdAndStartTimeBeforeAndEndTimeAfter(
                screeningDTO.getRoomId(),
                endTime.plusMinutes(cleaningDurationInMinutes),
                screeningDTO.getScreeningTime().minusMinutes(cleaningDurationInMinutes)
        );

        if(isOverlapping) {
            throw new ScreeningOverlapException("Screening overlaps with an existing screening in the same room.");
        }

        Screening screening = new Screening();
        screening.setMovie(movie);
        screening.setRoom(room);
        screening.setStartTime(screeningDTO.getScreeningTime());
        screening.setEndTime(endTime);

        screeningRepository.save(screening);
    }

    @Transactional(readOnly = true)
    public List<CollisionDTO> getCollidingScreenings(Long roomId, LocalDateTime proposedStartTime, LocalDateTime proposedEndTime) {
        List<Screening> screenings = screeningRepository.findByRoomIdAndStartTimeBeforeAndEndTimeAfter(
                roomId,
                proposedEndTime.plusMinutes(cleaningDurationInMinutes),
                proposedStartTime.minusMinutes(cleaningDurationInMinutes)
        );

        return screenings.stream()
                .map(screening -> CollisionDTO.builder()
                        .movieTitle(screening.getMovie().getTitle())
                        .roomName(screening.getRoom().getName())
                        .screeningTime(screening.getStartTime())
                        .endTime(screening.getEndTime())
                        .build())
                .toList();
    }


    @Transactional(readOnly = true)
    public List<CollisionDTO> getCollidingScreeningsByMovie(Long roomId, Long movieId, LocalDateTime start) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        LocalDateTime end = start.plusMinutes(movie.getDurationInMinutes());

        return getCollidingScreenings(roomId, start, end);
    }

    @Transactional
    public List<SeatStatusDTO> getSeatsWithStatus(Long screeningId) {
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found with id: " + screeningId));

        Room room = screening.getRoom();
        List<Seat> allSeats = seatRepository.findAllByRoomId(room.getId());

        List<Long> soldSeatIds = ticketRepository.findSoldSeatIdsByScreeningId(screeningId);

        List<SeatUserLockDTO> lockedSeats = temporaryReservationRepository.findLockedSeatIdsByScreeningId(screeningId);
        List<Long> lockedSeatIds = lockedSeats.stream()
                .map(SeatUserLockDTO::getSeatId)
                .toList();

        Set<Long> takenSeatIds = new HashSet<>();
        takenSeatIds.addAll(soldSeatIds);
        takenSeatIds.addAll(lockedSeatIds);

        return allSeats.stream()
                .map(seat -> {
                    boolean isTaken = takenSeatIds.contains(seat.getId());
                    Long userId = null;
                    if (isTaken) {
                        userId = lockedSeats.stream()
                                .filter(lock -> lock.getSeatId().equals(seat.getId()))
                                .map(SeatUserLockDTO::getUserId)
                                .findFirst()
                                .orElse(null);
                    }
                    return SeatStatusDTO.builder()
                            .seatId(seat.getId())
                            .rowNumber(seat.getRowNumber())
                            .seatNumber(seat.getSeatNumber())
                            .userId(userId)
                            .isAvailable(!isTaken)
                            .build();
                })
                .toList();

    }
}
