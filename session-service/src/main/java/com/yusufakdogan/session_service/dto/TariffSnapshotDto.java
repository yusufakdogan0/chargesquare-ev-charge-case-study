package com.yusufakdogan.session_service.dto;

import java.math.BigDecimal;

public record TariffSnapshotDto(
        BigDecimal pricePerKwh,
        BigDecimal startFee,
        String currency
) {
}
