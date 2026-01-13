package io.github.jakubpakula1.cinema.controller.view;

import io.github.jakubpakula1.cinema.dto.screening.RepertoireMovieDTO;
import io.github.jakubpakula1.cinema.service.ScreeningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/repertoire")
@RequiredArgsConstructor
public class RepertoireViewController {
    private final ScreeningService screeningService;

    @GetMapping()
    public String showRepertoire(@RequestParam(required = false) LocalDate date, Model model) {
        if (date == null) {
            date = LocalDate.now();
        }

        List<RepertoireMovieDTO> movies = screeningService.getRepertoireForDate(date);
        model.addAttribute("movies", movies);
        model.addAttribute("selectedDate", date);
        List<LocalDate> nextDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            nextDays.add(LocalDate.now().plusDays(i));
        }
        model.addAttribute("nextDays", nextDays);

        return "repertoire";
    }
}
