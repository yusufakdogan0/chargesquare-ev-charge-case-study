package com.yusufakdogan.session_service.facade;

import com.yusufakdogan.session_service.client.StationServiceClient;
import com.yusufakdogan.session_service.dto.StationConnectorResponse;
import com.yusufakdogan.session_service.dto.StationTariffResponse;
import com.yusufakdogan.session_service.entity.ChargingSession;
import com.yusufakdogan.session_service.entity.User;
import com.yusufakdogan.session_service.entity.enums.SessionStatus;
import com.yusufakdogan.session_service.exception.SessionNotActiveException;
import com.yusufakdogan.session_service.service.CostCalculator;
import com.yusufakdogan.session_service.service.SessionService;
import com.yusufakdogan.session_service.service.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionFacadeTest {

    @Mock
    private StationServiceClient stationServiceClient;

    @Mock
    private SessionService sessionService;

    @Mock
    private CostCalculator costCalculator;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private SessionFacade sessionFacade;

    @Test
    void shouldStopSessionSuccessfully() {
        // Arrange
        Long sessionId = 100L;
        BigDecimal energyKwh = new BigDecimal("12.5");

        User user = new User();
        user.setId(1L);
        user.setUsername("driver");
        user.setWalletBalance(new BigDecimal("500.00"));

        ChargingSession activeSession = new ChargingSession();
        activeSession.setId(sessionId);
        activeSession.setUser(user);
        activeSession.setConnectorId(10L);
        activeSession.setStatus(SessionStatus.ACTIVE);
        activeSession.setStartedAt(Instant.now());
        activeSession.setTariffPricePerKwh(new BigDecimal("8.50"));
        activeSession.setTariffStartFee(new BigDecimal("2.00"));
        activeSession.setTariffCurrency("TRY");

        ChargingSession completedSession = new ChargingSession();
        completedSession.setId(sessionId);
        completedSession.setUser(user);
        completedSession.setConnectorId(10L);
        completedSession.setStatus(SessionStatus.COMPLETED);
        completedSession.setStartedAt(activeSession.getStartedAt());
        completedSession.setEndedAt(Instant.now());
        completedSession.setEnergyKwh(energyKwh);
        completedSession.setCost(new BigDecimal("108.25"));
        completedSession.setCurrency("TRY");
        completedSession.setTariffPricePerKwh(new BigDecimal("8.50"));
        completedSession.setTariffStartFee(new BigDecimal("2.00"));
        completedSession.setTariffCurrency("TRY");

        when(sessionService.getSession(sessionId)).thenReturn(activeSession);
        when(costCalculator.calculate(energyKwh, new BigDecimal("8.50"), new BigDecimal("2.00")))
                .thenReturn(new BigDecimal("108.25"));
        when(walletService.debit(1L, new BigDecimal("108.25"))).thenReturn(new BigDecimal("391.75"));
        when(sessionService.completeSession(sessionId, energyKwh, new BigDecimal("108.25")))
                .thenReturn(completedSession);

        // Act
        var response = sessionFacade.stopSession(sessionId, energyKwh);

        // Assert
        assertThat(response.sessionId()).isEqualTo(sessionId);
        assertThat(response.status()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(response.energyKwh()).isEqualByComparingTo("12.5");
        assertThat(response.cost()).isEqualByComparingTo("108.25");
        assertThat(response.walletBalanceAfter()).isEqualByComparingTo("391.75");

        // Verify the flow
        verify(sessionService).getSession(sessionId);
        verify(costCalculator).calculate(energyKwh, new BigDecimal("8.50"), new BigDecimal("2.00"));
        verify(walletService).debit(1L, new BigDecimal("108.25"));
        verify(sessionService).completeSession(sessionId, energyKwh, new BigDecimal("108.25"));
        verify(stationServiceClient).releaseConnector(10L);
    }

    @Test
    void shouldThrowExceptionWhenStoppingNonActiveSession() {
        // Arrange
        Long sessionId = 100L;
        BigDecimal energyKwh = new BigDecimal("12.5");

        User user = new User();
        user.setId(1L);
        user.setWalletBalance(new BigDecimal("500.00"));

        ChargingSession completedSession = new ChargingSession();
        completedSession.setId(sessionId);
        completedSession.setUser(user);
        completedSession.setStatus(SessionStatus.COMPLETED);

        when(sessionService.getSession(sessionId)).thenReturn(completedSession);

        // Act & Assert
        assertThatThrownBy(() -> sessionFacade.stopSession(sessionId, energyKwh))
                .isInstanceOf(SessionNotActiveException.class);

        // Verify that wallet was not debited and connector was not released
        verify(walletService, never()).debit(any(), any());
        verify(sessionService, never()).completeSession(any(), any(), any());
        verify(stationServiceClient, never()).releaseConnector(any());
    }

    @Test
    void shouldGetSession() {
        // Arrange
        Long sessionId = 100L;

        User user = new User();
        user.setId(1L);
        user.setWalletBalance(new BigDecimal("500.00"));

        ChargingSession session = new ChargingSession();
        session.setId(sessionId);
        session.setUser(user);
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(Instant.now());
        session.setTariffPricePerKwh(new BigDecimal("8.50"));
        session.setTariffStartFee(new BigDecimal("2.00"));
        session.setTariffCurrency("TRY");

        when(sessionService.getSession(sessionId)).thenReturn(session);

        // Act
        var response = sessionFacade.getSession(sessionId);

        // Assert
        assertThat(response.sessionId()).isEqualTo(sessionId);
        assertThat(response.status()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(response.walletBalanceAfter()).isEqualByComparingTo("500.00");
    }
}
