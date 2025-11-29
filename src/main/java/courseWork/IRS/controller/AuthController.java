package courseWork.IRS.controller;

import courseWork.IRS.model.Role;
import courseWork.IRS.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // ЭТА СТРОКА РЕШАЕТ ВСЁ!
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";  // → templates/auth/login.html
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationForm());
        return "auth/register";  // → templates/auth/register.html
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") RegistrationForm form,
                           BindingResult result,
                           Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.registerUser(
                    form.getEmail(),
                    form.getPassword(),
                    form.getName(),
                    form.getSurname(),
                    form.getPhone()
            );
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    // Внутренний класс — оставляем как есть
    public static class RegistrationForm {
        @Email @NotBlank private String email;
        @NotBlank private String password;
        private String name;
        private String surname;
        private String phone;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSurname() { return surname; }
        public void setSurname(String surname) { this.surname = surname; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}