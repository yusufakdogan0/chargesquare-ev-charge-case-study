package com.yusufakdogan.station_service.mapper;

import com.yusufakdogan.station_service.dto.ConnectorResponse;
import com.yusufakdogan.station_service.entity.Connector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConnectorMapper {

    private final TariffMapper tariffMapper;

    public ConnectorResponse toResponse(Connector connector) {
        return new ConnectorResponse(
                connector.getId(),
                connector.getStation().getId(),
                connector.getType(),
                connector.getPowerKw(),
                connector.getStatus(),
                tariffMapper.toResponse(connector.getTariff())
        );
    }

    public List<ConnectorResponse> toResponseList(List<Connector> connectors) {
        return connectors.stream()
                .map(this::toResponse)
                .toList();
    }
}
