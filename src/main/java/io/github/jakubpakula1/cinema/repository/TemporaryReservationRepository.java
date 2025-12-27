package io.github.jakubpakula1.cinema.repository;

import io.github.jakubpakula1.cinema.dto.SeatUserLockDTO;
import io.github.jakubpakula1.cinema.model.TemporaryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TemporaryReservationRepository extends JpaRepository<TemporaryReservation, Long> {
    @Query("SELECT new io.github.jakubpakula1.cinema.dto.SeatUserLockDTO(tr.user.id,tr.seat.id) FROM TemporaryReservation tr WHERE tr.screening.id = :screeningId AND tr.expiresAt > CURRENT_TIMESTAMP")
    List<SeatUserLockDTO> findLockedSeatIdsByScreeningId(@Param("screeningId") Long screeningId);

    boolean existsBySeatIdAndScreeningIdAndExpiresAtAfter(Long seat_id, Long screening_id, LocalDateTime expiresAt);

    TemporaryReservation deleteTemporaryReservationByScreeningIdAndSeatId(Long screeningId, Long seatId);
}
