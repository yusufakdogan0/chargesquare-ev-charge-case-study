package com.yusufakdogan.station_service.dto;

import java.math.BigDecimal;

public record TariffResponse(
        Long id,
        BigDecimal pricePerKwh,
        BigDecimal startFee,
        String currency
) {
}
