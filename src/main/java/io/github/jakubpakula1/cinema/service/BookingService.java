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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final TemporaryReservationRepository temporaryReservationRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final OrderRepository orderRepository;
    private final PdfService pdfService;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public Order getOrderSummary(Long orderId, String userEmail) throws AccessDeniedException {
        log.info("Fetching order summary for orderId: {}, userEmail: {}", orderId, userEmail);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found with ID: {}", orderId);
                    return new ResourceNotFoundException("Nie znaleziono zamówienia o ID: " + orderId);
                });

        if (!order.getUser().getEmail().equals(userEmail)) {
            log.warn("Access denied: User {} attempted to access order {} owned by {}", userEmail, orderId, order.getUser().getEmail());
            throw new AccessDeniedException("Nie masz uprawnień do wyświetlenia tego zamówienia.");
        }

        log.debug("Order summary retrieved successfully for orderId: {}", orderId);
        return order;
    }

    @Transactional
    public Long finalizeOrder(BookingRequestDTO bookingRequestDTO, User user) {
        log.info("Finalizing order for user: {}", user.getId());

        List<Long> seatIds = bookingRequestDTO.getTickets().stream()
                .map(TicketSelectionDTO::getSeatId)
                .toList();

        log.debug("Checking reservations for {} seats", seatIds.size());
        List<TemporaryReservation> myReservations = temporaryReservationRepository.findByUserAndSeatIdInAndExpiresAtAfter(user, seatIds, LocalDateTime.now());

        if (myReservations.size() != seatIds.size()) {
            log.error("Reservation validation failed for user {}: expected {} seats, found {} valid reservations",
                    user.getId(), seatIds.size(), myReservations.size());
            throw new ResourceNotFoundException("Some seats are not reserved by the user or reservation has expired.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(ReservationStatus.PAID);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Ticket> ticketsToSave = new ArrayList<>();

        for (TicketSelectionDTO item : bookingRequestDTO.getTickets()) {
            TemporaryReservation reservation = myReservations.stream()
                    .filter(r -> r.getSeat().getId().equals(item.getSeatId()))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.error("Reservation not found for seat: {}", item.getSeatId());
                        return new ResourceNotFoundException("Reservation not found for seat id: " + item.getSeatId());
                    });

            TicketType type = ticketTypeRepository.findById(item.getTypeId())
                    .orElseThrow(() -> {
                        log.error("Ticket type not found with id: {}", item.getTypeId());
                        return new ResourceNotFoundException("Ticket type not found with id: " + item.getTypeId());
                    });

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

        log.info("Generating PDF for {} tickets, total amount: {}", ticketsToSave.size(), totalAmount);
        byte[] pdfBytes = pdfService.generateTicketPdf(ticketsToSave);
        orderRepository.save(order);
        log.info("Order saved with ID: {}", order.getId());

        log.debug("Sending confirmation email to: {}", user.getEmail());
        emailService.sendEmailWithAttachment(user.getEmail(), "Your Cinema Tickets", "Your cinema tickets are attached.", pdfBytes, "tickets_order_" + order.getId() + ".pdf");

        temporaryReservationRepository.deleteAll(myReservations);
        log.info("Order finalized successfully for user: {}, orderId: {}", user.getId(), order.getId());

        return order.getId();
    }

    @Transactional
    public List<Order> getAllOrdersForUser(Long id) {
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(id);
    }
}
