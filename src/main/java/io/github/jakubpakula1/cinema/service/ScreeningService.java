package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.dto.CollisionDTO;
import io.github.jakubpakula1.cinema.dto.ScreeningDTO;
import io.github.jakubpakula1.cinema.dto.ScreeningListDTO;
import io.github.jakubpakula1.cinema.exception.ResourceNotFoundException;
import io.github.jakubpakula1.cinema.exception.ScreeningOverlapException;
import io.github.jakubpakula1.cinema.model.Movie;
import io.github.jakubpakula1.cinema.model.Room;
import io.github.jakubpakula1.cinema.model.Screening;
import io.github.jakubpakula1.cinema.repository.MovieRepository;
import io.github.jakubpakula1.cinema.repository.RoomRepository;
import io.github.jakubpakula1.cinema.repository.ScreeningRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScreeningService {
    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    private final long cleaningDurationInMinutes;

    public  ScreeningService(ScreeningRepository screeningRepository, MovieRepository movieRepository, RoomRepository roomRepository, @Value("${cinema.cleaning-duration-minutes}") long cleaningDurationInMinutes) {
        this.screeningRepository = screeningRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.cleaningDurationInMinutes = cleaningDurationInMinutes;
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
            throw new IllegalArgumentException("Screening time cannot be in the past.");
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
 }
