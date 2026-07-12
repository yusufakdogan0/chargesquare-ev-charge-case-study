package com.yusufakdogan.session_service.dto;

import java.math.BigDecimal;

public record StationTariffResponse(
        Long id,
        BigDecimal pricePerKwh,
        BigDecimal startFee,
        String currency
) {
}
