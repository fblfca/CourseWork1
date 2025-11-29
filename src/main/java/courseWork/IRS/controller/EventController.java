package courseWork.IRS.controller;

import courseWork.IRS.model.Event;
import courseWork.IRS.model.EventBooking;
import courseWork.IRS.model.Role;
import courseWork.IRS.repository.EventBookingRepository;
import courseWork.IRS.repository.EventRepository;
import courseWork.IRS.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
                         Model model,
                         Authentication auth) {

        List<Event> events = eventRepository.findAll();

        if (title != null && !title.trim().isEmpty()) {
            String lower = title.toLowerCase().trim();
            events = events.stream()
                    .filter(e -> e.getTitle() != null && e.getTitle().toLowerCase().contains(lower))
                    .collect(Collectors.toList());
        }

        if (priceFrom != null && priceFrom.compareTo(BigDecimal.ZERO) > 0) {
            events = events.stream()
                    .filter(e -> e.getPrice() != null && e.getPrice().compareTo(priceFrom) >= 0)
                    .collect(Collectors.toList());
        }

        if (priceTo != null && priceTo.compareTo(BigDecimal.ZERO) > 0) {
            events = events.stream()
                    .filter(e -> e.getPrice() != null && e.getPrice().compareTo(priceTo) <= 0)
                    .collect(Collectors.toList());
        }

        boolean isAdminOrWorker = isAdminOrWorker(auth);

        model.addAttribute("events", events);
        model.addAttribute("isAdminOrWorker", isAdminOrWorker);
        model.addAttribute("title", title);
        model.addAttribute("priceFrom", priceFrom);
        model.addAttribute("priceTo", priceTo);

        return "events";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable(required = false) Integer id, Model model, Authentication auth) {
        if (!isAdminOrWorker(auth)) return "redirect:/events";

        Event event = (id == null) ? new Event() : eventRepository.findById(id).orElse(new Event());
        model.addAttribute("event", event);
        return "event-form";
    }

    @PostMapping("/save")
    public String saveEvent(@ModelAttribute Event event, Authentication auth) {
        if (!isAdminOrWorker(auth)) return "redirect:/events";
        eventRepository.save(event);
        return "redirect:/events";
    }

    @GetMapping("/delete/{id}")
    public String deleteEvent(@PathVariable Integer id, Authentication auth) {
        if (!isAdminOrWorker(auth)) return "redirect:/events";
        eventRepository.deleteById(id);
        return "redirect:/events";
    }

    @PostMapping("/book/{eventId}")
    public String bookEvent(@PathVariable Integer eventId,
                            @RequestParam Integer userId,
                            Authentication auth) {
        if (!isAdminOrWorker(auth)) return "redirect:/events";

        // Заменил orElseThrow() на старый способ с проверкой
        Event event = eventRepository.findById(eventId)
                .orElse(null);
        if (event == null) {
            return "redirect:/events?error=event_not_found";
        }

        Role user = roleRepository.findById(userId)
                .orElse(null);
        if (user == null) {
            return "redirect:/events?error=user_not_found";
        }

        EventBooking booking = new EventBooking();
        booking.setUserId(userId);
        booking.setEventId(eventId);
        booking.setPrice(event.getPrice() != null ? event.getPrice() : BigDecimal.ZERO);

        bookingRepository.save(booking);
        return "redirect:/events?success=booked";
    }

    private boolean isAdminOrWorker(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_АДМИН".equals(a.getAuthority()) || "ROLE_РАБОТНИК".equals(a.getAuthority()));
    }
}