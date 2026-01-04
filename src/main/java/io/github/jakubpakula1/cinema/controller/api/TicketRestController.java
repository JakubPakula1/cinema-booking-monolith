package io.github.jakubpakula1.cinema.controller.api;

import io.github.jakubpakula1.cinema.model.Ticket;
import io.github.jakubpakula1.cinema.service.PdfService;
import io.github.jakubpakula1.cinema.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketRestController {
    private final PdfService pdfService;
    private final TicketService ticketService;

    @GetMapping("/{orderId}/pdf")
    public ResponseEntity<byte[]> downloadTicketsPdf(@PathVariable Long orderId) {
        List<Ticket> tickets = ticketService.getTicketsByOrderId(orderId);

        byte[] pdfBytes = pdfService.generateTicketPdf(tickets);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tickets_order_" + orderId + ".pdf\"")
                .body(pdfBytes);
    }
}
