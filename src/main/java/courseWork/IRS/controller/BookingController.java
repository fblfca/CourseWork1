package courseWork.IRS.controller;

import courseWork.IRS.model.EventBooking;
import courseWork.IRS.model.RoomBooking;
import courseWork.IRS.repository.EventBookingRepository;
import courseWork.IRS.repository.RoomBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
    public String bookings(Model model, Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority();

        List<EventBooking> eventBookings;
        List<RoomBooking> roomBookings;

        if (role.contains("АДМИН") || role.contains("РАБОТНИК")) {
            eventBookings = eventBookingRepository.findAll();
            roomBookings = roomBookingRepository.findAll();
        } else {
            Integer userId = getUserIdFromAuth(auth);
            eventBookings = eventBookingRepository.findByUserId(userId);
            roomBookings = roomBookingRepository.findByUserId(userId);
        }

        model.addAttribute("eventBookings", eventBookings);
        model.addAttribute("roomBookings", roomBookings);
        model.addAttribute("isAdminOrWorker", role.contains("АДМИН") || role.contains("РАБОТНИК"));
        return "bookings";
    }

    private Integer getUserIdFromAuth(Authentication auth) {
        // Упрощённо — в реальности лучше через сервис
        return 1;
    }
}