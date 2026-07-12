package com.yusufakdogan.session_service.facade;

import com.yusufakdogan.session_service.client.StationServiceClient;
import com.yusufakdogan.session_service.dto.SessionResponse;
import com.yusufakdogan.session_service.dto.StationConnectorResponse;
import com.yusufakdogan.session_service.dto.TariffSnapshotDto;
import com.yusufakdogan.session_service.entity.ChargingSession;
import com.yusufakdogan.session_service.exception.ConnectorOccupiedException;
import com.yusufakdogan.session_service.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessionFacade {

    private final StationServiceClient stationServiceClient;
    private final SessionService sessionService;

    @Transactional
    public SessionResponse startSession(
            Long userId,
            Long connectorId,
            String bearerToken
    ) {
        // Step 1: Get connector from station service and validate status
        StationConnectorResponse connector = stationServiceClient.getConnectorById(connectorId, bearerToken);
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
            stationServiceClient.occupyConnector(connectorId, bearerToken);
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
                new TariffSnapshotDto(
                        session.getTariffPricePerKwh(),
                        session.getTariffStartFee(),
                        session.getTariffCurrency()
                )
        );
    }
}
