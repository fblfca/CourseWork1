package courseWork.IRS.controller;

import courseWork.IRS.model.Event;
import courseWork.IRS.model.EventBooking;
import courseWork.IRS.repository.EventBookingRepository;
import courseWork.IRS.repository.EventRepository;
import courseWork.IRS.repository.RoleRepository;
import courseWork.IRS.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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

/**
 * Контроллер для управления особыми мероприятиями парка.
 *
 * Назначение класса:
 * - Обеспечивает просмотр, фильтрацию, создание, редактирование и удаление особых событий.
 * - Обрабатывает бронирование билетов на мероприятия.
 * - Реализует разграничение доступа по ролям (посетитель, работник, админ).
 *
 * Связи с другими классами:
 * - Репозитории: EventRepository, EventBookingRepository, RoleRepository.
 * - Модели: Event, EventBooking, CustomUserDetails.
 * - Шаблоны: events.html (список), event-form.html (редактирование/создание).
 * - Использует Spring Security для определения текущего пользователя и его прав.
 *
 * Основные функции:
 * - list(): отображение списка событий с фильтрами по названию и цене.
 * - showEditForm(): форма создания/редактирования события (только админ).
 * - saveEvent(): сохранение события (только админ).
 * - deleteEvent(): удаление события (только админ).
 * - bookEvent(): бронирование билета на мероприятие (для всех авторизованных).
 */

/**
 * Контроллер для управления особыми мероприятиями парка.
 *
 * Назначение класса:
 * - Обеспечивает просмотр, фильтрацию, создание, редактирование и удаление особых событий.
 * - Обрабатывает бронирование билетов на мероприятия.
 * - Реализует разграничение доступа по ролям (посетитель, работник, админ).
 *
 * Связи с другими классами:
 * - Репозитории: EventRepository, EventBookingRepository, RoleRepository.
 * - Модели: Event, EventBooking, CustomUserDetails.
 * - Шаблоны: events.html (список), event-form.html (редактирование/создание).
 * - Использует Spring Security для определения текущего пользователя и его прав.
 *
 * Основные функции:
 * - list(): отображение списка событий с фильтрами по названию и цене.
 * - showEditForm(): форма создания/редактирования события (только админ).
 * - saveEvent(): сохранение события (только админ).
 * - deleteEvent(): удаление события (только админ).
 * - bookEvent(): бронирование билета на мероприятие (для всех авторизованных).
 */

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
                         @RequestParam(required = false, defaultValue = "date_asc") String sort, // параметр сортировки
                         Model model,
                         Authentication auth) {

        // Логика фильтрации дат
        LocalDateTime fromTime = LocalDateTime.of(date != null ? date : LocalDate.now(), LocalTime.MIN);
        LocalDateTime toTime = LocalDateTime.of(date != null ? date : LocalDate.now().plusYears(1), LocalTime.MAX);

        if (date == null) {
            fromTime = LocalDateTime.now().minusDays(30);
            toTime = LocalDateTime.now().plusMonths(3);
        }

        String finalTitle = title != null && !title.trim().isEmpty() ? title : "";
        BigDecimal finalPriceFrom = priceFrom != null ? priceFrom : BigDecimal.ZERO;
        BigDecimal finalPriceTo = priceTo != null ? priceTo : new BigDecimal("999999.00");

        Sort sortObj = Sort.by(Sort.Direction.ASC, "startTime"); // По умолчанию дата возр.
        switch (sort) {
            case "price_asc":
                sortObj = Sort.by(Sort.Direction.ASC, "price");
                break;
            case "price_desc":
                sortObj = Sort.by(Sort.Direction.DESC, "price");
                break;
            case "name_asc":
                sortObj = Sort.by(Sort.Direction.ASC, "title");
                break;
            case "name_desc":
                sortObj = Sort.by(Sort.Direction.DESC, "title");
                break;
            case "date_desc":
                sortObj = Sort.by(Sort.Direction.DESC, "startTime");
                break;
        }

        List<Event> events = eventRepository.findByTitleContainingIgnoreCaseAndPriceBetweenAndStartTimeBetween(
                finalTitle, finalPriceFrom, finalPriceTo, fromTime, toTime, sortObj
        );

        model.addAttribute("events", events);

        model.addAttribute("isAdminOrWorker", isAdminOrWorker(auth));
        model.addAttribute("isAdmin", isAdmin(auth));

        model.addAttribute("title", title);
        model.addAttribute("priceFrom", priceFrom);
        model.addAttribute("priceTo", priceTo);
        model.addAttribute("date", date);
        model.addAttribute("sort", sort); // Возвращаем текущую сортировку в шаблон

        return "events";
    }

    @GetMapping("/edit")
    public String showEventForm(Model model, Authentication auth) {
        if (!isAdmin(auth)) return "redirect:/events?error=access_denied"; // Строго админ
        model.addAttribute("event", new Event());
        return "event-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, Authentication auth) {
        if (!isAdmin(auth)) return "redirect:/events?error=access_denied"; // Строго админ
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID события:" + id));
        model.addAttribute("event", event);
        return "event-form";
    }

    @PostMapping("/save")
    public String saveEvent(@ModelAttribute Event event, Authentication auth) {
        if (!isAdmin(auth)) return "redirect:/events?error=access_denied"; // Строго админ
        eventRepository.save(event);
        return "redirect:/events";
    }

    @GetMapping("/delete/{id}")
    public String deleteEvent(@PathVariable Integer id, Authentication auth) {
        if (!isAdmin(auth)) return "redirect:/events?error=access_denied"; // Строго админ
        eventRepository.deleteById(id);
        return "redirect:/events?success=deleted";
    }

    @PostMapping("/book/{eventId}")
    public String bookEvent(
            @PathVariable Integer eventId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (currentUser == null) return "redirect:/login";

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return "redirect:/events?error=event_not_found";
        }

        Integer userId = currentUser.getId();

        if (roleRepository.findById(userId).isEmpty()) {
            return "redirect:/events?error=user_not_found";
        }

        boolean alreadyBooked = bookingRepository
                .findFirstByUserIdAndEventIdAndStatusNot(userId, eventId, "отменено")
                .isPresent();

        if (alreadyBooked) {
            return "redirect:/events?error=already_booked";
        }

        EventBooking booking = new EventBooking();
        booking.setUserId(userId);
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

    private boolean isAdmin(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_АДМИН".equals(a.getAuthority()));
    }
}