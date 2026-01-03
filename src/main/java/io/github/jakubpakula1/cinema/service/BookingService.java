package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.dto.booking.BookingRequestDTO;
import io.github.jakubpakula1.cinema.dto.booking.TicketSelectionDTO;
import io.github.jakubpakula1.cinema.enums.ReservationStatus;
import io.github.jakubpakula1.cinema.exception.ResourceNotFoundException;
import io.github.jakubpakula1.cinema.model.*;
import io.github.jakubpakula1.cinema.repository.OrderRepository;
import io.github.jakubpakula1.cinema.repository.TemporaryReservationRepository;
import io.github.jakubpakula1.cinema.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final TemporaryReservationRepository temporaryReservationRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public Order getOrderSummary(Long orderId, String userEmail) throws AccessDeniedException {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono zamówienia o ID: " + orderId));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("Nie masz uprawnień do wyświetlenia tego zamówienia.");
        }
        return order;
    }
    @Transactional
    public Long finalizeOrder(BookingRequestDTO bookingRequestDTO, User user){
        List<Long> seatIds = bookingRequestDTO.getTickets().stream()
                .map(TicketSelectionDTO::getSeatId)
                .toList();

        List<TemporaryReservation> myReservations = temporaryReservationRepository.findByUserAndSeatIdInAndExpiresAtAfter(user, seatIds, LocalDateTime.now());

        if (myReservations.size() != seatIds.size()){
            throw new ResourceNotFoundException("Some seats are not reserved by the user or reservation has expired.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(ReservationStatus.PAID);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Ticket> ticketsToSave = new ArrayList<>();

        for(TicketSelectionDTO item : bookingRequestDTO.getTickets()){
            TemporaryReservation reservation = myReservations.stream()
                    .filter(r -> r.getSeat().getId().equals(item.getSeatId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Reservation not found for seat id: " + item.getSeatId()));

            TicketType type = ticketTypeRepository.findById(item.getTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found with id: " + item.getTypeId()));

            Ticket ticket = new Ticket();
            ticket.setSeat(reservation.getSeat());
            ticket.setScreening(reservation.getScreening());
            ticket.setTicketType(type);
            ticket.setOrder(order);
            ticket.setPrice(type.getPrice());

            ticketsToSave.add(ticket);
            totalAmount = totalAmount.add(type.getPrice());
        }
        order.setTotalCost(totalAmount);
        order.setTickets(ticketsToSave);

        orderRepository.save(order);
        temporaryReservationRepository.deleteAll(myReservations);

        return order.getId();
    }
}
