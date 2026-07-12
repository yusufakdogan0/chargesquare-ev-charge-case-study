package com.yusufakdogan.session_service.service;

import com.yusufakdogan.session_service.entity.ChargingSession;
import com.yusufakdogan.session_service.entity.User;
import com.yusufakdogan.session_service.entity.enums.Role;
import com.yusufakdogan.session_service.exception.NegativeBalanceException;
import com.yusufakdogan.session_service.repository.ChargingSessionRepository;
import com.yusufakdogan.session_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

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
                .isInstanceOf(NegativeBalanceException.class)
                .hasMessageContaining("negative balance");

        verify(sessionRepository, never()).save(any(ChargingSession.class));
    }
}
