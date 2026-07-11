package com.yusufakdogan.session_service.repository;

import com.yusufakdogan.session_service.TestDatabaseConfig;
import com.yusufakdogan.session_service.entity.User;
import com.yusufakdogan.session_service.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDatabaseConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testdriver");
        testUser.setPassword("$2a$10$hashedPasswordPlaceholder");
        testUser.setRole(Role.VIEWER);
        testUser.setWalletBalance(new BigDecimal("500.00"));
        userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();
    }

    // ── CRUD ──

    @Test
    void shouldSaveAndFindUser() {
        Optional<User> found = userRepository.findById(testUser.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testdriver");
    }

    // ── Query Methods ──

    @Test
    void shouldFindByUsername() {
        Optional<User> found = userRepository.findByUsername("testdriver");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldReturnEmptyForNonExistentUsername() {
        Optional<User> found = userRepository.findByUsername("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckExistsByUsername() {
        assertThat(userRepository.existsByUsername("testdriver")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    // ── Enum Persistence ──

    @Test
    void shouldPersistRoleAsString() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("hashed");
        admin.setRole(Role.ADMIN);
        admin.setWalletBalance(BigDecimal.ZERO);
        userRepository.save(admin);
        entityManager.flush();
        entityManager.clear();

        User found = userRepository.findById(admin.getId()).orElseThrow();
        assertThat(found.getRole()).isEqualTo(Role.ADMIN);

        // Verify stored as string, not ordinal
        Object rawRole = entityManager.getEntityManager()
                .createNativeQuery("SELECT role FROM session.users WHERE id = :id")
                .setParameter("id", admin.getId())
                .getSingleResult();
        assertThat(rawRole).isEqualTo("ADMIN");
    }

    @Test
    void shouldPersistViewerRole() {
        User found = userRepository.findById(testUser.getId()).orElseThrow();

        assertThat(found.getRole()).isEqualTo(Role.VIEWER);
    }

    // ── Decimal Precision ──

    @Test
    void shouldPersistWalletBalancePrecision() {
        User user = new User();
        user.setUsername("precisiontest");
        user.setPassword("hashed");
        user.setRole(Role.VIEWER);
        user.setWalletBalance(new BigDecimal("1234567890.99"));
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        User found = userRepository.findById(user.getId()).orElseThrow();

        // NUMERIC(12,2) should preserve 2 decimal places
        assertThat(found.getWalletBalance()).isEqualByComparingTo(new BigDecimal("1234567890.99"));
    }

    @Test
    void shouldHandleZeroWalletBalance() {
        User user = new User();
        user.setUsername("zerowallet");
        user.setPassword("hashed");
        user.setRole(Role.VIEWER);
        user.setWalletBalance(BigDecimal.ZERO);
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        User found = userRepository.findById(user.getId()).orElseThrow();

        assertThat(found.getWalletBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ── Unique Constraint ──

    @Test
    void shouldEnforceUniqueUsername() {
        User duplicate = new User();
        duplicate.setUsername("testdriver"); // same as testUser
        duplicate.setPassword("hashed");
        duplicate.setRole(Role.VIEWER);
        duplicate.setWalletBalance(BigDecimal.ZERO);

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> {
                    userRepository.save(duplicate);
                    entityManager.flush();
                }
        );
    }
}
