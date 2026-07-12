package com.yusufakdogan.station_service.service;

import com.yusufakdogan.station_service.entity.Connector;
import com.yusufakdogan.station_service.entity.enums.ConnectorStatus;
import com.yusufakdogan.station_service.exception.ConnectorOccupiedException;
import com.yusufakdogan.station_service.exception.ResourceNotFoundException;
import com.yusufakdogan.station_service.repository.ConnectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional
    public Connector occupyConnector(Long id) {
        Connector connector = findById(id);
        
        if (connector.getStatus() == ConnectorStatus.OCCUPIED) {
            throw new ConnectorOccupiedException(
                    "Connector " + id + " is already occupied");
        }
        
        connector.setStatus(ConnectorStatus.OCCUPIED);
        return connectorRepository.save(connector);
    }

    @Transactional
    public Connector releaseConnector(Long id) {
        Connector connector = findById(id);
        
        if (connector.getStatus() == ConnectorStatus.AVAILABLE) {
            throw new ConnectorOccupiedException(
                    "Connector " + id + " is already available");
        }
        
        connector.setStatus(ConnectorStatus.AVAILABLE);
        return connectorRepository.save(connector);
    }
}
