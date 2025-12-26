package io.github.jakubpakula1.cinema.controller.view;

import io.github.jakubpakula1.cinema.dto.UserDTO;
import io.github.jakubpakula1.cinema.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthViewController {
    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new UserDTO());
        return "register";
    }
    @PostMapping("/register")
    public String handleRegistration(UserDTO userDTO) {
        userService.registerUser(userDTO);
        return "redirect:/login";
    }

}
