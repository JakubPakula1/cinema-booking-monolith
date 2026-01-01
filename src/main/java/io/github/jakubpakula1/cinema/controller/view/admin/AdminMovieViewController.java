package io.github.jakubpakula1.cinema.controller.view.admin;

import io.github.jakubpakula1.cinema.dto.MovieFormDTO;
import io.github.jakubpakula1.cinema.repository.projection.MovieListView;
import io.github.jakubpakula1.cinema.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/admin/movies")
@RequiredArgsConstructor
public class AdminMovieViewController {
    private final MovieService movieService;

    @GetMapping
    public String showAdminMovieList(Model model) {
        Page<MovieListView> movies = movieService.getAllMoviesProjected(0, 100);
        model.addAttribute("movies", movies.getContent());
        return "movies/admin/movie-list";
    }

    @GetMapping("/add")
    public String showAddMovieForm(Model model) {
        model.addAttribute("movieFormDTO", new MovieFormDTO());
        return "movies/admin/add-movie";
    }

    @PostMapping("/add")
    public String handleAddMovieForm(@Valid @ModelAttribute MovieFormDTO movieFormDTO, BindingResult bindingResult) throws IOException {
        if (bindingResult.hasErrors()) {
            return "movies/admin/add-movie";
        }
        System.out.println("Received movie form: " + movieFormDTO);
        movieService.addMovie(movieFormDTO);
        return "redirect:/admin/movies";
    }
}
