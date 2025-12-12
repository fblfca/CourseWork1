package courseWork.IRS.config;

import courseWork.IRS.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Конфигурационный класс Spring Security.
 *
 * Этот класс полностью отвечает за настройку безопасности приложения:
 * - какие URL доступны без авторизации,
 * - какая страница используется для входа,
 * - куда редиректить после успешного логина,
 * - как обрабатывается выход из системы,
 * - какой алгоритм хеширования паролей используется.
 *
 * Связи с другими классами:
 * - CustomUserDetailsService — реализация UserDetailsService для загрузки пользователя из БД.
 * - BCryptPasswordEncoder — бин для шифрования паролей.
 *
 * Основные функции:
 * - passwordEncoder() — создаёт и предоставляет бин для хеширования паролей.
 * - filterChain() — основной метод, в котором задаются все правила безопасности.
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Основная конфигурация цепочки фильтров безопасности.
     *
     * Здесь задаются:
     * - правила доступа к URL,
     * - настройки формы входа,
     * - настройки выхода из системы,
     * - подключение собственного UserDetailsService.
     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/css/**").permitAll()
                        .requestMatchers("/profile/**").authenticated()
                        .requestMatchers("/events/**", "/attractions-rooms/**", "/bookings/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/profile", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }
}