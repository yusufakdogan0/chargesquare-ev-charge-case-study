package com.yusufakdogan.session_service.security;

import com.yusufakdogan.session_service.entity.User;
import com.yusufakdogan.session_service.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm");
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        User user = new User(1L, "testuser", "password", Role.ADMIN, new BigDecimal("500.00"));

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void validateToken_ShouldReturnTrueForValidToken() {
        User user = new User(1L, "testuser", "password", Role.ADMIN, new BigDecimal("500.00"));
        String token = jwtService.generateToken(user);

        Boolean isValid = jwtService.validateToken(token, "testuser");

        assertTrue(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalseForInvalidUsername() {
        User user = new User(1L, "testuser", "password", Role.ADMIN, new BigDecimal("500.00"));
        String token = jwtService.generateToken(user);

        Boolean isValid = jwtService.validateToken(token, "wronguser");

        assertFalse(isValid);
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        User user = new User(1L, "testuser", "password", Role.ADMIN, new BigDecimal("500.00"));
        String token = jwtService.generateToken(user);

        String username = jwtService.extractUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    void extractUserId_ShouldReturnCorrectUserId() {
        User user = new User(1L, "testuser", "password", Role.ADMIN, new BigDecimal("500.00"));
        String token = jwtService.generateToken(user);

        Long userId = jwtService.extractUserId(token);

        assertEquals(1L, userId);
    }

    @Test
    void extractRole_ShouldReturnCorrectRole() {
        User user = new User(1L, "testuser", "password", Role.ADMIN, new BigDecimal("500.00"));
        String token = jwtService.generateToken(user);

        String role = jwtService.extractRole(token);

        assertEquals("ADMIN", role);
    }
}
