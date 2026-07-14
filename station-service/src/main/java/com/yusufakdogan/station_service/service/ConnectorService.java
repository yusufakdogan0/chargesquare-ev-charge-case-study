package com.yusufakdogan.station_service.service;

import com.yusufakdogan.station_service.entity.Connector;
import com.yusufakdogan.station_service.entity.enums.ConnectorStatus;
import com.yusufakdogan.station_service.exception.ConnectorOccupiedException;
import com.yusufakdogan.station_service.exception.ResourceNotFoundException;
import com.yusufakdogan.station_service.repository.ConnectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.context.SecurityContextHolder;
import com.yusufakdogan.station_service.security.JwtAuthenticationFilter;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectorService {

    private final ConnectorRepository connectorRepository;

    public List<Connector> findAllByStationId(Long stationId) {
        return connectorRepository.findAllByStationId(stationId);
    }

    public Connector findById(Long id) {
        return connectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Connector not found with id: " + id));
    }

    private String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof JwtAuthenticationFilter.AuthenticatedUser user) {
            return String.valueOf(user.userId);
        }
        return "UNKNOWN";
    }

    @Transactional
    public Connector occupyConnector(Long id) {
        String userId = getCurrentUserId();
        log.info("Attempting to occupy connector {} by user {}", id, userId);
        Connector connector = findById(id);
        
        if (connector.getStatus() == ConnectorStatus.OCCUPIED) {
            log.warn("Failed to occupy connector {} by user {}. It is already occupied.", id, userId);
            throw new ConnectorOccupiedException(
                    "Connector " + id + " is already occupied");
        }
        
        connector.setStatus(ConnectorStatus.OCCUPIED);
        Connector savedConnector = connectorRepository.save(connector);
        log.info("Successfully occupied connector {} by user {}", id, userId);
        return savedConnector;
    }

    @Transactional
    public Connector releaseConnector(Long id) {
        String userId = getCurrentUserId();
        log.info("Attempting to release connector {} by user {}", id, userId);
        Connector connector = findById(id);
        
        if (connector.getStatus() == ConnectorStatus.AVAILABLE) {
            log.warn("Failed to release connector {} by user {}. It is already available.", id, userId);
            throw new ConnectorOccupiedException(
                    "Connector " + id + " is already available");
        }
        
        connector.setStatus(ConnectorStatus.AVAILABLE);
        Connector savedConnector = connectorRepository.save(connector);
        log.info("Successfully released connector {} by user {}", id, userId);
        return savedConnector;
    }
}
