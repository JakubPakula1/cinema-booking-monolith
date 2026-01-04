package io.github.jakubpakula1.cinema.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import io.github.jakubpakula1.cinema.model.Screening;
import io.github.jakubpakula1.cinema.model.Ticket;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfService {

    public byte[] generateTicketPdf(List<Ticket> tickets) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.WHITE);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.DARK_GRAY);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);

            Ticket sampleTicket = tickets.getFirst();
            Screening screening = sampleTicket.getScreening();

            // Header with background
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            PdfPCell headerCell = new PdfPCell(new Phrase("🎬 CINEMA TICKET 🎬", titleFont));
            headerCell.setBackgroundColor(new Color(44, 62, 80));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setPadding(15);
            headerTable.addCell(headerCell);
            document.add(headerTable);

            document.add(new Paragraph(" "));

            // Movie Info Section
            document.add(new Paragraph("SCREENING INFORMATION", subtitleFont));
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10);

            addRow(infoTable, "Movie:", screening.getMovie().getTitle(), labelFont, valueFont);
            addRow(infoTable, "Date & Time:", screening.getStartTime().toString(), labelFont, valueFont);
            addRow(infoTable, "Cinema Hall:", screening.getRoom().getName(), labelFont, valueFont);

            document.add(infoTable);
            document.add(new Paragraph(" "));

            // Seats Section
            document.add(new Paragraph("RESERVED SEATS", subtitleFont));
            PdfPTable seatsTable = new PdfPTable(4);
            seatsTable.setWidthPercentage(100);
            seatsTable.setSpacingBefore(10);

            // Header row
            String[] headers = {"Row", "Seat", "Type", "Price"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, labelFont));
                cell.setBackgroundColor(new Color(189, 195, 199));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                seatsTable.addCell(cell);
            }

            // Seat rows
            for (Ticket ticket : tickets) {
                seatsTable.addCell(createCenteredCell(String.valueOf(ticket.getSeat().getRowNumber()), valueFont));
                seatsTable.addCell(createCenteredCell(String.valueOf(ticket.getSeat().getSeatNumber()), valueFont));
                seatsTable.addCell(createCenteredCell(ticket.getTicketType().getName(), valueFont));
                seatsTable.addCell(createCenteredCell(ticket.getPrice() + " PLN", valueFont));
            }

            document.add(seatsTable);
            document.add(new Paragraph(" "));

            // QR Code Section
            try {
                byte[] qrCode = generateQRCode(generateTicketData(sampleTicket, screening));
                Image qrImage = Image.getInstance(qrCode);
                qrImage.scaleToFit(150, 150);

                PdfPTable qrTable = new PdfPTable(1);
                qrTable.setWidthPercentage(100);
                PdfPCell qrCell = new PdfPCell(qrImage);
                qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                qrCell.setBorder(0);
                qrCell.setPadding(10);
                qrTable.addCell(qrCell);
                document.add(qrTable);
            } catch (Exception e) {
                document.add(new Paragraph("QR Code generation failed", valueFont));
            }

            document.add(new Paragraph(" "));

            // Footer
            PdfPTable footerTable = new PdfPTable(1);
            footerTable.setWidthPercentage(100);
            PdfPCell footerCell = new PdfPCell(new Phrase("Thank you for your purchase! Enjoy the movie! 🍿", valueFont));
            footerCell.setBackgroundColor(new Color(236, 240, 241));
            footerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            footerCell.setPadding(10);
            footerTable.addCell(footerCell);
            document.add(footerTable);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(8);
        labelCell.setBackgroundColor(new Color(236, 240, 241));
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(8);
        table.addCell(valueCell);
    }

    private PdfPCell createCenteredCell(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        return cell;
    }

    private byte[] generateQRCode(String data) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 200, 200);
        ByteArrayOutputStream qrOut = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", qrOut);
        return qrOut.toByteArray();
    }

    private String generateTicketData(Ticket ticket, Screening screening) {
        return String.format("CINEMA_TICKET|Movie:%s|Date:%s|Seat:%d-%d|Type:%s",
                screening.getMovie().getTitle(),
                screening.getStartTime(),
                ticket.getSeat().getRowNumber(),
                ticket.getSeat().getSeatNumber(),
                ticket.getTicketType().getName());
    }
}