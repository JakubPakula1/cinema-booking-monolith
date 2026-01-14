package io.github.jakubpakula1.cinema.dao;

import io.github.jakubpakula1.cinema.dto.raport.DailySalesStatsDTO;
import io.github.jakubpakula1.cinema.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SalesStatsDAO.class)
class SalesStatsDAOTest {

    @Autowired
    private SalesStatsDAO salesStatsDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should calculate daily sales stats correctly")
    void testGetDailySalesStats() {
        // given

        User user = new User();
        user.setEmail("test@test.pl");
        user.setPassword("pass");
        entityManager.persist(user);

        Room room = new Room();
        room.setName("Sala 1");
        entityManager.persist(room);

        Seat seat1 = new Seat();
        seat1.setRoom(room);
        seat1.setRowNumber(1);
        seat1.setSeatNumber(1);
        entityManager.persist(seat1);

        Seat seat2 = new Seat();
        seat2.setRoom(room);
        seat2.setRowNumber(1);
        seat2.setSeatNumber(2);
        entityManager.persist(seat2);

        Movie movie = new Movie();
        movie.setTitle("Test Movie");
        movie.setDurationInMinutes(120);
        entityManager.persist(movie);

        Screening screening = new Screening();
        screening.setMovie(movie);
        screening.setRoom(room);
        screening.setStartTime(LocalDateTime.now().plusHours(2));
        screening.setEndTime(screening.getStartTime().plusMinutes(movie.getDurationInMinutes()));
        screening.setTickets(new ArrayList<>());
        entityManager.persist(screening);

        TicketType normalType = new TicketType();
        normalType.setName("Normalny");
        normalType.setPrice(new BigDecimal("20.00"));
        entityManager.persist(normalType);

        Order order = new Order();
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalCost(BigDecimal.ZERO);
        entityManager.persist(order);

        // when

        createTicket(order, screening, seat1, normalType);
        createTicket(order, screening, seat2, normalType);

        entityManager.flush();

        // then
        List<DailySalesStatsDTO> stats = salesStatsDAO.getDailySalesStats();

        assertThat(stats).isNotEmpty();
        DailySalesStatsDTO todayStats = stats.getFirst();

        assertThat(todayStats.getTicketsSold()).isEqualTo(2); // 2 bilety
        assertThat(todayStats.getTotalRevenue()).isEqualByComparingTo("40.00"); // 2 * 20.00
    }

    @Test
    @DisplayName("Should insert a log entry")
    void testLogReportAccess() {
        // when
        salesStatsDAO.logReportAccess("admin_user");

        // then
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM report_logs WHERE username = 'admin_user'", Integer.class);

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Should delete old logs")
    void testClearLogsOlderThan() {
        // given
        jdbcTemplate.update("INSERT INTO report_logs (username, accessed_at) VALUES (?, ?)",
                "old_user", LocalDateTime.now().minusDays(40));

        jdbcTemplate.update("INSERT INTO report_logs (username, accessed_at) VALUES (?, ?)",
                "new_user", LocalDateTime.now());

        // when
        int deletedCount = salesStatsDAO.clearLogsOlderThan(30);
        Integer remaining = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM report_logs", Integer.class);

        // then
        assertThat(deletedCount)
                .isEqualTo(1)
                .isEqualTo(remaining);
    }

    @Test
    @DisplayName("Should update username to ANONYMOUS")
    void testAnonymizeLogs() {
        // given
        salesStatsDAO.logReportAccess("john_doe");

        // when
        salesStatsDAO.anonymizeLogsForUser("john_doe");

        // then
        String newName = jdbcTemplate.queryForObject(
                "SELECT username FROM report_logs LIMIT 1", String.class);

        assertThat(newName).isEqualTo("ANONYMOUS");
    }

    private void createTicket(Order order, Screening screening, Seat seat, TicketType ticketType) {
        Ticket ticket = new Ticket();

        ticket.setOrder(order);
        ticket.setScreening(screening);
        ticket.setSeat(seat);
        ticket.setTicketType(ticketType);

        ticket.setPrice(ticketType.getPrice());

        entityManager.persist(ticket);
    }
}