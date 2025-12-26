package io.github.jakubpakula1.cinema.controller.view;

import io.github.jakubpakula1.cinema.dto.ScreeningDTO;
import io.github.jakubpakula1.cinema.dto.ScreeningListDTO;
import io.github.jakubpakula1.cinema.exception.ScreeningOverlapException;
import io.github.jakubpakula1.cinema.service.MovieService;
import io.github.jakubpakula1.cinema.service.RoomService;
import io.github.jakubpakula1.cinema.service.ScreeningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/screenings")
@RequiredArgsConstructor
public class ScreeningViewController {
    private final ScreeningService screeningService;
    private final MovieService movieService;
    private final RoomService roomService;

    @GetMapping()
    public String getAllScreenings(Model model) {
        List<ScreeningListDTO> screenings = screeningService.getAllScreeningsForList();
        model.addAttribute("screenings", screenings);
        return "screening/list";
    }

    @GetMapping("/admin/add")
    public String showScreeningForm(Model model) {
        model.addAttribute("screening", new ScreeningDTO());
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "screening/screening-form";
    }

    @PostMapping("/admin/add")
    public String addScreening(@ModelAttribute ScreeningDTO screeningDTO, Model model) {
        try{
            screeningService.createScreening(screeningDTO);
            return "redirect:/screenings";
        }catch (ScreeningOverlapException e){
            model.addAttribute("errorMessage", e.getMessage());

            model.addAttribute("screening", screeningDTO);
            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("rooms", roomService.getAllRooms());

            model.addAttribute("showConflictButton", true);

            return "screening/screening-form";
        }
    }
}
