package io.github.jakubpakula1.cinema.controller.view;

import io.github.jakubpakula1.cinema.model.Movie;
import io.github.jakubpakula1.cinema.service.MovieService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/movies")
public class MovieViewController {

    private final MovieService movieService;

    public MovieViewController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public String showMovieList(Model model) {
        List<Movie> movies = movieService.getAllMovies();

        model.addAttribute("movies", movies);

        return "movies/movie-list";
    }
}
