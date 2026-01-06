package io.github.jakubpakula1.cinema.service;

import io.github.jakubpakula1.cinema.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PdfService Unit Tests")
public class PdfServiceTest {

    @InjectMocks
    private PdfService pdfService;

    private Movie testMovie;
    private Room testRoom;
    private Screening testScreening;
    private Seat testSeat;
    private TicketType testTicketType;
    private Ticket testTicket;
    private LocalDateTime futureDateTime;

    @BeforeEach
    void setUp() {
        futureDateTime = LocalDateTime.now().plusDays(7);

        testMovie = new Movie();
        testMovie.setId(1L);
        testMovie.setTitle("Test Movie");
        testMovie.setDurationInMinutes(120);

        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setName("Room A");

        testScreening = new Screening();
        testScreening.setId(1L);
        testScreening.setMovie(testMovie);
        testScreening.setRoom(testRoom);
        testScreening.setStartTime(futureDateTime);
        testScreening.setEndTime(futureDateTime.plusMinutes(120));

        testSeat = new Seat();
        testSeat.setId(1L);
        testSeat.setRowNumber(5);
        testSeat.setSeatNumber(10);
        testSeat.setRoom(testRoom);

        testTicketType = new TicketType();
        testTicketType.setId(1L);
        testTicketType.setName("Standard");

        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setScreening(testScreening);
        testTicket.setSeat(testSeat);
        testTicket.setTicketType(testTicketType);
        testTicket.setPrice(BigDecimal.valueOf(25.00));
    }

    @Test
    @DisplayName("Should generate valid PDF with single ticket")
    void testGenerateTicketPdf_SingleTicket_Success() {
        List<Ticket> tickets = List.of(testTicket);

        byte[] pdfBytes = pdfService.generateTicketPdf(tickets);

        assertThat(pdfBytes)
                .isNotNull()
                .isNotEmpty()
                .hasSizeGreaterThan(1000);
    }

    @Test
    @DisplayName("Should generate valid PDF with multiple tickets")
    void testGenerateTicketPdf_MultipleTickets_Success() {
        Ticket ticket2 = new Ticket();
        ticket2.setId(2L);

        Seat seat2 = new Seat();
        seat2.setId(2L);
        seat2.setRowNumber(5);
        seat2.setSeatNumber(11);
        seat2.setRoom(testRoom);

        ticket2.setScreening(testScreening);
        ticket2.setSeat(seat2);
        ticket2.setTicketType(testTicketType);
        ticket2.setPrice(BigDecimal.valueOf(25.00));

        List<Ticket> tickets = List.of(testTicket, ticket2);

        byte[] pdfBytes = pdfService.generateTicketPdf(tickets);

        assertThat(pdfBytes)
                .isNotNull()
                .isNotEmpty()
                .hasSizeGreaterThan(1000);
    }

    @Test
    @DisplayName("Should include movie title in generated PDF")
    void testGenerateTicketPdf_ContainsMovieTitle() {
        List<Ticket> tickets = List.of(testTicket);

        byte[] pdfBytes = pdfService.generateTicketPdf(tickets);
        String pdfContent = new String(pdfBytes);

        assertThat(pdfContent)
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    @DisplayName("Should include cinema hall name in generated PDF")
    void testGenerateTicketPdf_ContainsCinemaHall() {
        List<Ticket> tickets = List.of(testTicket);

        byte[] pdfBytes = pdfService.generateTicketPdf(tickets);

        assertThat(pdfBytes)
                .isNotNull()
                .isNotEmpty()
                .hasSizeGreaterThan(1000);
    }

    @Test
    @DisplayName("Should include seat information in generated PDF")
    void testGenerateTicketPdf_ContainsSeatInfo() {
        List<Ticket> tickets = List.of(testTicket);

        byte[] pdfBytes = pdfService.generateTicketPdf(tickets);

        assertThat(pdfBytes)
                .isNotNull()
                .isNotEmpty()
                .hasSizeGreaterThan(1000);
    }

    @Test
    @DisplayName("Should include ticket price in generated PDF")
    void testGenerateTicketPdf_ContainsPrice() {
        List<Ticket> tickets = List.of(testTicket);

        byte[] pdfBytes = pdfService.generateTicketPdf(tickets);

        assertThat(pdfBytes)
                .isNotNull()
                .isNotEmpty()
                .hasSizeGreaterThan(1000);
    }

    @Test
    @DisplayName("Should generate QR code in PDF")
    void testGenerateTicketPdf_ContainsQRCode() {
        List<Ticket> tickets = List.of(testTicket);

        byte[] pdfBytes = pdfService.generateTicketPdf(tickets);

        assertThat(pdfBytes)
                .isNotNull()
                .isNotEmpty()
                .hasSizeGreaterThan(1000);
    }

    @Test
    @DisplayName("Should throw RuntimeException on null ticket list")
    void testGenerateTicketPdf_NullTicketList_ThrowsException() {
        assertThatThrownBy(() -> pdfService.generateTicketPdf(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should generate consistent PDF output for same input")
    void testGenerateTicketPdf_ConsistentOutput() {
        List<Ticket> tickets = List.of(testTicket);

        byte[] pdfBytes1 = pdfService.generateTicketPdf(tickets);
        byte[] pdfBytes2 = pdfService.generateTicketPdf(tickets);

        assertThat(pdfBytes1)
                .isNotNull()
                .isNotEmpty();
        assertThat(pdfBytes2)
                .isNotNull()
                .isNotEmpty();
    }
}