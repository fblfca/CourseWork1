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
                         @RequestParam(required = false) LocalDate date,
                         Model model,
                         Authentication auth) {

        boolean isAdminOrWorker = isAdminOrWorker(auth);

        // Инициализация значений по умолчанию для фильтрации
        String searchTitle = (title != null && !title.isEmpty()) ? title : "";
        BigDecimal minPrice = (priceFrom != null) ? priceFrom : BigDecimal.ZERO;
        BigDecimal maxPrice = (priceTo != null) ? priceTo : new BigDecimal("9999999.99");

        // Диапазон времени для фильтрации по дате
        LocalDateTime fromTime = (date != null) ? date.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime toTime = (date != null) ? date.atTime(LocalTime.MAX) : LocalDateTime.of(3000, 1, 1, 0, 0);

        List<Event> events = eventRepository.findByTitleContainingIgnoreCaseAndPriceBetweenAndStartTimeBetween(
                searchTitle,
                minPrice,
                maxPrice,
                fromTime,
                toTime
        );

        model.addAttribute("events", events);
        model.addAttribute("title", title);
        model.addAttribute("priceFrom", priceFrom);
        model.addAttribute("priceTo", priceTo);
        model.addAttribute("date", date);
        model.addAttribute("isAdminOrWorker", isAdminOrWorker);

        return "events";
    }

    @GetMapping("/edit")
    public String addEvent(Model model, Authentication auth) {
        if (!isAdminOrWorker(auth)) {
            return "redirect:/events?error=access_denied";
        }
        model.addAttribute("event", new Event());
        return "event-form"; // → templates/event-form.html
    }

    @GetMapping("/edit/{id}")
    public String editEvent(@PathVariable Integer id, Model model, Authentication auth) {
        if (!isAdminOrWorker(auth)) {
            return "redirect:/events?error=access_denied";
        }
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isPresent()) {
            model.addAttribute("event", eventOpt.get());
            return "event-form";
        }
        return "redirect:/events?error=not_found";
    }

    @PostMapping("/save")
    public String saveEvent(@ModelAttribute Event event, Authentication auth) {
        if (!isAdminOrWorker(auth)) {
            return "redirect:/events?error=access_denied";
        }
        eventRepository.save(event);
        return "redirect:/events?success=saved";
    }

    @GetMapping("/delete/{id}") // Используем GET для простоты
    public String deleteEvent(@PathVariable Integer id, Authentication auth) {
        if (!isAdminOrWorker(auth)) {
            return "redirect:/events?error=access_denied";
        }
        // Удаляем все бронирования перед удалением самого события
        bookingRepository.findByEventId(id).forEach(booking -> bookingRepository.delete(booking));
        eventRepository.deleteById(id);
        return "redirect:/events?success=deleted";
    }

    @PostMapping("/book/{eventId}")
    public String bookEvent(
            @PathVariable Integer eventId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (currentUser == null) return "redirect:/login";

        Integer userId = currentUser.getId(); // Получаем ID клиента

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return "redirect:/events?error=event_not_found";
        }

        // Проверка, что пользователь существует
        if (roleRepository.findById(userId).isEmpty()) {
            return "redirect:/events?error=user_not_found";
        }

        /** Проверяем, есть ли у пользователя НЕотмененная бронь на это событие.
         * Если статус брони НЕ "отменено" (т.е. "подтверждено" или любой другой активный),
         * то бронирование запрещено.
         **/
        boolean alreadyBooked = bookingRepository
                .findFirstByUserIdAndEventIdAndStatusNot(userId, eventId, "отменено")
                .isPresent();

        if (alreadyBooked) {
            return "redirect:/events?error=already_booked";
        }

        EventBooking booking = new EventBooking();
        booking.setUserId(userId);
        booking.setEventId(eventId);
        // Статус новой брони по умолчанию будет "подтверждено"
        booking.setPrice(event.getPrice() != null ? event.getPrice() : BigDecimal.ZERO);

        try {
            bookingRepository.save(booking);
        } catch (Exception e) {
            return "redirect:/events?error=" + e.getMessage();
        }

        return "redirect:/events?success=booked";
    }

    // Вспомогательный метод для проверки прав (Authentication)
    private boolean isAdminOrWorker(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_АДМИН".equals(a.getAuthority()) || "ROLE_РАБОТНИК".equals(a.getAuthority()));
    }
}