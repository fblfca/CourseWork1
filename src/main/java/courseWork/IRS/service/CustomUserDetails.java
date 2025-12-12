package courseWork.IRS.service;

import courseWork.IRS.model.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

/**
 * Реализация интерфейса UserDetails из Spring Security.
 *
 * Назначение класса:
 * - Предоставляет Spring Security информацию о текущем аутентифицированном пользователе.
 * - Преобразует сущность Role (пользователь из базы) в объект, понятный Spring Security.
 * - Используется CustomUserDetailsService при загрузке пользователя по логину.
 *
 * Связи с другими классами:
 * - Role — основная модель пользователя в приложении.
 * - CustomUserDetailsService — создаёт экземпляры этого класса при аутентификации.
 * - Используется в контроллерах через аннотацию @AuthenticationPrincipal.
 *
 * Основные функции:
 * - Конструктор: принимает объект Role и заполняет все необходимые поля UserDetails.
 * - Геттеры: возвращают данные пользователя (логин, пароль, роль, статусы аккаунта).
 * - Реализует все методы интерфейса UserDetails (в текущей реализации аккаунт всегда активен).
 */

public class CustomUserDetails implements UserDetails {

    private final Integer id; // Ключевое добавление: ID пользователя из таблицы roles
    private final String login;
    private final String passwordHash;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Конструктор: создаёт объект UserDetails на основе сущности Role.
     *
     * @param role объект пользователя из базы данных
     */

    public CustomUserDetails(Role role) {
        this.id = role.getId();
        this.login = role.getLogin();
        this.passwordHash = role.getPasswordHash();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.getRole().toUpperCase()));
    }

    public Integer getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}