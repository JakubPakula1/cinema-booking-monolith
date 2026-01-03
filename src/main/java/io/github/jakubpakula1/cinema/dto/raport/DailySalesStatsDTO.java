package io.github.jakubpakula1.cinema.dto.raport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailySalesStatsDTO {
    private LocalDate date;
    private Long ticketsSold;
    private BigDecimal totalRevenue;
}
