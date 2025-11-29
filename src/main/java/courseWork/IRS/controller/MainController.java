package courseWork.IRS.controller;

import courseWork.IRS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String profile(Model model, Authentication auth) {
        model.addAttribute("userCount", userService.getUserCount());
        model.addAttribute("username", auth.getName());
        return "profile";
    }
}