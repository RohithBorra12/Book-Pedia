package com.bookstore.user.config;

import com.bookstore.user.model.Role;
import com.bookstore.user.model.User;
import com.bookstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates a demo admin on startup so you can test ADMIN-only features. */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository repository;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (!repository.existsByUsername("admin")) {
            repository.save(new User("admin", encoder.encode("admin123"), Role.ADMIN));
            // Text block (Java 15+)
            log.info("""

                    ==========================================
                      Demo admin created -> admin / admin123
                    ==========================================""");
        }
    }
}
