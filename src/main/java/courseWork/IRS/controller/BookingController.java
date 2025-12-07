package courseWork.IRS.controller;

import courseWork.IRS.model.EventBooking;
import courseWork.IRS.model.Room;
import courseWork.IRS.model.RoomBooking;
import courseWork.IRS.repository.EventBookingRepository;
import courseWork.IRS.repository.RoomBookingRepository;
import courseWork.IRS.repository.RoomRepository;
import courseWork.IRS.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер для управления бронированиями (залы и особые события).
 *
 * Назначение класса:
 * - Отображение всех броней (для админа/работника) или только своих (для посетителя).
 * - Удаление и редактирование существующих броней.
 * - Расчёт стоимости брони зала на основе количества часов и цены за слот.
 *
 * Связи с другими классами:
 * - Репозитории: EventBookingRepository, RoomBookingRepository, RoomRepository.
 * - Модели: EventBooking, RoomBooking, Room, CustomUserDetails.
 * - Шаблон: bookings.html (отображает таблицу броней).
 * - Использует Spring Security для определения текущего пользователя и его роли.
 *
 * Основные функции:
 * - list(): отображение списка броней с фильтрацией по роли.
 * - deleteEventBooking(): удаление брони на особое событие.
 * - deleteRoomBooking(): удаление брони зала.
 * - editRoomBookingForm(): отображение формы редактирования брони зала.
 * - updateRoomBooking(): сохранение изменений в бронировании зала с пересчётом цены.
 */

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired private EventBookingRepository eventBookingRepository;
    @Autowired private RoomBookingRepository roomBookingRepository;
    @Autowired private RoomRepository roomRepository;

    /**
     * Отображает список всех броней.
     *
     * Поведение зависит от роли пользователя:
     * - Посетитель видит только свои брони.
     * - Работник и админ видят все брони.
     */

    @GetMapping
    public String bookings(
            Model model,
            Authentication auth,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) String clientPhone,
            @RequestParam(required = false) String objectTitle) {

        boolean isAdminOrWorker = isAdminOrWorker(auth);

        List<EventBooking> eventBookings;
        List<RoomBooking> roomBookings;

        if (isAdminOrWorker) {
            // Для Админа/Работника: показываются все брони
            eventBookings = eventBookingRepository.findAll();
            roomBookings = roomBookingRepository.findAll();

            // Для посетителей, соответственно, не все
            if (clientName != null && !clientName.isEmpty()) {
                String q = clientName.toLowerCase();
                eventBookings = eventBookings.stream().filter(b -> b.getUserInfo() != null && (b.getUserInfo().getName() + " " + b.getUserInfo().getSurname()).toLowerCase().contains(q)).collect(Collectors.toList());
                roomBookings = roomBookings.stream().filter(b -> b.getUserInfo() != null && (b.getUserInfo().getName() + " " + b.getUserInfo().getSurname()).toLowerCase().contains(q)).collect(Collectors.toList());
            }
            if (clientPhone != null && !clientPhone.isEmpty()) {
                eventBookings = eventBookings.stream().filter(b -> b.getUserInfo() != null && b.getUserInfo().getPhone().contains(clientPhone)).collect(Collectors.toList());
                roomBookings = roomBookings.stream().filter(b -> b.getUserInfo() != null && b.getUserInfo().getPhone().contains(clientPhone)).collect(Collectors.toList());
            }
            if (objectTitle != null && !objectTitle.isEmpty()) {
                String q = objectTitle.toLowerCase();
                eventBookings = eventBookings.stream().filter(b -> b.getEvent().getTitle().toLowerCase().contains(q)).collect(Collectors.toList());
                roomBookings = roomBookings.stream().filter(b -> b.getRoom().getName().toLowerCase().contains(q)).collect(Collectors.toList());
            }

        } else {
            Integer userId = currentUser.getId();
            eventBookings = eventBookingRepository.findByUserId(userId);
            roomBookings = roomBookingRepository.findByUserId(userId);
        }

        // Действия по обработке брони во вкладке "Брони" для AdminOrWorker
        eventBookings = eventBookings.stream()
                .filter(b -> !"отменено".equals(b.getStatus()) && !"завершено".equals(b.getStatus()))
                .collect(Collectors.toList());
        roomBookings = roomBookings.stream()
                .filter(b -> !"отменено".equals(b.getStatus()) && !"завершено".equals(b.getStatus()))
                .collect(Collectors.toList());

        model.addAttribute("eventBookings", eventBookings);
        model.addAttribute("roomBookings", roomBookings);
        model.addAttribute("isAdminOrWorker", isAdminOrWorker);

        model.addAttribute("clientName", clientName);
        model.addAttribute("clientPhone", clientPhone);
        model.addAttribute("objectTitle", objectTitle);

        return "bookings";
    }

    @PostMapping("/cancel/event/{id}")
    public String cancelEventBooking(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Optional<EventBooking> bookingOpt = eventBookingRepository.findById(id);

        if (bookingOpt.isPresent()) {
            EventBooking booking = bookingOpt.get();
            boolean isOwner = currentUser != null && booking.getUserId().equals(currentUser.getId());
            boolean hasPrivileges = isAdminOrWorker(currentUser);

            if (isOwner || hasPrivileges) {
                booking.setStatus("отменено");
                eventBookingRepository.save(booking);
            }
        }
        return "redirect:/bookings";
    }

    @PostMapping("/complete/event/{id}")
    public String completeEventBooking(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!isAdminOrWorker(currentUser)) return "redirect:/bookings?error=access_denied";

        Optional<EventBooking> bookingOpt = eventBookingRepository.findById(id);
        bookingOpt.ifPresent(b -> {
            b.setStatus("завершено");
            eventBookingRepository.save(b);
        });
        return "redirect:/bookings";
    }

    @PostMapping("/cancel/room/{id}")
    public String cancelRoomBooking(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!isAdminOrWorker(currentUser)) return "redirect:/bookings?error=access_denied";

        Optional<RoomBooking> bookingOpt = roomBookingRepository.findById(id);
        bookingOpt.ifPresent(b -> {
            b.setStatus("отменено");
            roomBookingRepository.save(b);
        });
        return "redirect:/bookings";
    }

    @PostMapping("/complete/room/{id}")
    public String completeRoomBooking(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!isAdminOrWorker(currentUser)) return "redirect:/bookings?error=access_denied";

        Optional<RoomBooking> bookingOpt = roomBookingRepository.findById(id);
        bookingOpt.ifPresent(b -> {
            b.setStatus("завершено");
            roomBookingRepository.save(b);
        });
        return "redirect:/bookings";
    }

    /**
     * Сохранение отредактированной брони зала.
     *
     * Происходит пересчёт стоимости на основе количества часов
     * и цены зала за слот (pricePerSlotHour).
     * Доступно только админу и работнику.
     */

    @PostMapping("/update/room")
    public String updateRoomBooking(@RequestParam Integer bookingId, @RequestParam Integer slotNumber, @RequestParam String startTime, @RequestParam String endTime, @RequestParam Integer bookingWeight, @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!isAdminOrWorker(currentUser)) return "redirect:/bookings?error=access_denied";

        Optional<RoomBooking> bookingOpt = roomBookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            RoomBooking booking = bookingOpt.get();
            booking.setSlotNumber(slotNumber);
            booking.setBookingWeight(bookingWeight);
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            booking.setStartTime(start);
            booking.setEndTime(end);
            Optional<Room> roomOpt = roomRepository.findById(booking.getRoomId());
            if (roomOpt.isPresent()) {
                long hours = ChronoUnit.HOURS.between(start, end);
                if (hours > 0) {
                    BigDecimal price = roomOpt.get().getPricePerSlotHour().multiply(new BigDecimal(hours));
                    booking.setPrice(price);
                }
            }
            roomBookingRepository.save(booking);
        }
        return "redirect:/bookings";
    }

    // Вспомогательные методы
    private boolean isAdminOrWorker(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_АДМИН") || a.getAuthority().equals("ROLE_РАБОТНИК"));
    }

    private boolean isAdminOrWorker(CustomUserDetails user) {
        if (user == null) return false;
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_АДМИН") || a.getAuthority().equals("ROLE_РАБОТНИК"));
    }
}