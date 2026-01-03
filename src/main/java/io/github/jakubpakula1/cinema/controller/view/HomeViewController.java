package io.github.jakubpakula1.cinema.controller.view;

import io.github.jakubpakula1.cinema.repository.projection.MovieCarouselDTO;
import io.github.jakubpakula1.cinema.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeViewController {
    private final MovieService movieService;

    @GetMapping("/")
    public String showHomePage(Model model) {
        List<MovieCarouselDTO> latestMovies = movieService.getLatestMovies(3);
        model.addAttribute("latestMovies", latestMovies);
        return "index";
    }
}
