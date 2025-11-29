package courseWork.IRS;

import courseWork.IRS.model.Role;
import courseWork.IRS.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class AmusementParkApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmusementParkApplication.class, args);
    }

    @Bean
    CommandLineRunner initAdmin(RoleRepository roleRepository, BCryptPasswordEncoder encoder) {
        return args -> {
            if (roleRepository.findByLogin("admin@park.ru").isEmpty()) {
                Role admin = new Role();
                admin.setLogin("admin@park.ru");
                admin.setPasswordHash(encoder.encode("admin123"));
                admin.setRole("админ");
                roleRepository.save(admin);
            }
        };
    }
}