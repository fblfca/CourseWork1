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

/**
 * Контроллер, отвечающий за аутентификацию и регистрацию пользователей.
 *
 * Назначение класса:
 * - Обеспечивает отображение страниц входа и регистрации.
 * - Обрабатывает процесс регистрации нового пользователя (включая валидацию формы).
 * - Является точкой входа в приложение — корневой путь "/" перенаправляет на страницу логина.
 *
 * Связи с другими классами:
 * - UserService — сервис для создания пользователя в базе данных.
 * - Role — модель пользователя (в проекте используется как основная сущность авторизации).
 * - Шаблоны: auth/login.html и auth/register.html.
 *
 * Основные функции:
 * - root(): обработка корневого пути с редиректом на страницу входа.
 * - login(): отображение страницы входа.
 * - showRegistrationForm(): отображение формы регистрации с пустой моделью.
 * - register(): обработка отправленной формы регистрации с валидацией и сохранением.
 * - RegistrationForm: внутренний класс-DTO для передачи и валидации данных формы регистрации.
 */

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

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

    /**
     * Обработка отправленной формы регистрации.
     *
     * Параметры:
     * - @Valid RegistrationForm form — данные из формы с включённой валидацией.
     * - BindingResult result — результат валидации.
     * - Model model — для передачи сообщения об ошибке (если регистрация не удалась).
     *
     * При успешной регистрации происходит редирект на страницу входа с параметром ?registered.
     * При ошибке (например, email уже занят) — возвращается та же форма с сообщением об ошибке.
     */

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

    /**
     * Внутренний DTO-класс для представления и валидации формы регистрации.
     *
     * Поля:
     * - email    — обязательный и валидный email (используется как логин).
     * - password — обязательный пароль.
     * - name, surname, phone — необязательные личные данные пользователя.
     */

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