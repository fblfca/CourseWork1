package courseWork.IRS.controller;

import courseWork.IRS.model.*;
import courseWork.IRS.repository.*;
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

@Controller
@RequestMapping("/attractions-rooms")
public class AttractionRoomController {

    @Autowired private AttractionRepository attractionRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private RoomSlotRepository slotRepository;
    @Autowired private RoomBookingRepository roomBookingRepository;
    @Autowired private UserInfoRepository userInfoRepository; // Добавили репозиторий

    @GetMapping
    public String list(Model model,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) BigDecimal priceFrom,
                       @RequestParam(required = false) BigDecimal priceTo,
                       Authentication auth) {

        List<Attraction> attractions = attractionRepository.findAll();
        List<Room> rooms = roomRepository.findAll();

        // Фильтрация (оставляем как было)
        if (search != null && !search.trim().isEmpty()) {
            String lower = search.toLowerCase().trim();
            attractions = attractions.stream()
                    .filter(a -> a.getName().toLowerCase().contains(lower) ||
                            (a.getDescription() != null && a.getDescription().toLowerCase().contains(lower)))
                    .collect(Collectors.toList());
            rooms = rooms.stream()
                    .filter(r -> r.getName().toLowerCase().contains(lower) ||
                            (r.getDescription() != null && r.getDescription().toLowerCase().contains(lower)))
                    .collect(Collectors.toList());
        }

        BigDecimal finalPriceFrom = priceFrom != null ? priceFrom : BigDecimal.ZERO;
        BigDecimal finalPriceTo = priceTo != null ? priceTo : new BigDecimal("999999.00");

        attractions = attractions.stream()
                .filter(a -> a.getPrice() != null && a.getPrice().compareTo(finalPriceFrom) >= 0 && a.getPrice().compareTo(finalPriceTo) <= 0)
                .collect(Collectors.toList());

        rooms = rooms.stream()
                .filter(r -> r.getPricePerSlotHour() != null && r.getPricePerSlotHour().compareTo(finalPriceFrom) >= 0 && r.getPricePerSlotHour().compareTo(finalPriceTo) <= 0)
                .collect(Collectors.toList());

        model.addAttribute("attractions", attractions);
        model.addAttribute("rooms", rooms);
        model.addAttribute("isAdminOrWorker", isAdminOrWorker(auth));
        model.addAttribute("search", search);
        model.addAttribute("priceFrom", priceFrom);
        model.addAttribute("priceTo", priceTo);

        return "attractions-rooms";
    }

    @PostMapping("/book-room")
    public String bookRoom(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam Integer roomId,
            @RequestParam Integer slotNumber,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam Integer peopleCount,
            // Новые параметры для поиска клиента
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) String clientPhone) {

        // Проверка прав: бронировать могут только Админ или Работник
        // (хотя кнопка скрыта, проверка на сервере обязательна)
        if (!isAdminOrWorker(currentUser)) {
            return "redirect:/attractions-rooms?error=access_denied";
        }

        // Поиск клиента по телефону
        UserInfo client = userInfoRepository.findByPhone(clientPhone)
                .orElse(null);

        if (client == null) {
            return "redirect:/attractions-rooms?error=client_not_found";
        }

        // (Опционально) Можно проверить совпадает ли имя, но телефон надежнее

        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isEmpty()) {
            return "redirect:/attractions-rooms?error=room_not_found";
        }
        Room room = roomOpt.get();

        LocalDateTime startLdt = LocalDateTime.parse(startTime);
        LocalDateTime endLdt = LocalDateTime.parse(endTime);

        long hours = ChronoUnit.HOURS.between(startLdt, endLdt);
        if (hours <= 0) {
            return "redirect:/attractions-rooms?error=invalid_time";
        }

        BigDecimal price = room.getPricePerSlotHour().multiply(new BigDecimal(hours));

        RoomBooking booking = new RoomBooking();
        booking.setUserId(client.getId()); // ID найденного клиента, а не работника!
        booking.setRoomId(roomId);
        booking.setSlotNumber(slotNumber);
        booking.setStartTime(startLdt);
        booking.setEndTime(endLdt);
        booking.setBookingWeight(peopleCount);
        booking.setPrice(price);

        try {
            roomBookingRepository.save(booking);
        } catch (Exception e) {
            return "redirect:/attractions-rooms?error=" + e.getMessage();
        }

        return "redirect:/attractions-rooms?success=booked_for_" + client.getSurname();
    }

    // Вспомогательный метод для проверки прав
    private boolean isAdminOrWorker(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_АДМИН") || a.getAuthority().equals("ROLE_РАБОТНИК"));
    }

    // Перегрузка для использования CustomUserDetails внутри метода
    private boolean isAdminOrWorker(CustomUserDetails user) {
        if (user == null) return false;
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_АДМИН") || a.getAuthority().equals("ROLE_РАБОТНИК"));
    }
}