package com.yusufakdogan.session_service.service;

import com.yusufakdogan.session_service.entity.ChargingSession;
import com.yusufakdogan.session_service.entity.User;
import com.yusufakdogan.session_service.entity.enums.Role;
import com.yusufakdogan.session_service.entity.enums.SessionStatus;
import com.yusufakdogan.session_service.exception.NegativeBalanceException;
import com.yusufakdogan.session_service.exception.ResourceNotFoundException;
import com.yusufakdogan.session_service.exception.SessionNotActiveException;
import com.yusufakdogan.session_service.repository.ChargingSessionRepository;
import com.yusufakdogan.session_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private ChargingSessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SessionService sessionService;

    @Test
    void shouldRejectStartingSessionWhenUserBalanceIsNegative() {
        User user = new User();
        user.setId(1L);
        user.setUsername("driver");
        user.setRole(Role.ADMIN);
        user.setWalletBalance(new BigDecimal("-5.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> sessionService.createSession(1L, 10L, BigDecimal.TEN, BigDecimal.ONE, "EUR"))
                .isInstanceOf(NegativeBalanceException.class);

        verify(sessionRepository, never()).save(any(ChargingSession.class));
    }

    @Test
    void shouldCompleteSession() {
        ChargingSession session = new ChargingSession();
        session.setId(100L);
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(Instant.now());
        session.setTariffPricePerKwh(new BigDecimal("8.50"));
        session.setTariffStartFee(new BigDecimal("2.00"));
        session.setTariffCurrency("TRY");

        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(ChargingSession.class))).thenReturn(session);

        ChargingSession completed = sessionService.completeSession(100L, new BigDecimal("12.5"), new BigDecimal("108.25"));

        assertThat(completed.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(completed.getEndedAt()).isNotNull();
        assertThat(completed.getEnergyKwh()).isEqualByComparingTo("12.5");
        assertThat(completed.getCost()).isEqualByComparingTo("108.25");
        assertThat(completed.getCurrency()).isEqualTo("TRY");
        verify(sessionRepository).save(session);
    }

    @Test
    void shouldThrowSessionNotActiveExceptionWhenCompletingAlreadyCompletedSession() {
        ChargingSession session = new ChargingSession();
        session.setId(100L);
        session.setStatus(SessionStatus.COMPLETED);

        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> sessionService.completeSession(100L, new BigDecimal("12.5"), new BigDecimal("108.25")))
                .isInstanceOf(SessionNotActiveException.class);

        verify(sessionRepository, never()).save(any(ChargingSession.class));
    }

    @Test
    void shouldGetSessionById() {
        ChargingSession session = new ChargingSession();
        session.setId(100L);
        session.setStatus(SessionStatus.ACTIVE);

        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session));

        ChargingSession result = sessionService.getSession(100L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenSessionNotFound() {
        when(sessionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.getSession(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetUserSessions() {
        ChargingSession session1 = new ChargingSession();
        session1.setId(100L);
        session1.setStatus(SessionStatus.COMPLETED);

        ChargingSession session2 = new ChargingSession();
        session2.setId(101L);
        session2.setStatus(SessionStatus.ACTIVE);

        when(sessionRepository.findAllByUserId(1L)).thenReturn(List.of(session1, session2));

        List<ChargingSession> sessions = sessionService.getUserSessions(1L);

        assertThat(sessions).hasSize(2);
        assertThat(sessions.get(0).getId()).isEqualTo(100L);
        assertThat(sessions.get(1).getId()).isEqualTo(101L);
    }
}
