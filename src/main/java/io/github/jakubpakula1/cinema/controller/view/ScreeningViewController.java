package io.github.jakubpakula1.cinema.controller.view;

import io.github.jakubpakula1.cinema.dto.ScreeningListDTO;
import io.github.jakubpakula1.cinema.service.ScreeningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/screenings")
@RequiredArgsConstructor
public class ScreeningViewController {
    private final ScreeningService screeningService;

    @GetMapping()
    public String getAllScreenings(Model model) {
        List<ScreeningListDTO> screenings = screeningService.getAllScreeningsForList();
        model.addAttribute("screenings", screenings);
        return "screening/list";
    }

    @GetMapping("/{screeningId}")
    public String showRoom(@PathVariable Long screeningId, Model model) {
        model.addAttribute("seats", screeningService.getSeatsWithStatus(screeningId));
        model.addAttribute("screeningId", screeningId);
        return "screening/seat-view";
    }
}
