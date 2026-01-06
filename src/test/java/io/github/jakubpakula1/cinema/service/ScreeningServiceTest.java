package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.dto.screening.*;
import io.github.jakubpakula1.cinema.dto.seat.SeatStatusDTO;
import io.github.jakubpakula1.cinema.dto.seat.SeatUserLockDTO;
import io.github.jakubpakula1.cinema.enums.MovieGenre;
import io.github.jakubpakula1.cinema.exception.ResourceNotFoundException;
import io.github.jakubpakula1.cinema.exception.ScreeningDateInPastException;
import io.github.jakubpakula1.cinema.exception.ScreeningOverlapException;
import io.github.jakubpakula1.cinema.model.*;
import io.github.jakubpakula1.cinema.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ScreeningService covering:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Business logic validation (date validation, overlap detection)
 * - Data transformation (entities to DTOs)
 * - Exception handling
 */
@ExtendWith(MockitoExtension.class)
public class ScreeningServiceTest {

    @Mock
    private ScreeningRepository screeningRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TemporaryReservationRepository temporaryReservationRepository;

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private ScreeningService screeningService;

    private LocalDateTime futureDateTime;
    private Movie testMovie;
    private Room testRoom;
    private Screening testScreening;

    @BeforeEach
    void setUp() {
        futureDateTime = LocalDateTime.now().plusDays(1);

        testMovie = new Movie();
        testMovie.setId(1L);
        testMovie.setDurationInMinutes(120);
        testMovie.setTitle("Test Movie");
        testMovie.setGenre(MovieGenre.ACTION);

        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setName("Room A");

        testScreening = new Screening();
        testScreening.setId(1L);
        testScreening.setMovie(testMovie);
        testScreening.setRoom(testRoom);
        testScreening.setStartTime(futureDateTime);
        testScreening.setEndTime(futureDateTime.plusMinutes(120));
    }

    // ==================== CREATE SCREENING TESTS ====================

    @Test
    @DisplayName("Should create screening successfully with valid data")
    void testCreateScreening_Success() {
        ScreeningDTO screeningDTO = ScreeningDTO.builder()
                .movieId(1L)
                .roomId(1L)
                .screeningTime(futureDateTime)
                .build();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(screeningRepository.existsByRoomIdAndStartTimeBeforeAndEndTimeAfter(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(false);

        screeningService.createScreening(screeningDTO);

        verify(screeningRepository).save(any(Screening.class));
    }

    @Test
    @DisplayName("Should throw ScreeningDateInPastException when date is in the past")
    void testCreateScreening_DateInPast_ThrowsException() {
        ScreeningDTO screeningDTO = ScreeningDTO.builder()
                .movieId(1L)
                .roomId(1L)
                .screeningTime(LocalDateTime.now().minusDays(1))
                .build();

        assertThatThrownBy(() -> screeningService.createScreening(screeningDTO))
                .isInstanceOf(ScreeningDateInPastException.class);

        verify(screeningRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ScreeningOverlapException when screening overlaps with existing one")
    void testCreateScreening_ScreeningOverlap_ThrowsException() {
        ScreeningDTO screeningDTO = ScreeningDTO.builder()
                .movieId(1L)
                .roomId(1L)
                .screeningTime(futureDateTime)
                .build();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(screeningRepository.existsByRoomIdAndStartTimeBeforeAndEndTimeAfter(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(true);

        assertThatThrownBy(() -> screeningService.createScreening(screeningDTO))
                .isInstanceOf(ScreeningOverlapException.class)
                .hasMessageContaining("overlaps");

        verify(screeningRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when movie does not exist")
    void testCreateScreening_InvalidMovie_ThrowsException() {
        ScreeningDTO screeningDTO = ScreeningDTO.builder()
                .movieId(999L)
                .roomId(1L)
                .screeningTime(futureDateTime)
                .build();

        when(roomRepository.findById(1L)).thenReturn(Optional.ofNullable(testRoom));
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.createScreening(screeningDTO))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(screeningRepository, never()).save(any());
    }
    @Test
    @DisplayName("Should throw ResourceNotFoundException when room does not exist")
    void testCreateScreening_InvalidRoom_ThrowsException() {
        ScreeningDTO screeningDTO = ScreeningDTO.builder()
                .movieId(1L)
                .roomId(999L)
                .screeningTime(futureDateTime)
                .build();

        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.createScreening(screeningDTO))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(screeningRepository, never()).save(any());
    }


    // ==================== READ SCREENING TESTS ====================

    @Test
    @DisplayName("Should retrieve screening by ID and convert to DTO")
    void testGetScreeningById_Success() {
        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));

        ScreeningDTO result = screeningService.getScreeningById(1L);

        assertThat(result)
                .isNotNull()
                .extracting("id", "movieId", "roomId")
                .containsExactly(1L, 1L, 1L);
        assertThat(result.getScreeningTime()).isEqualTo(futureDateTime);

        verify(screeningRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when screening does not exist")
    void testGetScreeningById_NotFound_ThrowsException() {
        when(screeningRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.getScreeningById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should retrieve all screenings from repository")
    void testGetAllScreenings_Success() {
        List<Screening> screenings = List.of(testScreening);
        when(screeningRepository.findAll()).thenReturn(screenings);

        List<Screening> result = screeningService.getAllScreenings();

        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .extracting("id")
                .containsExactly(1L);

        verify(screeningRepository).findAll();
    }

    @Test
    @DisplayName("Should transform screenings to list DTOs with proper formatting")
    void testGetAllScreeningsForList_Success() {
        List<Screening> screenings = List.of(testScreening);
        when(screeningRepository.findAll()).thenReturn(screenings);

        List<ScreeningListDTO> result = screeningService.getAllScreeningsForList();

        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .extracting("id", "movieTitle", "roomName")
                .containsExactly(tuple(1L, "Test Movie", "Room A"));

        verify(screeningRepository).findAll();
    }

    // ==================== SEAT STATUS TESTS ====================

    @Test
    @DisplayName("Should calculate correct seat status with mixed sold and available seats")
    void testGetSeatsWithStatus_Success() {
        Seat seat1 = new Seat();
        seat1.setId(1L);
        seat1.setRowNumber(1);
        seat1.setSeatNumber(1);

        Seat seat2 = new Seat();
        seat2.setId(2L);
        seat2.setRowNumber(1);
        seat2.setSeatNumber(2);

        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));
        when(seatRepository.findAllByRoomId(1L)).thenReturn(List.of(seat1, seat2));
        when(ticketRepository.findSoldSeatIdsByScreeningId(1L)).thenReturn(List.of(1L));
        when(temporaryReservationRepository.findLockedSeatIdsByScreeningId(1L)).thenReturn(List.of());

        List<SeatStatusDTO> result = screeningService.getSeatsWithStatus(1L);

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .extracting("seatId", "isAvailable", "isSold")
                .containsExactlyInAnyOrder(
                        tuple(1L, false, true),
                        tuple(2L, true, false)
                );

        verify(screeningRepository).findById(1L);
    }

    @Test
    @DisplayName("Should correctly identify locked seats with user and expiration info")
    void testGetSeatsWithStatus_WithLockedSeats() {
        Seat seat1 = new Seat();
        seat1.setId(1L);
        seat1.setRowNumber(1);
        seat1.setSeatNumber(1);

        SeatUserLockDTO lockDTO = new SeatUserLockDTO();
        lockDTO.setSeatId(1L);
        lockDTO.setUserId(5L);
        lockDTO.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));
        when(seatRepository.findAllByRoomId(1L)).thenReturn(List.of(seat1));
        when(ticketRepository.findSoldSeatIdsByScreeningId(1L)).thenReturn(List.of());
        when(temporaryReservationRepository.findLockedSeatIdsByScreeningId(1L)).thenReturn(List.of(lockDTO));

        List<SeatStatusDTO> result = screeningService.getSeatsWithStatus(1L);

        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .extracting("seatId", "isAvailable", "isSold", "userId")
                .containsExactly(
                        tuple(1L, false, false, 5L)
                );
    }

    // ==================== COLLISION DETECTION TESTS ====================

    @Test
    @DisplayName("Should detect overlapping screenings with cleaning duration buffer")
    void testGetCollidingScreenings_Success() {
        List<Screening> collidingScreenings = List.of(testScreening);
        LocalDateTime proposedStart = futureDateTime;
        LocalDateTime proposedEnd = futureDateTime.plusMinutes(120);

        when(screeningRepository.findByRoomIdAndStartTimeBeforeAndEndTimeAfter(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(collidingScreenings);

        List<CollisionDTO> result = screeningService.getCollidingScreenings(1L, proposedStart, proposedEnd);

        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .extracting("movieTitle", "roomName")
                .containsExactly(
                        tuple("Test Movie", "Room A")
                );
    }

    // ==================== REPERTOIRE TESTS ====================

    @Test
    @DisplayName("Should organize repertoire by movie with screenings grouped properly")
    void testGetRepertoireForDate_Success() {
        LocalDate testDate = futureDateTime.toLocalDate();
        List<Screening> screenings = List.of(testScreening);

        when(screeningRepository.findAllByStartTimeBetweenOrderByStartTimeAsc(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(screenings);

        List<RepertoireMovieDTO> result = screeningService.getRepertoireForDate(testDate);

        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .extracting("movieId", "title", "duration")
                .containsExactly(
                        tuple(1L, "Test Movie", 120)
                );
        assertThat(result.getFirst().getScreenings()).isNotEmpty();
    }

    @Test
    @DisplayName("Should return empty list when no screenings exist for date")
    void testGetRepertoireForDate_NoScreenings_ReturnsEmptyList() {
        LocalDate testDate = futureDateTime.toLocalDate();

        when(screeningRepository.findAllByStartTimeBetweenOrderByStartTimeAsc(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of());

        List<RepertoireMovieDTO> result = screeningService.getRepertoireForDate(testDate);

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }
// ==================== UPDATE SCREENING TESTS ====================

    @Test
    @DisplayName("Should update screening successfully with valid data")
    void testUpdateScreening_Success() {
        ScreeningDTO screeningDTO = ScreeningDTO.builder()
                .movieId(1L)
                .roomId(1L)
                .screeningTime(futureDateTime.plusDays(1))
                .build();

        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(screeningRepository.existsByRoomIdAndStartTimeBeforeAndEndTimeAfterAndIdNot(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(1L)
        )).thenReturn(false);

        screeningService.updateScreening(1L, screeningDTO);

        verify(screeningRepository).save(any(Screening.class));
        verify(screeningRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when screening does not exist")
    void testUpdateScreening_ScreeningNotFound_ThrowsException() {
        ScreeningDTO screeningDTO = ScreeningDTO.builder()
                .movieId(1L)
                .roomId(1L)
                .screeningTime(futureDateTime)
                .build();

        when(screeningRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.updateScreening(99L, screeningDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");

        verify(screeningRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ScreeningDateInPastException when update date is in the past")
    void testUpdateScreening_DateInPast_ThrowsException() {
        ScreeningDTO screeningDTO = ScreeningDTO.builder()
                .movieId(1L)
                .roomId(1L)
                .screeningTime(LocalDateTime.now().minusDays(1))
                .build();

        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));

        assertThatThrownBy(() -> screeningService.updateScreening(1L, screeningDTO))
                .isInstanceOf(ScreeningDateInPastException.class);

        verify(screeningRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when movie does not exist during update")
    void testUpdateScreening_InvalidMovie_ThrowsException() {
        ScreeningDTO screeningDTO = ScreeningDTO.builder()
                .movieId(999L)
                .roomId(1L)
                .screeningTime(futureDateTime.plusDays(1))
                .build();

        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.updateScreening(1L, screeningDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Invalid movie ID: " + screeningDTO.getMovieId());

        verify(screeningRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when room does not exist during update")
    void testUpdateScreening_InvalidRoom_ThrowsException() {
        ScreeningDTO screeningDTO = ScreeningDTO.builder()
                .movieId(1L)
                .roomId(999L)
                .screeningTime(futureDateTime.plusDays(1))
                .build();

        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.updateScreening(1L, screeningDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Room Not Found");

        verify(screeningRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ScreeningOverlapException when updated screening overlaps with existing one")
    void testUpdateScreening_ScreeningOverlap_ThrowsException() {
        ScreeningDTO screeningDTO = ScreeningDTO.builder()
                .movieId(1L)
                .roomId(1L)
                .screeningTime(futureDateTime.plusDays(1))
                .build();

        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(screeningRepository.existsByRoomIdAndStartTimeBeforeAndEndTimeAfterAndIdNot(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(1L)
        )).thenReturn(true);

        assertThatThrownBy(() -> screeningService.updateScreening(1L, screeningDTO))
                .isInstanceOf(ScreeningOverlapException.class)
                .hasMessageContaining("overlaps");

        verify(screeningRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update screening with different movie and room")
    void testUpdateScreening_DifferentMovieAndRoom_Success() {
        Movie newMovie = new Movie();
        newMovie.setId(2L);
        newMovie.setDurationInMinutes(150);
        newMovie.setTitle("New Movie");
        newMovie.setGenre(MovieGenre.DRAMA);

        Room newRoom = new Room();
        newRoom.setId(2L);
        newRoom.setName("Room B");

        ScreeningDTO screeningDTO = ScreeningDTO.builder()
                .movieId(2L)
                .roomId(2L)
                .screeningTime(futureDateTime.plusDays(2))
                .build();

        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));
        when(movieRepository.findById(2L)).thenReturn(Optional.of(newMovie));
        when(roomRepository.findById(2L)).thenReturn(Optional.of(newRoom));
        when(screeningRepository.existsByRoomIdAndStartTimeBeforeAndEndTimeAfterAndIdNot(
                eq(2L),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(1L)
        )).thenReturn(false);

        screeningService.updateScreening(1L, screeningDTO);

        verify(screeningRepository).save(any(Screening.class));
    }

    @Test
    @DisplayName("Should not conflict with itself when checking for overlaps")
    void testUpdateScreening_NoSelfConflict() {
        ScreeningDTO screeningDTO = ScreeningDTO.builder()
                .movieId(1L)
                .roomId(1L)
                .screeningTime(futureDateTime)
                .build();

        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(screeningRepository.existsByRoomIdAndStartTimeBeforeAndEndTimeAfterAndIdNot(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(1L)
        )).thenReturn(false);

        screeningService.updateScreening(1L, screeningDTO);

        verify(screeningRepository).save(any(Screening.class));
        verify(screeningRepository).existsByRoomIdAndStartTimeBeforeAndEndTimeAfterAndIdNot(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(1L)
        );
    }
// ==================== DELETE SCREENING TESTS ====================

    @Test
    @DisplayName("Should delete screening successfully when it exists")
    void testDeleteScreening_Success() {
        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));

        screeningService.deleteScreening(1L);

        verify(screeningRepository).findById(1L);
        verify(screeningRepository).delete(testScreening);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when screening does not exist")
    void testDeleteScreening_NotFound_ThrowsException() {
        when(screeningRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.deleteScreening(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");

        verify(screeningRepository).findById(99L);
        verify(screeningRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should delete screening even if it has associated tickets")
    void testDeleteScreening_WithTickets_Success() {
        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));

        screeningService.deleteScreening(1L);

        verify(screeningRepository).delete(testScreening);
    }

    @Test
    @DisplayName("Should delete screening even if it has temporary reservations")
    void testDeleteScreening_WithTemporaryReservations_Success() {
        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));

        screeningService.deleteScreening(1L);

        verify(screeningRepository).delete(testScreening);
    }

    @Test
    @DisplayName("Should verify correct screening instance is deleted")
    void testDeleteScreening_VerifyCorrectInstance() {
        Screening anotherScreening = new Screening();
        anotherScreening.setId(2L);
        anotherScreening.setMovie(testMovie);
        anotherScreening.setRoom(testRoom);
        anotherScreening.setStartTime(futureDateTime.plusDays(1));
        anotherScreening.setEndTime(futureDateTime.plusDays(1).plusMinutes(120));

        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));

        screeningService.deleteScreening(1L);

        verify(screeningRepository).delete(testScreening);
        verify(screeningRepository, never()).delete(anotherScreening);
    }
// ==================== COLLISION DETECTION BY MOVIE TESTS ====================

    @Test
    @DisplayName("Should find colliding screenings for movie in given room and time")
    void testGetCollidingScreeningsByMovie_Success() {
        List<Screening> collidingScreenings = List.of(testScreening);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(screeningRepository.findByRoomIdAndStartTimeBeforeAndEndTimeAfter(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(collidingScreenings);

        List<CollisionDTO> result = screeningService.getCollidingScreeningsByMovie(1L, 1L, futureDateTime);

        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .extracting("movieTitle", "roomName")
                .containsExactly(
                        tuple("Test Movie", "Room A")
                );

        verify(movieRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when movie does not exist")
    void testGetCollidingScreeningsByMovie_InvalidMovie_ThrowsException() {
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.getCollidingScreeningsByMovie(1L, 999L, futureDateTime))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Movie not found");

        verify(screeningRepository, never()).findByRoomIdAndStartTimeBeforeAndEndTimeAfter(
                anyLong(),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("Should return empty list when no colliding screenings exist")
    void testGetCollidingScreeningsByMovie_NoCollisions_ReturnsEmptyList() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(screeningRepository.findByRoomIdAndStartTimeBeforeAndEndTimeAfter(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of());

        List<CollisionDTO> result = screeningService.getCollidingScreeningsByMovie(1L, 1L, futureDateTime);

        assertThat(result)
                .isNotNull()
                .isEmpty();

        verify(movieRepository).findById(1L);
    }

    @Test
    @DisplayName("Should calculate correct end time based on movie duration")
    void testGetCollidingScreeningsByMovie_CalculatesEndTime() {
        Movie movieWith150Minutes = new Movie();
        movieWith150Minutes.setId(2L);
        movieWith150Minutes.setDurationInMinutes(150);
        movieWith150Minutes.setTitle("Longer Movie");
        movieWith150Minutes.setGenre(MovieGenre.DRAMA);

        when(movieRepository.findById(2L)).thenReturn(Optional.of(movieWith150Minutes));
        when(screeningRepository.findByRoomIdAndStartTimeBeforeAndEndTimeAfter(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of());

        screeningService.getCollidingScreeningsByMovie(1L, 2L, futureDateTime);

        verify(screeningRepository).findByRoomIdAndStartTimeBeforeAndEndTimeAfter(
                eq(1L),
                argThat(endTime -> endTime.isEqual(futureDateTime.plusMinutes(150))),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("Should find multiple colliding screenings for same movie")
    void testGetCollidingScreeningsByMovie_MultipleCollisions() {
        Screening anotherScreening = new Screening();
        anotherScreening.setId(2L);
        anotherScreening.setMovie(testMovie);
        anotherScreening.setRoom(testRoom);
        anotherScreening.setStartTime(futureDateTime.plusMinutes(150));
        anotherScreening.setEndTime(futureDateTime.plusMinutes(270));

        List<Screening> collidingScreenings = List.of(testScreening, anotherScreening);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(screeningRepository.findByRoomIdAndStartTimeBeforeAndEndTimeAfter(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(collidingScreenings);

        List<CollisionDTO> result = screeningService.getCollidingScreeningsByMovie(1L, 1L, futureDateTime);

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .extracting("movieTitle")
                .containsExactly("Test Movie", "Test Movie");
    }

    @Test
    @DisplayName("Should include collision end times in result")
    void testGetCollidingScreeningsByMovie_IncludesEndTimes() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(screeningRepository.findByRoomIdAndStartTimeBeforeAndEndTimeAfter(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(testScreening));

        List<CollisionDTO> result = screeningService.getCollidingScreeningsByMovie(1L, 1L, futureDateTime);

        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .extracting("endTime")
                .containsExactly(futureDateTime.plusMinutes(120));
    }

    @Test
    @DisplayName("Should work correctly for different rooms")
    void testGetCollidingScreeningsByMovie_DifferentRooms() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(screeningRepository.findByRoomIdAndStartTimeBeforeAndEndTimeAfter(
                eq(2L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of());

        List<CollisionDTO> result = screeningService.getCollidingScreeningsByMovie(2L, 1L, futureDateTime);

        assertThat(result).isEmpty();

        verify(screeningRepository).findByRoomIdAndStartTimeBeforeAndEndTimeAfter(
                eq(2L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

}