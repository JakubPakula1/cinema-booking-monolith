package io.github.jakubpakula1.cinema.repository;

import io.github.jakubpakula1.cinema.model.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    // Validate if there is a screening in the given room that overlaps with the given time range
    boolean existsByRoomIdAndStartTimeBeforeAndEndTimeAfter(Long roomId, LocalDateTime endTime, LocalDateTime startTime);

    // Find screenings in the given room that overlap with the given time range
    List<Screening> findByRoomIdAndStartTimeBeforeAndEndTimeAfter(Long roomId, LocalDateTime endTime, LocalDateTime startTime);
}
