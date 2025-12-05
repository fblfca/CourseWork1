package courseWork.IRS.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {

    @GetMapping("/about")
    public String aboutPage() {
        return "about"; // вернет шаблон about.html
    }
}