package io.github.jakubpakula1.cinema.controller.view.admin;

import io.github.jakubpakula1.cinema.dto.ScreeningDTO;
import io.github.jakubpakula1.cinema.service.MovieService;
import io.github.jakubpakula1.cinema.service.RoomService;
import io.github.jakubpakula1.cinema.service.ScreeningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/screenings")
@RequiredArgsConstructor
public class AdminScreeningViewController {
    private final ScreeningService screeningService;
    private final MovieService movieService;
    private final RoomService roomService;

    @GetMapping("/add")
    public String showScreeningForm(Model model) {
        model.addAttribute("screening", new ScreeningDTO());
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "screening/admin/screening-form";
    }

    @PostMapping("/add")
    public String addScreening(@ModelAttribute ScreeningDTO screeningDTO) {
            screeningService.createScreening(screeningDTO);
            return "redirect:/screenings";
    }
}
