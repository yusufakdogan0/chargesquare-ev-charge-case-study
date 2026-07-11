package com.yusufakdogan.station_service.mapper;

import com.yusufakdogan.station_service.dto.ConnectorResponse;
import com.yusufakdogan.station_service.dto.StationDetailResponse;
import com.yusufakdogan.station_service.dto.StationResponse;
import com.yusufakdogan.station_service.entity.Connector;
import com.yusufakdogan.station_service.entity.Station;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StationMapper {

    private final ConnectorMapper connectorMapper;

    public StationResponse toResponse(Station station) {
        return new StationResponse(station.getId(), station.getName());
    }

    public List<StationResponse> toResponseList(List<Station> stations) {
        return stations.stream()
                .map(this::toResponse)
                .toList();
    }

    public StationDetailResponse toDetailResponse(Station station, List<Connector> connectors) {
        List<ConnectorResponse> connectorResponses = connectorMapper.toResponseList(connectors);
        return new StationDetailResponse(station.getId(), station.getName(), connectorResponses);
    }
}
