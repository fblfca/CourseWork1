package courseWork.IRS.controller;

import courseWork.IRS.model.*;
import courseWork.IRS.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/attractions-rooms")
public class AttractionRoomController {

    @Autowired private AttractionRepository attractionRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private RoomSlotRepository slotRepository;
    @Autowired private RoomBookingRepository roomBookingRepository;
    @Autowired private RoleRepository roleRepository;

    @GetMapping
    public String list(Model model,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) BigDecimal priceFrom,
                       @RequestParam(required = false) BigDecimal priceTo,
                       Authentication auth) {

        List<Attraction> attractions = attractionRepository.findAll();
        List<Room> rooms = roomRepository.findAll();

        // Фильтр по названию
        if (search != null && !search.trim().isEmpty()) {
            String lower = search.toLowerCase().trim();
            attractions = attractions.stream()
                    .filter(a -> a.getName() != null && a.getName().toLowerCase().contains(lower))
                    .collect(Collectors.toList());

            rooms = rooms.stream()
                    .filter(r -> r.getName() != null && r.getName().toLowerCase().contains(lower))
                    .collect(Collectors.toList());
        }

        // Фильтр по цене "от"
        if (priceFrom != null && priceFrom.compareTo(BigDecimal.ZERO) > 0) {
            attractions = attractions.stream()
                    .filter(a -> a.getPrice() != null && a.getPrice().compareTo(priceFrom) >= 0)
                    .collect(Collectors.toList());
            rooms = rooms.stream()
                    .filter(r -> r.getPricePerSlotHour() != null && r.getPricePerSlotHour().compareTo(priceFrom) >= 0)
                    .collect(Collectors.toList());
        }

        // Фильтр по цене "до"
        if (priceTo != null && priceTo.compareTo(BigDecimal.ZERO) > 0) {
            attractions = attractions.stream()
                    .filter(a -> a.getPrice() != null && a.getPrice().compareTo(priceTo) <= 0)
                    .collect(Collectors.toList());
            rooms = rooms.stream()
                    .filter(r -> r.getPricePerSlotHour() != null && r.getPricePerSlotHour().compareTo(priceTo) <= 0)
                    .collect(Collectors.toList());
        }

        model.addAttribute("attractions", attractions);
        model.addAttribute("rooms", rooms);
        model.addAttribute("isAdminOrWorker", isAdminOrWorker(auth));
        model.addAttribute("search", search);
        model.addAttribute("priceFrom", priceFrom);
        model.addAttribute("priceTo", priceTo);

        return "attractions-rooms";
    }

    @PostMapping("/book-room")
    public String bookRoom(@RequestParam Integer userId,
                           @RequestParam Integer roomId,
                           @RequestParam Integer slotNumber,
                           @RequestParam String startTime,
                           @RequestParam String endTime,
                           @RequestParam Integer peopleCount,
                           Authentication auth) {

        if (!isAdminOrWorker(auth)) {
            return "redirect:/attractions-rooms";
        }

        RoomBooking booking = new RoomBooking();
        booking.setUserId(userId);
        booking.setRoomId(roomId);
        booking.setSlotNumber(slotNumber);
        booking.setStartTime(ZonedDateTime.parse(startTime));
        booking.setEndTime(ZonedDateTime.parse(endTime));
        booking.setBookingWeight(peopleCount);
        booking.setPrice(new BigDecimal("1500.00")); // можно улучшить расчёт

        roomBookingRepository.save(booking);
        return "redirect:/attractions-rooms";
    }

    private boolean isAdminOrWorker(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_АДМИН") || a.getAuthority().equals("ROLE_РАБОТНИК"));
    }
}