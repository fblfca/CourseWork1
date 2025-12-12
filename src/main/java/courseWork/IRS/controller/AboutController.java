package courseWork.IRS.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для отображения статической страницы «О парке» (About).
 *
 * Назначение класса:
 * - Обеспечивает доступ к информационной странице о парке развлечений.
 *
 * Связи с другими классами:
 * - Не зависит от репозиториев и сервисов — чисто отображение шаблона.
 * - Возвращает шаблон about.html, расположенный в src/main/resources/templates/about.html
 *
 * Основные функции:
 * - about(): Обрабатывает GET-запрос на /about и возвращает соответствующий шаблон.
 */

@Controller
public class AboutController {

    @GetMapping("/about")
    public String aboutPage() {
        return "about"; // вернет шаблон about.html
    }
}