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
import org.springframework.web.bind.annotation.*;

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
        if (movieFormDTO.getPosterImageFile() == null || movieFormDTO.getPosterImageFile().isEmpty()) {
            bindingResult.rejectValue("posterImageFile", "error.movieFormDTO", "Poster image is required when creating a movie.");
        }
        if (movieFormDTO.getBackdropImageFile() == null || movieFormDTO.getBackdropImageFile().isEmpty()) {
            bindingResult.rejectValue("backdropImageFile", "error.movieFormDTO", "Backdrop image is required when creating a movie.");
        }
        if (bindingResult.hasErrors()) {
            return "movies/admin/add-movie";
        }
        movieService.addMovie(movieFormDTO);
        return "redirect:/admin/movies";
    }
    @GetMapping("/edit/{id}")
    public String showEditMovieForm(@PathVariable Long id, Model model) {
        MovieFormDTO movieFormDTO = movieService.getMovieFormDTOById(id);

        model.addAttribute("movieFormDTO", movieFormDTO);
        return "movies/admin/add-movie";
    }

    @PutMapping("/edit/{id}")
    public String handleEditMovieForm(@PathVariable Long id, @Valid @ModelAttribute MovieFormDTO movieFormDTO, BindingResult bindingResult) throws IOException {
        if (bindingResult.hasErrors()) {
            return "movies/admin/add-movie";
        }
        movieService.updateMovie(id, movieFormDTO);
        return "redirect:/admin/movies";
    }

    @PostMapping("/delete/{id}")
    public String handleDeleteMovie(@PathVariable Long id) throws IOException {
        movieService.deleteMovie(id);
        return "redirect:/admin/movies";
    }

}
