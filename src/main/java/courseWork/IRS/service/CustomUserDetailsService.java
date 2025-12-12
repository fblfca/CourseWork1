package courseWork.IRS.service;

import courseWork.IRS.model.Role;
import courseWork.IRS.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Реализация интерфейса UserDetailsService из Spring Security.
 *
 * Назначение класса:
 * - Отвечает за загрузку данных пользователя из базы данных по логину (email).
 * - Является ключевым компонентом Spring Security — без него аутентификация невозможна.
 * - Преобразует найденную сущность Role в объект CustomUserDetails,
 *   который реализует UserDetails и используется в SecurityContext.
 *
 * Связи с другими классами:
 * - RoleRepository — для поиска пользователя по email.
 * - Role — модель пользователя в базе данных.
 * - CustomUserDetails — класс-обёртка, реализующий UserDetails.
 * - Конфигурируется в SecurityConfig через .userDetailsService(userDetailsService).
 *
 * Основные функции:
 * - loadUserByUsername(): единственный обязательный метод интерфейса UserDetailsService.
 *   Выполняется при каждой попытке входа в систему.
 */

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Загружает пользователя по его логину (email).
     *
     * Этот метод вызывается Spring Security автоматически при обработке формы входа.
     *
     * @return объект UserDetails (CustomUserDetails), содержащий данные пользователя и его роль
     * @throws UsernameNotFoundException если пользователь с указанным email не найден
     */

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Role role = roleRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + login));

        return new CustomUserDetails(role);
    }
}