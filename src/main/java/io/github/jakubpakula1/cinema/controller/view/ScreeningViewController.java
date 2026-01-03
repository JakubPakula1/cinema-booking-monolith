package io.github.jakubpakula1.cinema.controller.view;

import io.github.jakubpakula1.cinema.dto.booking.BookingRequestDTO;
import io.github.jakubpakula1.cinema.dto.booking.BookingSummaryDTO;
import io.github.jakubpakula1.cinema.dto.screening.ScreeningListDTO;
import io.github.jakubpakula1.cinema.exception.EmptyCartException;
import io.github.jakubpakula1.cinema.exception.ReservationExpiredException;
import io.github.jakubpakula1.cinema.model.Order;
import io.github.jakubpakula1.cinema.model.Screening;
import io.github.jakubpakula1.cinema.model.User;
import io.github.jakubpakula1.cinema.security.CustomUserDetails;
import io.github.jakubpakula1.cinema.service.BookingService;
import io.github.jakubpakula1.cinema.service.ReservationService;
import io.github.jakubpakula1.cinema.service.ScreeningService;
import io.github.jakubpakula1.cinema.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/screenings")
@RequiredArgsConstructor
public class ScreeningViewController {
    private final ScreeningService screeningService;
    private final ReservationService reservationService;
    private final BookingService bookingService;
    private final UserService userService;

    @GetMapping()
    public String getAllScreenings(Model model) {
        List<ScreeningListDTO> screenings = screeningService.getAllScreeningsForList();
        model.addAttribute("screenings", screenings);
        return "screening/admin/list";
    }

    @GetMapping("/booking/{screeningId}")
    public String showRoom(@PathVariable Long screeningId, Model model) {
        Screening screening = screeningService.getScreeningEntityById(screeningId);

        model.addAttribute("seats", screeningService.getSeatsWithStatus(screeningId));
        model.addAttribute("movie", screening.getMovie());
        model.addAttribute("screeningTime", screening.getStartTime());
        model.addAttribute("screeningId", screeningId);

        return "screening/seat-view";
    }

    @GetMapping("booking/summary")
    public String summary(Model model, Principal principal) {
        try {
            BookingSummaryDTO summary = reservationService.prepareSummary(principal.getName());

            model.addAttribute("summary", summary);

            return "screening/booking-summary";

        } catch (EmptyCartException e) {
            return "redirect:/repertoire";
        }
    }

    @PostMapping("booking/process")
    public String processBooking(@ModelAttribute BookingRequestDTO form, @AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByEmail(userDetails.getUsername());

            Long orderId = bookingService.finalizeOrder(form, user);

            return "redirect:/screenings/booking/success/" + orderId;
        } catch (ReservationExpiredException e) {
            redirectAttributes.addFlashAttribute("error", "Time to complete the reservation has expired. Please try again.");
            return "redirect:/repertoire";
        } catch (EmptyCartException e) {
            redirectAttributes.addFlashAttribute("error", "Your reservation cart is empty. Please select seats to book.");
            return "redirect:/repertoire";
        }
    }

    @GetMapping("booking/success/{orderId}")
    public String successPage(@PathVariable Long orderId, Model model, @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        Order order = bookingService.getOrderSummary(orderId, userDetails.getUsername());

        model.addAttribute("order", order);
        return "screening/booking-success";
    }
}
