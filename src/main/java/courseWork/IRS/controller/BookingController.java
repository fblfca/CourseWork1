package courseWork.IRS.controller;

import courseWork.IRS.model.EventBooking;
import courseWork.IRS.model.RoomBooking;
import courseWork.IRS.repository.EventBookingRepository;
import courseWork.IRS.repository.RoomBookingRepository;
import courseWork.IRS.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired private EventBookingRepository eventBookingRepository;
    @Autowired private RoomBookingRepository roomBookingRepository;

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
            // Администраторы и работники видят все бронирования
            eventBookings = eventBookingRepository.findAll();
            roomBookings = roomBookingRepository.findAll();
        } else {
            // Обычные пользователи видят только свои
            Integer userId = currentUser.getId(); // ИЗМЕНЕНИЕ: Получение ID из CustomUserDetails
            eventBookings = eventBookingRepository.findByUserId(userId);
            roomBookings = roomBookingRepository.findByUserId(userId);
        }

        model.addAttribute("eventBookings", eventBookings);
        model.addAttribute("roomBookings", roomBookings);
        model.addAttribute("isAdminOrWorker", isAdminOrWorker);
        return "bookings";
    }
}