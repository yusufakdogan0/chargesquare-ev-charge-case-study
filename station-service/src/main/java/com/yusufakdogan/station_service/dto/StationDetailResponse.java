package com.yusufakdogan.station_service.dto;

import java.util.List;

public record StationDetailResponse(
        Long id,
        String name,
        List<ConnectorResponse> connectors
) {
}
