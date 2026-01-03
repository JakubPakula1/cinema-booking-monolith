package io.github.jakubpakula1.cinema.dto.booking;

import lombok.Data;

import java.util.List;

@Data
public class BookingRequestDTO {
    private List<TicketSelectionDTO> tickets;
}
