package com.yusufakdogan.session_service.dto;

import java.math.BigDecimal;

public record StationConnectorResponse(
        Long id,
        Long stationId,
        String type,
        BigDecimal powerKw,
        String status,
        StationTariffResponse tariff
) {
}
