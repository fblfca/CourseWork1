package courseWork.IRS.controller;

import courseWork.IRS.model.UserInfo;
import courseWork.IRS.service.CustomUserDetails;
import courseWork.IRS.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProfileController {

    @Autowired private UserService userService;

    @GetMapping("/profile")
    public String showProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model) {

        UserInfo fullUser = userService.findByLogin(currentUser.getUsername());
        if (fullUser == null) return "redirect:/login";

        model.addAttribute("user", fullUser);
        return "profile";
    }

    @PostMapping("/profile/update/personal")
    public String updatePersonalInfo(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam String name,
            @RequestParam String surname,
            @RequestParam String phone,
            @RequestParam String currentPassword) {

        // Проверка пароля
        if (!userService.checkPassword(currentUser.getUsername(), currentPassword)) {
            return "redirect:/profile?error=wrong_password";
        }

        userService.updatePersonalInfo(currentUser.getId(), name, surname, phone);
        return "redirect:/profile?success=personal_updated";
    }

    @PostMapping("/profile/update/email")
    public String updateEmail(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam String newEmail,
            @RequestParam String currentPassword,
            HttpSession session) {

        if (!userService.checkPassword(currentUser.getUsername(), currentPassword)) {
            return "redirect:/profile?error=wrong_password";
        }

        try {
            userService.updateEmail(currentUser.getId(), newEmail);
            // Принудительный выход
            SecurityContextHolder.clearContext();
            session.invalidate();
            return "redirect:/login?success=email_changed";
        } catch (Exception e) {
            return "redirect:/profile?error=" + e.getMessage();
        }
    }

    @PostMapping("/profile/update/password")
    public String updatePassword(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam String newPassword,
            @RequestParam String currentPassword,
            HttpSession session) {

        if (!userService.checkPassword(currentUser.getUsername(), currentPassword)) {
            return "redirect:/profile?error=wrong_password";
        }

        userService.updatePassword(currentUser.getId(), newPassword);

        SecurityContextHolder.clearContext();
        session.invalidate();
        return "redirect:/login?success=password_changed";
    }
}