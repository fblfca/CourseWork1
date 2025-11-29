package courseWork.IRS.controller;

import courseWork.IRS.model.UserInfo;
import courseWork.IRS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal UserInfo currentUser, Model model) {
        // currentUser — это объект из SecurityContext (email уже есть)
        // Подгружаем полную информацию из БД по email
        UserInfo fullUser = userService.findByEmail(currentUser.getEmail());
        model.addAttribute("user", fullUser);
        return "profile";
    }
}