package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.dto.screening.*;
import io.github.jakubpakula1.cinema.dto.seat.SeatStatusDTO;
import io.github.jakubpakula1.cinema.dto.seat.SeatUserLockDTO;
import io.github.jakubpakula1.cinema.exception.ResourceNotFoundException;
import io.github.jakubpakula1.cinema.exception.ScreeningDateInPastException;
import io.github.jakubpakula1.cinema.exception.ScreeningOverlapException;
import io.github.jakubpakula1.cinema.model.*;
import io.github.jakubpakula1.cinema.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public ScreeningDTO getScreeningById(Long screeningId) {
        Optional<Screening> screening= screeningRepository.findById(screeningId);
        if (screening.isEmpty()) {
            throw new ResourceNotFoundException("Screening not found with id: " + screeningId);
        }
        return ScreeningDTO.builder()
                .id(screening.get().getId())
                .movieId(screening.get().getMovie().getId())
                .roomId(screening.get().getRoom().getId())
                .screeningTime(screening.get().getStartTime())
                .build();
    }

    @Transactional
    public Screening getScreeningEntityById(Long id) {
        return screeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found"));
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

    @Transactional
    public void updateScreening(Long screeningId, ScreeningDTO screeningDTO) {
        Screening existingScreening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found with id: " + screeningId));
        if (screeningDTO.getScreeningTime().isBefore(LocalDateTime.now())) {
            throw new ScreeningDateInPastException();
        }
        Room room = roomRepository.findById(screeningDTO.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room Not Found"));

        Movie movie = movieRepository.findById(screeningDTO.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid movie ID: " + screeningDTO.getMovieId()));

        LocalDateTime endTime = screeningDTO.getScreeningTime().plusMinutes(movie.getDurationInMinutes());

        boolean isOverlapping = screeningRepository.existsByRoomIdAndStartTimeBeforeAndEndTimeAfterAndIdNot(
                screeningDTO.getRoomId(),
                endTime.plusMinutes(cleaningDurationInMinutes),
                screeningDTO.getScreeningTime().minusMinutes(cleaningDurationInMinutes),
                screeningId
        );

        if(isOverlapping) {
            throw new ScreeningOverlapException("Screening overlaps with an existing screening in the same room.");
        }
        existingScreening.setMovie(movie);
        existingScreening.setRoom(room);
        existingScreening.setStartTime(screeningDTO.getScreeningTime());
        existingScreening.setEndTime(endTime);
        screeningRepository.save(existingScreening);
    }

    @Transactional
    public void deleteScreening(Long screeningId) {
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found with id: " + screeningId));
        screeningRepository.delete(screening);
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
                        .cleaningDurationMinutes(cleaningDurationInMinutes)
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

        Set<Long> soldSeatIds = new HashSet<>(ticketRepository.findSoldSeatIdsByScreeningId(screeningId));

        List<SeatUserLockDTO> lockedSeats = temporaryReservationRepository.findLockedSeatIdsByScreeningId(screeningId);
        Map<Long, SeatUserLockDTO> locksMap = lockedSeats.stream()
                .collect(Collectors.toMap(SeatUserLockDTO::getSeatId, lock -> lock));

        return allSeats.stream()
                .map(seat -> {
                    Long seatId = seat.getId();

                    boolean isSold = soldSeatIds.contains(seatId);
                    SeatUserLockDTO lockInfo = locksMap.get(seatId); // Pobieramy info o blokadzie z mapy
                    boolean isLocked = (lockInfo != null);

                    boolean isTaken = isSold || isLocked;
                    Long userId = isLocked ? lockInfo.getUserId() : null;
                    LocalDateTime expiresAt = isLocked ? lockInfo.getExpiresAt() : null;

                    return SeatStatusDTO.builder()
                            .seatId(seatId)
                            .rowNumber(seat.getRowNumber())
                            .seatNumber(seat.getSeatNumber())
                            .userId(userId)
                            .isAvailable(!isTaken)
                            .expiresAt(expiresAt)
                            .build();
                })
                .sorted(Comparator.comparing(SeatStatusDTO::getRowNumber)
                        .thenComparing(SeatStatusDTO::getSeatNumber)) // Warto sortować też po numerze w rzędzie
                .toList();

    }

    @Transactional(readOnly = true)
    public List<RepertoireMovieDTO> getRepertoireForDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Screening> screenings = screeningRepository.findAllByStartTimeBetweenOrderByStartTimeAsc(startOfDay, endOfDay);

        Map<Movie, List<Screening>> screeningsByMovie = screenings.stream()
                .collect(Collectors.groupingBy(Screening::getMovie));

        List<RepertoireMovieDTO> result = new ArrayList<>();

        for (Map.Entry<Movie, List<Screening>> entry : screeningsByMovie.entrySet()) {
            Movie movie = entry.getKey();
            List<Screening> movieScreenings = entry.getValue();
            List<ScreeningTimeDTO> timeDTOs = movieScreenings.stream()
                    .map(s -> {
                        boolean isTimeValid = s.getStartTime().isAfter(LocalDateTime.now().plusMinutes(15));
                        //TODO Add check if any seats are available for screening(not sold)
                        return ScreeningTimeDTO.builder()
                                .screeningId(s.getId())
                                .time(s.getStartTime().toLocalTime())
                                .isAvailable(isTimeValid)
                                .roomName(s.getRoom().getName())
                                .build();
                    })
                    .sorted(Comparator.comparing(ScreeningTimeDTO::getTime))
                    .collect(Collectors.toList());

            result.add(RepertoireMovieDTO.builder()
                    .movieId(movie.getId())
                    .title(movie.getTitle())
                    .genre(movie.getGenre().toString())
                    .duration(movie.getDurationInMinutes())
                    .posterFileName(movie.getPosterFileName())
                    .screenings(timeDTOs)
                    .build());
        }

        result.sort(Comparator.comparing(RepertoireMovieDTO::getTitle));

        return result;
    }
}
