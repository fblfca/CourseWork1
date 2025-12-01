package courseWork.IRS.controller;

import courseWork.IRS.model.Role;
import courseWork.IRS.model.UserInfo;
import courseWork.IRS.service.CustomUserDetails;
import courseWork.IRS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.ZonedDateTime;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String showProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model,
            Authentication auth) {

        // Получаем полную информацию о пользователе
        UserInfo fullUser = userService.findByLogin(currentUser.getUsername());

        // ЗАЩИТА ОТ ОШИБКИ 500 (Для админа или некорректных данных)
        if (fullUser == null) {
            fullUser = new UserInfo();
            fullUser.setName("Администратор");
            fullUser.setSurname("Системный");
            fullUser.setPhone("Не указан");

            // Создаем временную роль для отображения
            Role tempRole = new Role();
            tempRole.setLogin(currentUser.getUsername());
            tempRole.setRole("админ"); // Предполагаем админа, если данных нет
            tempRole.setCreatedAt(ZonedDateTime.now());
            fullUser.setRole(tempRole);
        }

        model.addAttribute("userCount", userService.getUserCount());
        model.addAttribute("username", auth.getName());
        model.addAttribute("user", fullUser);

        boolean isAdminOrWorker = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_АДМИН") || a.getAuthority().equals("ROLE_РАБОТНИК"));
        model.addAttribute("isAdminOrWorker", isAdminOrWorker);

        return "profile";
    }
}