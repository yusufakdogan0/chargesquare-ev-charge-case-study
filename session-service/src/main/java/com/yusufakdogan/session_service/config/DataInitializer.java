package com.yusufakdogan.session_service.config;

import com.yusufakdogan.session_service.entity.User;
import com.yusufakdogan.session_service.entity.enums.Role;
import com.yusufakdogan.session_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setWalletBalance(new BigDecimal("500.00"));
            userRepository.save(admin);
        }

        if (userRepository.findByUsername("viewer").isEmpty()) {
            User viewer = new User();
            viewer.setUsername("viewer");
            viewer.setPassword(passwordEncoder.encode("viewer123"));
            viewer.setRole(Role.VIEWER);
            viewer.setWalletBalance(new BigDecimal("500.00"));
            userRepository.save(viewer);
        }
    }
}
