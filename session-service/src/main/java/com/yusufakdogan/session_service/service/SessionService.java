package com.yusufakdogan.session_service.service;

import com.yusufakdogan.session_service.entity.ChargingSession;
import com.yusufakdogan.session_service.entity.User;
import com.yusufakdogan.session_service.entity.enums.SessionStatus;
import com.yusufakdogan.session_service.exception.NegativeBalanceException;
import com.yusufakdogan.session_service.exception.ResourceNotFoundException;
import com.yusufakdogan.session_service.exception.SessionNotActiveException;
import com.yusufakdogan.session_service.repository.ChargingSessionRepository;
import com.yusufakdogan.session_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

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

        if (user.getWalletBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new NegativeBalanceException(
                    "User has an outstanding negative balance and must settle it before starting another session"
            );
        }

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

    public ChargingSession getSession(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
    }

    public List<ChargingSession> getUserSessions(Long userId) {
        return sessionRepository.findAllByUserId(userId);
    }

    @Transactional
    public ChargingSession completeSession(Long sessionId, BigDecimal energyKwh, BigDecimal cost) {
        ChargingSession session = getSession(sessionId);

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new SessionNotActiveException("Session " + sessionId + " is not ACTIVE and cannot be completed");
        }

        session.setStatus(SessionStatus.COMPLETED);
        session.setEndedAt(Instant.now());
        session.setEnergyKwh(energyKwh);
        session.setCost(cost);
        session.setCurrency(session.getTariffCurrency());

        return sessionRepository.save(session);
    }
}
