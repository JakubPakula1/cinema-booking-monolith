package io.github.jakubpakula1.cinema.controller.view.admin;

import io.github.jakubpakula1.cinema.dto.ScreeningDTO;
import io.github.jakubpakula1.cinema.exception.ScreeningDateInPastException;
import io.github.jakubpakula1.cinema.exception.ScreeningOverlapException;
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
        model.addAttribute("action", "add");
        model.addAttribute("screening", new ScreeningDTO());
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "screening/admin/screening-form";
    }

    @PostMapping("/add")
    public String addScreening(@ModelAttribute ScreeningDTO screeningDTO, Model model) {
        try{
            screeningService.createScreening(screeningDTO);
            return "redirect:/screenings";
        } catch (ScreeningOverlapException | ScreeningDateInPastException e) {
            model.addAttribute("errorMessage", e.getMessage());

            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("rooms", roomService.getAllRooms());
            model.addAttribute("screening", screeningDTO);
            return "screening/admin/screening-form";
        }
    }
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        ScreeningDTO screening = screeningService.getScreeningById(id);
        model.addAttribute("action", "edit");
        model.addAttribute("screening", screening);
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("rooms", roomService.getAllRooms());

        return "screening/admin/screening-form";
    }

    @PutMapping("/edit/{id}")
    public String editScreening(@PathVariable Long id, @ModelAttribute ScreeningDTO screeningDTO, Model model) {
        try {
            screeningService.updateScreening(id, screeningDTO);
            return "redirect:/screenings";
        } catch (ScreeningOverlapException | ScreeningDateInPastException e) {
            model.addAttribute("errorMessage", e.getMessage());

            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("rooms", roomService.getAllRooms());
            model.addAttribute("screening", screeningService.getScreeningById(id));
            return "screening/admin/screening-form";
        }
    }

    @DeleteMapping("/delete/{id}")
    public String deleteScreening(@PathVariable Long id) {
        screeningService.deleteScreening(id);

        return "redirect:/screenings";
    }
}
