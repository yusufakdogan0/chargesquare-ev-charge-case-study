package com.yusufakdogan.station_service.facade;

import com.yusufakdogan.station_service.dto.ConnectorResponse;
import com.yusufakdogan.station_service.dto.StationDetailResponse;
import com.yusufakdogan.station_service.dto.StationResponse;
import com.yusufakdogan.station_service.entity.Connector;
import com.yusufakdogan.station_service.entity.Station;
import com.yusufakdogan.station_service.mapper.ConnectorMapper;
import com.yusufakdogan.station_service.mapper.StationMapper;
import com.yusufakdogan.station_service.service.ConnectorService;
import com.yusufakdogan.station_service.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StationFacade {

    private final StationService stationService;
    private final ConnectorService connectorService;
    private final StationMapper stationMapper;
    private final ConnectorMapper connectorMapper;

    public List<StationResponse> getAllStations() {
        List<Station> stations = stationService.findAll();
        return stationMapper.toResponseList(stations);
    }

    public StationDetailResponse getStationById(Long id) {
        Station station = stationService.findById(id);
        List<Connector> connectors = connectorService.findAllByStationId(id);
        return stationMapper.toDetailResponse(station, connectors);
    }

    public List<ConnectorResponse> getConnectorsByStationId(Long stationId) {
        stationService.findById(stationId); // validates station exists
        List<Connector> connectors = connectorService.findAllByStationId(stationId);
        return connectorMapper.toResponseList(connectors);
    }

    public ConnectorResponse getConnectorById(Long id) {
        Connector connector = connectorService.findById(id);
        return connectorMapper.toResponse(connector);
    }

    @Transactional
    public ConnectorResponse occupyConnector(Long id) {
        Connector connector = connectorService.occupyConnector(id);
        return connectorMapper.toResponse(connector);
    }
}
