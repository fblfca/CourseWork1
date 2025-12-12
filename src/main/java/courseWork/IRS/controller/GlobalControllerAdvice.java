package courseWork.IRS.controller;

import courseWork.IRS.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Глобальный обработчик (ControllerAdvice), добавляющий общие данные во все представления.
 *
 * Назначение класса:
 * - Автоматически добавляет в модель атрибут userCount — текущее количество пользователей в системе.
 * - Этот атрибут становится доступным во всех HTML-шаблонах без необходимости передавать его вручную в каждом контроллере.
 * - Используется для отображения статистики.
 *
 * Связи с другими классами:
 * - RoleRepository — репозиторий для работы с сущностью Role (в проекте Role используется как пользователь).
 * - Работает на уровне всего приложения благодаря аннотации @ControllerAdvice.
 *
 * Основные функции:
 * - populateUserCount(): метод, помеченный @ModelAttribute, выполняется перед каждым запросом к контроллеру
 *   и добавляет в модель значение количества всех пользователей в базе данных.
 */

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private RoleRepository roleRepository;

    @ModelAttribute("userCount")
    public long populateUserCount() {
        return roleRepository.count();
    }
}