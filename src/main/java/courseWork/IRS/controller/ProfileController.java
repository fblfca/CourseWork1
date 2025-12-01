package courseWork.IRS.controller;

import courseWork.IRS.model.UserInfo;
import courseWork.IRS.service.CustomUserDetails;
import courseWork.IRS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String showProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model,
            Authentication auth) {

        // Получаем полную информацию о пользователе по его логину (email)
        UserInfo fullUser = userService.findByLogin(currentUser.getUsername());

        // Добавляем атрибуты для header.html и profile.html
        model.addAttribute("userCount", userService.getUserCount()); // Перенесено из MainController
        model.addAttribute("username", auth.getName());
        model.addAttribute("user", fullUser); // Полная информация UserInfo

        // Определяем роль для условного отображения
        boolean isAdminOrWorker = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_АДМИН") || a.getAuthority().equals("ROLE_РАБОТНИК"));
        model.addAttribute("isAdminOrWorker", isAdminOrWorker);

        return "profile";
    }
}