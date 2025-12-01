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

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired private EventBookingRepository eventBookingRepository;
    @Autowired private RoomBookingRepository roomBookingRepository;
    @Autowired private RoomRepository roomRepository;

    @GetMapping
    public String bookings(
            Model model,
            Authentication auth,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        boolean isAdminOrWorker = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_АДМИН") || a.getAuthority().equals("ROLE_РАБОТНИК"));

        List<EventBooking> eventBookings;
        List<RoomBooking> roomBookings;

        if (isAdminOrWorker) {
            eventBookings = eventBookingRepository.findAll();
            roomBookings = roomBookingRepository.findAll();
        } else {
            Integer userId = currentUser.getId();
            eventBookings = eventBookingRepository.findByUserId(userId);
            roomBookings = roomBookingRepository.findByUserId(userId);
        }

        // ФИЛЬТРАЦИЯ: Убираем отмененные брони, чтобы они "исчезли"
        eventBookings = eventBookings.stream()
                .filter(b -> !"отменено".equals(b.getStatus()))
                .collect(Collectors.toList());

        roomBookings = roomBookings.stream()
                .filter(b -> !"отменено".equals(b.getStatus()))
                .collect(Collectors.toList());

        model.addAttribute("eventBookings", eventBookings);
        model.addAttribute("roomBookings", roomBookings);
        model.addAttribute("isAdminOrWorker", isAdminOrWorker);
        return "bookings";
    }

    // --- ОТМЕНА БРОНИРОВАНИЙ ---

    @PostMapping("/cancel/event/{id}")
    public String cancelEventBooking(@PathVariable Integer id) {
        Optional<EventBooking> bookingOpt = eventBookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            EventBooking booking = bookingOpt.get();
            booking.setStatus("отменено");
            eventBookingRepository.save(booking);
        }
        return "redirect:/bookings";
    }

    @PostMapping("/cancel/room/{id}")
    public String cancelRoomBooking(@PathVariable Integer id) {
        Optional<RoomBooking> bookingOpt = roomBookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            RoomBooking booking = bookingOpt.get();
            booking.setStatus("отменено");
            roomBookingRepository.save(booking);
        }
        return "redirect:/bookings";
    }

    // --- РЕДАКТИРОВАНИЕ БРОНИРОВАНИЙ (ЗАЛЫ) ---

    @PostMapping("/update/room")
    public String updateRoomBooking(
            @RequestParam Integer bookingId,
            @RequestParam Integer slotNumber,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam Integer bookingWeight) {

        Optional<RoomBooking> bookingOpt = roomBookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            RoomBooking booking = bookingOpt.get();
            booking.setSlotNumber(slotNumber);
            booking.setBookingWeight(bookingWeight);

            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            booking.setStartTime(start);
            booking.setEndTime(end);

            // Пересчет цены
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
}