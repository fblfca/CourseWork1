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

        // ... (логика фильтрации - не меняется)

        List<Event> events;

        BigDecimal minPrice = priceFrom != null ? priceFrom : BigDecimal.ZERO;
        BigDecimal maxPrice = priceTo != null ? priceTo : new BigDecimal("999999"); // Достаточно большое число

        LocalDateTime fromTime = null;
        LocalDateTime toTime = null;
        if (date != null) {
            fromTime = date.atStartOfDay();
            toTime = date.atTime(LocalTime.MAX);
        }

        if (title != null || date != null || priceFrom != null || priceTo != null) {
            // Если есть хотя бы один параметр фильтрации, используем объединенный метод
            events = eventRepository.findByTitleContainingIgnoreCaseAndPriceBetweenAndStartTimeBetween(
                    title != null ? title : "",
                    minPrice,
                    maxPrice,
                    fromTime != null ? fromTime : LocalDateTime.MIN,
                    toTime != null ? toTime : LocalDateTime.MAX
            );
        } else {
            // Иначе - все события
            events = eventRepository.findAll();
        }


        model.addAttribute("events", events);
        model.addAttribute("isAdminOrWorker", isAdminOrWorker(auth));
        model.addAttribute("title", title);
        model.addAttribute("priceFrom", priceFrom);
        model.addAttribute("priceTo", priceTo);
        model.addAttribute("date", date); // ДОБАВЛЕНО для сохранения значения в форме

        return "events";
    }

    @GetMapping("/edit")
    public String createEventForm(Model model, Authentication auth) {
        if (!isAdminOrWorker(auth)) return "redirect:/events?error=access_denied";

        model.addAttribute("event", new Event());
        return "event-form";
    }

    @GetMapping("/edit/{id}")
    public String editEventForm(@PathVariable Integer id, Model model, Authentication auth) {
        if (!isAdminOrWorker(auth)) return "redirect:/events?error=access_denied";

        Optional<Event> event = eventRepository.findById(id);
        if (event.isEmpty()) {
            return "redirect:/events?error=event_not_found";
        }
        model.addAttribute("event", event.get());
        return "event-form";
    }

    @PostMapping("/save")
    public String saveEvent(@ModelAttribute Event event, Authentication auth) {
        if (!isAdminOrWorker(auth)) return "redirect:/events?error=access_denied";

        eventRepository.save(event);
        return "redirect:/events?success=saved";
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

        // --- ИЗМЕНЕНИЕ ДЛЯ ПУНКТА 1: Проверка существующей брони ---
        List<EventBooking> existingBookings = bookingRepository.findByUserIdAndEventId(currentUser.getId(), eventId);
        if (!existingBookings.isEmpty()) {
            return "redirect:/events?error=already_booked";
        }
        // -----------------------------------------------------------

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