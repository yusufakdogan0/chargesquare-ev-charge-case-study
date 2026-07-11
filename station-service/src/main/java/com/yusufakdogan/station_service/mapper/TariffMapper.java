package com.yusufakdogan.station_service.mapper;

import com.yusufakdogan.station_service.dto.TariffResponse;
import com.yusufakdogan.station_service.entity.Tariff;
import org.springframework.stereotype.Component;

@Component
public class TariffMapper {

    public TariffResponse toResponse(Tariff tariff) {
        return new TariffResponse(
                tariff.getId(),
                tariff.getPricePerKwh(),
                tariff.getStartFee(),
                tariff.getCurrency()
        );
    }
}
