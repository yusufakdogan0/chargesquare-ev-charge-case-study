package com.yusufakdogan.session_service.repository;

import com.yusufakdogan.session_service.TestDatabaseConfig;
import com.yusufakdogan.session_service.entity.ChargingSession;
import com.yusufakdogan.session_service.entity.User;
import com.yusufakdogan.session_service.entity.enums.Role;
import com.yusufakdogan.session_service.entity.enums.SessionStatus;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDatabaseConfig.class)
class ChargingSessionRepositoryTest {

    @Autowired
    private ChargingSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("driver");
        testUser.setPassword("hashed");
        testUser.setRole(Role.ADMIN);
        testUser.setWalletBalance(new BigDecimal("500.00"));
        userRepository.save(testUser);
        entityManager.flush();
    }

    private ChargingSession createActiveSession() {
        ChargingSession session = new ChargingSession();
        session.setUser(testUser);
        session.setConnectorId(1L);
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(Instant.parse("2026-07-09T10:00:00Z"));
        session.setTariffPricePerKwh(new BigDecimal("8.5000"));
        session.setTariffStartFee(new BigDecimal("2.00"));
        session.setTariffCurrency("TRY");
        return session;
    }

    // ── CRUD ──

    @Test
    void shouldSaveAndFindActiveSession() {
        ChargingSession session = createActiveSession();
        sessionRepository.save(session);
        entityManager.flush();
        entityManager.clear();

        Optional<ChargingSession> found = sessionRepository.findById(session.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getConnectorId()).isEqualTo(1L);
        assertThat(found.get().getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(found.get().getStartedAt()).isNotNull();
        assertThat(found.get().getEndedAt()).isNull();
        assertThat(found.get().getCost()).isNull();
        assertThat(found.get().getEnergyKwh()).isNull();
    }

    @Test
    void shouldSaveCompletedSession() {
        ChargingSession session = createActiveSession();
        session.setStatus(SessionStatus.COMPLETED);
        session.setEndedAt(Instant.parse("2026-07-09T10:45:00Z"));
        session.setEnergyKwh(new BigDecimal("12.5000"));
        session.setCost(new BigDecimal("108.25"));
        session.setCurrency("TRY");

        sessionRepository.save(session);
        entityManager.flush();
        entityManager.clear();

        ChargingSession found = sessionRepository.findById(session.getId()).orElseThrow();

        assertThat(found.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(found.getEndedAt()).isNotNull();
        assertThat(found.getCost()).isEqualByComparingTo(new BigDecimal("108.25"));
        assertThat(found.getEnergyKwh()).isEqualByComparingTo(new BigDecimal("12.5000"));
    }

    // ── Query Methods ──

    @Test
    void shouldFindAllByUserId() {
        ChargingSession session1 = createActiveSession();
        ChargingSession session2 = createActiveSession();
        session2.setConnectorId(2L);
        sessionRepository.save(session1);
        sessionRepository.save(session2);
        entityManager.flush();
        entityManager.clear();

        List<ChargingSession> sessions = sessionRepository.findAllByUserId(testUser.getId());

        assertThat(sessions).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListForUserWithNoSessions() {
        List<ChargingSession> sessions = sessionRepository.findAllByUserId(999L);

        assertThat(sessions).isEmpty();
    }

    // ── Enum Persistence ──

    @Test
    void shouldPersistSessionStatusAsString() {
        ChargingSession session = createActiveSession();
        sessionRepository.save(session);
        entityManager.flush();
        entityManager.clear();

        Object rawStatus = entityManager.getEntityManager()
                .createNativeQuery("SELECT status FROM session.charging_sessions WHERE id = :id")
                .setParameter("id", session.getId())
                .getSingleResult();

        assertThat(rawStatus).isEqualTo("ACTIVE");
    }

    @Test
    void shouldPersistCompletedStatus() {
        ChargingSession session = createActiveSession();
        session.setStatus(SessionStatus.COMPLETED);
        session.setEndedAt(Instant.now());
        session.setEnergyKwh(new BigDecimal("5.0000"));
        session.setCost(new BigDecimal("44.50"));
        session.setCurrency("TRY");
        sessionRepository.save(session);
        entityManager.flush();
        entityManager.clear();

        ChargingSession found = sessionRepository.findById(session.getId()).orElseThrow();

        assertThat(found.getStatus()).isEqualTo(SessionStatus.COMPLETED);
    }

    // ── Decimal Precision ──

    @Test
    void shouldPersistTariffSnapshotPrecision() {
        ChargingSession session = createActiveSession();
        session.setTariffPricePerKwh(new BigDecimal("7.1234"));
        session.setTariffStartFee(new BigDecimal("1.99"));
        sessionRepository.save(session);
        entityManager.flush();
        entityManager.clear();

        ChargingSession found = sessionRepository.findById(session.getId()).orElseThrow();

        // NUMERIC(10,4) for price
        assertThat(found.getTariffPricePerKwh()).isEqualByComparingTo(new BigDecimal("7.1234"));
        // NUMERIC(10,2) for fee
        assertThat(found.getTariffStartFee()).isEqualByComparingTo(new BigDecimal("1.99"));
        assertThat(found.getTariffCurrency()).isEqualTo("TRY");
    }

    @Test
    void shouldPersistCostPrecision() {
        ChargingSession session = createActiveSession();
        session.setStatus(SessionStatus.COMPLETED);
        session.setEndedAt(Instant.now());
        session.setEnergyKwh(new BigDecimal("12.5000"));
        session.setCost(new BigDecimal("108.25"));
        session.setCurrency("TRY");
        sessionRepository.save(session);
        entityManager.flush();
        entityManager.clear();

        ChargingSession found = sessionRepository.findById(session.getId()).orElseThrow();

        // NUMERIC(12,2) for cost
        assertThat(found.getCost()).isEqualByComparingTo(new BigDecimal("108.25"));
        // NUMERIC(10,4) for energy
        assertThat(found.getEnergyKwh()).isEqualByComparingTo(new BigDecimal("12.5000"));
    }

    // ── Timestamps ──

    @Test
    void shouldPersistTimestampsWithTimezone() {
        Instant start = Instant.parse("2026-07-09T10:00:00Z");
        Instant end = Instant.parse("2026-07-09T10:45:00Z");

        ChargingSession session = createActiveSession();
        session.setStartedAt(start);
        session.setStatus(SessionStatus.COMPLETED);
        session.setEndedAt(end);
        session.setEnergyKwh(new BigDecimal("10.0000"));
        session.setCost(new BigDecimal("87.00"));
        session.setCurrency("TRY");
        sessionRepository.save(session);
        entityManager.flush();
        entityManager.clear();

        ChargingSession found = sessionRepository.findById(session.getId()).orElseThrow();

        assertThat(found.getStartedAt()).isEqualTo(start);
        assertThat(found.getEndedAt()).isEqualTo(end);
    }

    // ── Lazy Loading ──

    @Test
    void shouldLazyLoadUser() {
        ChargingSession session = createActiveSession();
        sessionRepository.save(session);
        entityManager.flush();
        entityManager.clear();

        ChargingSession found = sessionRepository.findById(session.getId()).orElseThrow();

        // ManyToOne with LAZY fetch — should not be initialized yet
        assertThat(Hibernate.isInitialized(found.getUser())).isFalse();

        // Accessing the field triggers the lazy load
        String username = found.getUser().getUsername();
        assertThat(username).isEqualTo("driver");
        assertThat(Hibernate.isInitialized(found.getUser())).isTrue();
    }
}
