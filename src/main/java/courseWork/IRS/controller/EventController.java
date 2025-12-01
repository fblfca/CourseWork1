package courseWork.IRS.controller;

import courseWork.IRS.model.Event;
import courseWork.IRS.model.EventBooking;
import courseWork.IRS.repository.EventBookingRepository;
import courseWork.IRS.repository.EventRepository;
import courseWork.IRS.repository.RoleRepository;
import courseWork.IRS.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/events")
public class EventController {

    @Autowired private EventRepository eventRepository;
    @Autowired private EventBookingRepository bookingRepository;
    @Autowired private RoleRepository roleRepository;

    @GetMapping
    public String events(@RequestParam(required = false) String title,
                         @RequestParam(required = false) BigDecimal priceFrom,
                         @RequestParam(required = false) BigDecimal priceTo,
                         @RequestParam(required = false) LocalDate date, // ДОБАВЛЕНО для фильтрации
                         Model model,
                         Authentication auth) {

        // Начальные и конечные границы для времени:
        LocalDateTime fromTime = LocalDateTime.of(date != null ? date : LocalDate.now(), LocalTime.MIN);
        LocalDateTime toTime = LocalDateTime.of(date != null ? date : LocalDate.now().plusYears(1), LocalTime.MAX);

        // Если дата не указана, ищем события в широком диапазоне (например, ближайшие 3 месяца)
        if (date == null) {
            fromTime = LocalDateTime.now().minusDays(30);
            toTime = LocalDateTime.now().plusMonths(3);
        }

        // Фильтры по умолчанию
        String finalTitle = title != null && !title.trim().isEmpty() ? title : "";
        BigDecimal finalPriceFrom = priceFrom != null ? priceFrom : BigDecimal.ZERO;
        BigDecimal finalPriceTo = priceTo != null ? priceTo : new BigDecimal("999999.00");

        // ИЗМЕНЕНИЕ: Используем объединенный метод репозитория для фильтрации на уровне БД
        List<Event> events = eventRepository.findByTitleContainingIgnoreCaseAndPriceBetweenAndStartTimeBetween(
                finalTitle,
                finalPriceFrom,
                finalPriceTo,
                fromTime,
                toTime
        );

        model.addAttribute("events", events);
        model.addAttribute("isAdminOrWorker", isAdminOrWorker(auth));
        model.addAttribute("title", title);
        model.addAttribute("priceFrom", priceFrom);
        model.addAttribute("priceTo", priceTo);
        model.addAttribute("date", date);

        return "events";
    }

    @GetMapping("/edit")
    public String showEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "event-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID события:" + id));
        model.addAttribute("event", event);
        return "event-form";
    }

    @PostMapping("/save")
    public String saveEvent(@ModelAttribute Event event, Authentication auth) {
        if (!isAdminOrWorker(auth)) return "redirect:/events?error=access_denied";
        eventRepository.save(event);
        return "redirect:/events";
    }

    @GetMapping("/delete/{id}")
    public String deleteEvent(@PathVariable Integer id, Authentication auth) {
        if (!isAdminOrWorker(auth)) return "redirect:/events?error=access_denied";
        eventRepository.deleteById(id);
        return "redirect:/events?success=deleted";
    }

    @PostMapping("/book/{eventId}") // ИЗМЕНЕНИЕ: Используем POST
    public String bookEvent(
            @PathVariable Integer eventId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (currentUser == null) return "redirect:/login";

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return "redirect:/events?error=event_not_found";
        }

        // Проверка, что пользователь существует
        if (roleRepository.findById(currentUser.getId()).isEmpty()) {
            return "redirect:/events?error=user_not_found";
        }

        EventBooking booking = new EventBooking();
        booking.setUserId(currentUser.getId());
        booking.setEventId(eventId);
        booking.setPrice(event.getPrice() != null ? event.getPrice() : BigDecimal.ZERO);

        try {
            bookingRepository.save(booking);
        } catch (Exception e) {
            return "redirect:/events?error=" + e.getMessage();
        }

        return "redirect:/events?success=booked";
    }

    private boolean isAdminOrWorker(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_АДМИН".equals(a.getAuthority()) || "ROLE_РАБОТНИК".equals(a.getAuthority()));
    }
}