package io.github.jakubpakula1.cinema.controller.view;

import io.github.jakubpakula1.cinema.dto.BookingSummaryDTO;
import io.github.jakubpakula1.cinema.dto.ScreeningListDTO;
import io.github.jakubpakula1.cinema.exception.EmptyCartException;
import io.github.jakubpakula1.cinema.model.Screening;
import io.github.jakubpakula1.cinema.service.ReservationService;
import io.github.jakubpakula1.cinema.service.ScreeningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/screenings")
@RequiredArgsConstructor
public class ScreeningViewController {
    private final ScreeningService screeningService;
    private final ReservationService reservationService;

    @GetMapping()
    public String getAllScreenings(Model model) {
        List<ScreeningListDTO> screenings = screeningService.getAllScreeningsForList();
        model.addAttribute("screenings", screenings);
        return "screening/admin/list";
    }

    @GetMapping("/booking/{screeningId}")
    public String showRoom(@PathVariable Long screeningId, Model model) {
        Screening screening = screeningService.getScreeningEntityById(screeningId);

        model.addAttribute("seats", screeningService.getSeatsWithStatus(screeningId));
        model.addAttribute("movie", screening.getMovie());
        model.addAttribute("screeningTime", screening.getStartTime());
        model.addAttribute("screeningId", screeningId);

        return "screening/seat-view";
    }

    @GetMapping("booking/summary")
    public String summary(Model model, Principal principal) {
        try {
            BookingSummaryDTO summary = reservationService.prepareSummary(principal.getName());

            model.addAttribute("summary", summary);

            return "screening/booking-summary";

        } catch (EmptyCartException e) {
            return "redirect:/repertoire";
        }
    }

}
