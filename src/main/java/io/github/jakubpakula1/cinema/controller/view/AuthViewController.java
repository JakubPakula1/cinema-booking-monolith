package io.github.jakubpakula1.cinema.controller.view;

import io.github.jakubpakula1.cinema.dto.UserDTO;
import io.github.jakubpakula1.cinema.model.Order;
import io.github.jakubpakula1.cinema.model.User;
import io.github.jakubpakula1.cinema.service.BookingService;
import io.github.jakubpakula1.cinema.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthViewController {
    private final UserService userService;
    private final BookingService bookingService;

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
    @GetMapping("/profile")
    public String userProfile(Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());

        List<Order> orders = bookingService.getAllOrdersForUser(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("orders", orders);

        return "profile";
    }
}
