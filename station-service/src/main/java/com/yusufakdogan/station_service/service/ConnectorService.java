package com.yusufakdogan.station_service.service;

import com.yusufakdogan.station_service.entity.Connector;
import com.yusufakdogan.station_service.exception.ResourceNotFoundException;
import com.yusufakdogan.station_service.repository.ConnectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
