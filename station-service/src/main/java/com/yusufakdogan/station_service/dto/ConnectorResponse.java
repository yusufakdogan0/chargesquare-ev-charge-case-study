package com.yusufakdogan.station_service.dto;

import com.yusufakdogan.station_service.entity.enums.ConnectorStatus;

import java.math.BigDecimal;

public record ConnectorResponse(
        Long id,
        Long stationId,
        String type,
        BigDecimal powerKw,
        ConnectorStatus status,
        TariffResponse tariff
) {
}
