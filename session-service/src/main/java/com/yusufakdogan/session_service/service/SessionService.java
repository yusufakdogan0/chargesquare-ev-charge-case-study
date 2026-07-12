package com.yusufakdogan.session_service.service;

import com.yusufakdogan.session_service.entity.ChargingSession;
import com.yusufakdogan.session_service.entity.User;
import com.yusufakdogan.session_service.entity.enums.SessionStatus;
import com.yusufakdogan.session_service.exception.ResourceNotFoundException;
import com.yusufakdogan.session_service.repository.ChargingSessionRepository;
import com.yusufakdogan.session_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final ChargingSessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChargingSession createSession(
            Long userId,
            Long connectorId,
            BigDecimal tariffPricePerKwh,
            BigDecimal tariffStartFee,
            String tariffCurrency
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        ChargingSession session = new ChargingSession();
        session.setUser(user);
        session.setConnectorId(connectorId);
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(Instant.now());
        session.setTariffPricePerKwh(tariffPricePerKwh);
        session.setTariffStartFee(tariffStartFee);
        session.setTariffCurrency(tariffCurrency);
        
        return sessionRepository.save(session);
    }
}
