package courseWork.IRS;

import courseWork.IRS.model.Role;
import courseWork.IRS.model.UserInfo;
import courseWork.IRS.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Главный класс приложения — точка входа в Spring Boot.
 *
 * Назначение класса:
 * - Запускает всё веб-приложение.
 * - Автоматически создаёт учётную запись администратора при первом запуске,
 *   если её ещё нет в базе данных.
 * - Обеспечивает интеграцию всех компонентов (контроллеры, сервисы, репозитории, безопасность).
 *
 * Связи с другими классами:
 * - RoleRepository — для проверки существования и создания администратора.
 * - BCryptPasswordEncoder — для хеширования пароля администратора.
 * - Role — модель пользователя, в которую сохраняется админ.
 *
 * Основные функции:
 * - main(): статический метод, запускающий Spring Boot приложение.
 * - initAdmin(): бин CommandLineRunner, выполняющийся один раз после старта приложения.
 *   Проверяет наличие администратора по email и создаёт его, если отсутствует.
 */

@SpringBootApplication
public class AmusementParkApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmusementParkApplication.class, args);
    }

    /**
     * Инициализация учётной записи администратора при запуске приложения.
     *
     * Этот бин выполняется автоматически после старта контекста Spring.
     * Если в базе данных нет пользователя с email "admin@park.ru",
     * создаётся администратор с паролем "admin123".
     *
     * @param roleRepository репозиторий для работы с пользователями
     * @param encoder        шифровальщик паролей
     * @return CommandLineRunner, выполняющий проверку и создание админа
     */


    @Bean
    CommandLineRunner initAdmin(RoleRepository roleRepository, BCryptPasswordEncoder encoder) {
        return args -> {
            if (roleRepository.findByLogin("admin@park.ru").isEmpty()) {
                Role admin = new Role();
                admin.setLogin("admin@park.ru");
                admin.setPasswordHash(encoder.encode("admin123"));
                admin.setRole("админ");

                UserInfo adminInfo = new UserInfo();
                adminInfo.setName("Системный");
                adminInfo.setSurname("Администратор");
                adminInfo.setPhone("+71112223344");
                adminInfo.setRole(admin);
                admin.setUserInfo(adminInfo);

                roleRepository.save(admin);
            }
        };
    }
}