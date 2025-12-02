package courseWork.IRS.controller;

import courseWork.IRS.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private RoleRepository roleRepository;

    // Этот метод делает переменную userCount доступной во всех HTML-шаблонах
    @ModelAttribute("userCount")
    public long populateUserCount() {
        return roleRepository.count();
    }
}