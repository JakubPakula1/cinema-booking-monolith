package io.github.jakubpakula1.cinema.controller.view.admin;

import io.github.jakubpakula1.cinema.dao.SalesStatsDAO;
import io.github.jakubpakula1.cinema.dto.raport.DailySalesStatsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("admin/reports")
@RequiredArgsConstructor
public class ReportController {

    private final SalesStatsDAO salesStatsDAO;

    @GetMapping("/sales")
    public String showSalesReport(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        salesStatsDAO.logReportAccess(userDetails.getUsername());
        List<DailySalesStatsDTO> statsDTOS = salesStatsDAO.getDailySalesStats();
        model.addAttribute("stats", statsDTOS);
        return "report/sales-report";
    }
}
