package com.yusufakdogan.session_service.facade;

import com.yusufakdogan.session_service.client.StationServiceClient;
import com.yusufakdogan.session_service.dto.SessionResponse;
import com.yusufakdogan.session_service.dto.StationConnectorResponse;
import com.yusufakdogan.session_service.dto.TariffSnapshotDto;
import com.yusufakdogan.session_service.entity.ChargingSession;
import com.yusufakdogan.session_service.exception.ConnectorOccupiedException;
import com.yusufakdogan.session_service.exception.SessionNotActiveException;
import com.yusufakdogan.session_service.service.CostCalculator;
import com.yusufakdogan.session_service.service.SessionService;
import com.yusufakdogan.session_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionFacade {

    private final StationServiceClient stationServiceClient;
    private final SessionService sessionService;
    private final CostCalculator costCalculator;
    private final WalletService walletService;

    @Transactional
    public SessionResponse startSession(
            Long userId,
            Long connectorId
    ) {
        // Step 1: Get connector from station service and validate status
        StationConnectorResponse connector = stationServiceClient.getConnectorById(connectorId);
        if ("OCCUPIED".equals(connector.status())) {
            throw new ConnectorOccupiedException("Connector " + connectorId + " is already occupied");
        }

        // Step 2: Create session first in our DB
        ChargingSession session = sessionService.createSession(
                userId,
                connectorId,
                connector.tariff().pricePerKwh(),
                connector.tariff().startFee(),
                connector.tariff().currency()
        );

        // Step3: Try to occupy connector at station service
        try {
            stationServiceClient.occupyConnector(connectorId);
        } catch (Exception e) {
            // If occupy fails, delete the session (or mark as failed)
            // Since we have @Transactional, session creation will roll back automatically!
            throw e;
        }

        // Step4: Return session response
        return new SessionResponse(
                session.getId(),
                session.getStatus(),
                session.getConnectorId(),
                session.getStartedAt(),
                null, // endedAt - null for active sessions
                null, // energyKwh - null for active sessions
                null, // cost - null for active sessions
                null, // currency - null for active sessions
                session.getUser().getWalletBalance(),
                new TariffSnapshotDto(
                        session.getTariffPricePerKwh(),
                        session.getTariffStartFee(),
                        session.getTariffCurrency()
                )
        );
    }

    @Transactional
    public SessionResponse stopSession(Long sessionId, BigDecimal energyKwh) {
        // Step 1: Get session
        ChargingSession session = sessionService.getSession(sessionId);

        // Step 2: Guard - session must be ACTIVE
        if (session.getStatus() != com.yusufakdogan.session_service.entity.enums.SessionStatus.ACTIVE) {
            throw new SessionNotActiveException("Session " + sessionId + " is not ACTIVE and cannot be stopped");
        }

        // Step 3: Compute cost using tariff snapshot
        BigDecimal cost = costCalculator.calculate(
                energyKwh,
                session.getTariffPricePerKwh(),
                session.getTariffStartFee()
        );

        // Step 4: Debit wallet (allows negative balance)
        BigDecimal newBalance = walletService.debit(session.getUser().getId(), cost);

        // Step 5: Complete session
        ChargingSession completedSession = sessionService.completeSession(sessionId, energyKwh, cost);

        // Step 6: Release connector
        stationServiceClient.releaseConnector(session.getConnectorId());

        // Step 7: Return response
        return new SessionResponse(
                completedSession.getId(),
                completedSession.getStatus(),
                completedSession.getConnectorId(),
                completedSession.getStartedAt(),
                completedSession.getEndedAt(),
                completedSession.getEnergyKwh(),
                completedSession.getCost(),
                completedSession.getCurrency(),
                newBalance,
                new TariffSnapshotDto(
                        completedSession.getTariffPricePerKwh(),
                        completedSession.getTariffStartFee(),
                        completedSession.getTariffCurrency()
                )
        );
    }

    public SessionResponse getSession(Long id) {
        ChargingSession session = sessionService.getSession(id);
        return mapToSessionResponse(session, session.getUser().getWalletBalance());
    }

    public List<SessionResponse> getUserSessions(Long userId) {
        List<ChargingSession> sessions = sessionService.getUserSessions(userId);
        BigDecimal currentBalance = walletService.getBalance(userId);
        return sessions.stream()
                .map(session -> mapToSessionResponse(session, currentBalance))
                .collect(Collectors.toList());
    }

    private SessionResponse mapToSessionResponse(ChargingSession session, BigDecimal walletBalance) {
        return new SessionResponse(
                session.getId(),
                session.getStatus(),
                session.getConnectorId(),
                session.getStartedAt(),
                session.getEndedAt(),
                session.getEnergyKwh(),
                session.getCost(),
                session.getCurrency(),
                walletBalance,
                new TariffSnapshotDto(
                        session.getTariffPricePerKwh(),
                        session.getTariffStartFee(),
                        session.getTariffCurrency()
                )
        );
    }
}
