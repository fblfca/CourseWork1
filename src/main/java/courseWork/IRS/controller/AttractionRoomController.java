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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер для управления аттракционами и залами парка.
 *
 * Назначение класса:
 * - Обеспечивает просмотр, фильтрацию, создание, редактирование и удаление аттракционов и залов.
 * - Обрабатывает бронирование залов (только для работников и админов).
 * - Реализует разграничение доступа в зависимости от роли пользователя.
 *
 * Связи с другими классами:
 * - Репозитории: AttractionRepository, RoomRepository, RoomSlotRepository, RoomBookingRepository, RoleRepository.
 * - Модели: Attraction, Room, RoomBooking, Role, CustomUserDetails.
 * - Шаблоны: attractions-rooms.html, attraction-form.html, room-form.html.
 *
 * Основные функции:
 * - list(): Отображение списка аттракционов и залов с фильтрами.
 * - bookRoom(): Бронирование зала для посетителя (только админ/работник).
 * - Методы редактирования/создания/удаления аттракционов и залов (только админ).
 * - Вспомогательные методы проверки прав доступа.
 */

@Controller
@RequestMapping("/attractions-rooms")
public class AttractionRoomController {

    @Autowired private AttractionRepository attractionRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private RoomBookingRepository roomBookingRepository;
    @Autowired private UserInfoRepository userInfoRepository;

    @GetMapping
    public String list(Model model,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) BigDecimal priceFrom,
                       @RequestParam(required = false) BigDecimal priceTo,
                       @RequestParam(required = false, defaultValue = "name_asc") String sort,
                       Authentication auth) {

        List<Attraction> attractions = attractionRepository.findAll();
        List<Room> rooms = roomRepository.findAll();

        // Фильтрация по поисковому запросу
        if (search != null && !search.trim().isEmpty()) {
            String lower = search.toLowerCase().trim();
            attractions = attractions.stream().filter(a -> a.getName().toLowerCase().contains(lower)).collect(Collectors.toList());
            rooms = rooms.stream().filter(r -> r.getName().toLowerCase().contains(lower)).collect(Collectors.toList());
        }

        // Фильтрация по цене
        BigDecimal finalPriceFrom = priceFrom != null ? priceFrom : BigDecimal.ZERO;
        BigDecimal finalPriceTo = priceTo != null ? priceTo : new BigDecimal("999999.00");

        attractions = attractions.stream()
                .filter(a -> a.getPrice() != null && a.getPrice().compareTo(finalPriceFrom) >= 0 && a.getPrice().compareTo(finalPriceTo) <= 0)
                .collect(Collectors.toList());

        rooms = rooms.stream()
                .filter(r -> r.getPricePerSlotHour() != null && r.getPricePerSlotHour().compareTo(finalPriceFrom) >= 0 && r.getPricePerSlotHour().compareTo(finalPriceTo) <= 0)
                .collect(Collectors.toList());

        // Сортировка
        applySorting(attractions, rooms, sort);

        model.addAttribute("attractions", attractions);
        model.addAttribute("rooms", rooms);
        model.addAttribute("search", search);
        model.addAttribute("priceFrom", priceFrom);
        model.addAttribute("priceTo", priceTo);
        model.addAttribute("sort", sort);

        // Права доступа
        boolean isAdmin = isAdmin(auth);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isAdminOrWorker", isAdminOrWorker(auth));

        // Объекты для форм добавления (только админ)
        if (isAdmin) {
            model.addAttribute("newAttraction", new Attraction());
            model.addAttribute("newRoom", new Room());
        }

        return "attractions-rooms";
    }

    private void applySorting(List<Attraction> attractions, List<Room> rooms, String sort) {
        switch (sort) {
            case "price_asc":
                attractions.sort(Comparator.comparing(Attraction::getPrice));
                rooms.sort(Comparator.comparing(Room::getPricePerSlotHour));
                break;
            case "price_desc":
                attractions.sort(Comparator.comparing(Attraction::getPrice).reversed());
                rooms.sort(Comparator.comparing(Room::getPricePerSlotHour).reversed());
                break;
            case "name_desc":
                attractions.sort(Comparator.comparing(Attraction::getName, String.CASE_INSENSITIVE_ORDER).reversed());
                rooms.sort(Comparator.comparing(Room::getName, String.CASE_INSENSITIVE_ORDER).reversed());
                break;
            case "name_asc":
            default:
                attractions.sort(Comparator.comparing(Attraction::getName, String.CASE_INSENSITIVE_ORDER));
                rooms.sort(Comparator.comparing(Room::getName, String.CASE_INSENSITIVE_ORDER));
                break;
        }
    }

    @PostMapping("/book-room")
    public String bookRoom(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam Integer roomId,
            @RequestParam Integer slotNumber,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam Integer peopleCount,
            @RequestParam(required = false) String clientPhone) {

        // Проверка авторизации
        if (currentUser == null) return "redirect:/login";

        // Проверка прав (админ/работник или клиент)
        boolean hasPrivileges = isAdminOrWorker(currentUser.getAuthorities());
        if (!hasPrivileges) {
            // Обычный пользователь должен звонить оператору для брони зала
            return "redirect:/attractions-rooms?error=call_operator&tab=rooms";
        }

        // Определение клиента (для кого бронируем)
        UserInfo client;
        if (clientPhone != null && !clientPhone.isEmpty()) {
            client = userInfoRepository.findByPhone(clientPhone).orElse(null);
            if (client == null) return "redirect:/attractions-rooms?error=client_not_found&tab=rooms";
        } else {
            client = userInfoRepository.findById(currentUser.getId()).orElse(null);
        }

        if (client == null) return "redirect:/attractions-rooms?error=client_not_found&tab=rooms";

        // Поиск комнаты
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isEmpty()) return "redirect:/attractions-rooms?error=room_not_found&tab=rooms";
        Room room = roomOpt.get();

        // Парсинг времени
        LocalDateTime startLdt;
        LocalDateTime endLdt;
        try {
            startLdt = LocalDateTime.parse(startTime);
            endLdt = LocalDateTime.parse(endTime);
        } catch (Exception e) {
            return "redirect:/attractions-rooms?error=invalid_date_format&tab=rooms";
        }

        // Проверка валидности интервала
        long hours = ChronoUnit.HOURS.between(startLdt, endLdt);
        if (hours <= 0) return "redirect:/attractions-rooms?error=invalid_time&tab=rooms";

        // Проверка на пересечение бронирований
        List<RoomBooking> overlaps = roomBookingRepository.findOverlappingBookings(roomId, slotNumber, startLdt, endLdt);
        if (!overlaps.isEmpty()) {
            return "redirect:/attractions-rooms?error=time_conflict&tab=rooms";
        }

        // Расчет цены
        BigDecimal price = room.getPricePerSlotHour().multiply(new BigDecimal(hours));

        // Создание брони
        RoomBooking booking = new RoomBooking();
        booking.setUserId(client.getId());
        booking.setRoomId(roomId);
        booking.setSlotNumber(slotNumber);
        booking.setStartTime(startLdt);
        booking.setEndTime(endLdt);
        booking.setBookingWeight(peopleCount);
        booking.setPrice(price);

        try {
            roomBookingRepository.save(booking);
        } catch (Exception e) {
            return "redirect:/attractions-rooms?error=save_error&tab=rooms";
        }

        String surname = (client.getSurname() != null) ? client.getSurname() : "Клиент";
        return "redirect:/attractions-rooms?success=booked_for_" + surname + "&tab=rooms";
    }

    // Методы управления (Только Админ)

    @PostMapping("/attraction/save")
    public String saveAttraction(@ModelAttribute Attraction attraction, Authentication auth) {
        if (!isAdmin(auth)) return "redirect:/attractions-rooms?error=access_denied";
        attractionRepository.save(attraction);
        return "redirect:/attractions-rooms";
    }

    @PostMapping("/attraction/delete/{id}")
    public String deleteAttraction(@PathVariable Integer id, Authentication auth) {
        if (!isAdmin(auth)) return "redirect:/attractions-rooms?error=access_denied";
        attractionRepository.deleteById(id);
        return "redirect:/attractions-rooms";
    }

    @PostMapping("/room/save")
    public String saveRoom(@ModelAttribute Room room, Authentication auth) {
        if (!isAdmin(auth)) return "redirect:/attractions-rooms?error=access_denied&tab=rooms";
        roomRepository.save(room);
        return "redirect:/attractions-rooms?tab=rooms";
    }

    @PostMapping("/room/delete/{id}")
    public String deleteRoom(@PathVariable Integer id, Authentication auth) {
        if (!isAdmin(auth)) return "redirect:/attractions-rooms?error=access_denied&tab=rooms";
        roomRepository.deleteById(id);
        return "redirect:/attractions-rooms?tab=rooms";
    }

    // Вспомогательные методы проверки прав
    private boolean isAdminOrWorker(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_АДМИН") || a.getAuthority().equals("ROLE_РАБОТНИК"));
    }

    private boolean isAdminOrWorker(java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {
        if (authorities == null) return false;
        return authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_АДМИН") || a.getAuthority().equals("ROLE_РАБОТНИК"));
    }

    private boolean isAdmin(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_АДМИН"));
    }
}