package com.yusufakdogan.session_service.config;

import com.yusufakdogan.session_service.entity.User;
import com.yusufakdogan.session_service.entity.enums.Role;
import com.yusufakdogan.session_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createIfNotFound("admin1", "admin123", Role.ADMIN, new BigDecimal("500.00"));
        createIfNotFound("admin2", "admin123", Role.ADMIN, new BigDecimal("1000.00"));
        createIfNotFound("admin3", "admin123", Role.ADMIN, new BigDecimal("150.00"));

        createIfNotFound("viewer1", "viewer123", Role.VIEWER, new BigDecimal("50.00"));
        createIfNotFound("viewer2", "viewer123", Role.VIEWER, new BigDecimal("200.00"));
    }

    private void createIfNotFound(String username, String password, Role role, BigDecimal balance) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setWalletBalance(balance);
            userRepository.save(user);
        }
    }
}
