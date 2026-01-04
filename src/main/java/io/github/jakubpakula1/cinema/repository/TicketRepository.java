package io.github.jakubpakula1.cinema.repository;

import io.github.jakubpakula1.cinema.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("SELECT t.seat.id FROM Ticket t WHERE t.screening.id = :screeningId")
    List<Long> findSoldSeatIdsByScreeningId(@Param("screeningId") Long screeningId);

    boolean existsBySeatIdAndScreeningId(Long seatId, Long screeningId);

    //  Method to find tickets by order ID
    List<Ticket> findAllByOrderId(Long orderId);
}
