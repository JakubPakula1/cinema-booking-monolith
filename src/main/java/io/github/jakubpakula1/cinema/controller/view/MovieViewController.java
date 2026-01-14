package io.github.jakubpakula1.cinema.controller.view;

import io.github.jakubpakula1.cinema.model.Movie;
import io.github.jakubpakula1.cinema.repository.projection.MovieListViewDTO;
import io.github.jakubpakula1.cinema.service.MovieService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/movies")
public class MovieViewController {

    private final MovieService movieService;

    public MovieViewController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public String showMovieList(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "8") int size) {
        Page<MovieListViewDTO> moviePage = movieService.getAllMoviesProjected(page, size);
        model.addAttribute("moviePage", moviePage);

        return "movies/movie-list";
    }

    @GetMapping("/{movieId}")
    public String showMovieDetails(@PathVariable Long movieId, Model model) {
        Movie movie = movieService.getMovieById(movieId);
        model.addAttribute("movie", movie);
        return "movies/movie-details";
    }
}
