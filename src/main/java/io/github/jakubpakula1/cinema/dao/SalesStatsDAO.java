package io.github.jakubpakula1.cinema.dao;

import io.github.jakubpakula1.cinema.dto.raport.DailySalesStatsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SalesStatsDAO {
    private final JdbcTemplate jdbcTemplate;

    public List<DailySalesStatsDTO> getDailySalesStats() {
        String sql = """
            SELECT\s
                CAST(o.created_at AS DATE) as sale_date,\s
                COUNT(t.id) as tickets_count,\s
                SUM(t.price) as total_revenue
            FROM orders o
            JOIN tickets t ON t.order_id = o.id
            GROUP BY CAST(o.created_at AS DATE)
            ORDER BY sale_date DESC
            LIMIT 30
       \s""";

        return jdbcTemplate.query(sql, new SalesRowMapper());
    }

    public void logReportAccess(String username) {
        String sql = "INSERT INTO report_logs (username, accessed_at) VALUES (?, ?)";
        jdbcTemplate.update(sql, username, LocalDateTime.now());
    }
    public int clearLogsOlderThan(int days) {
        String sql = "DELETE FROM report_logs WHERE accessed_at < ?";
        return jdbcTemplate.update(sql, LocalDateTime.now().minusDays(days));
    }

    public void anonymizeLogsForUser(String username) {
        String sql = "UPDATE report_logs SET username = 'ANONYMOUS' WHERE username = ?";
        jdbcTemplate.update(sql, username);
    }
    private static class SalesRowMapper implements RowMapper<DailySalesStatsDTO> {
        @Override
        public DailySalesStatsDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new DailySalesStatsDTO(
                    rs.getDate("sale_date").toLocalDate(),
                    rs.getLong("tickets_count"),
                    rs.getBigDecimal("total_revenue")
            );
        }
    }
}
