package com.yusufakdogan.session_service.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CostCalculator {

    /**
     * Calculates the cost for a charging session.
     * Formula: energyKwh × pricePerKwh + startFee
     * Result is rounded to 2 decimal places using HALF_UP rounding mode.
     *
     * @param energyKwh   Energy consumed in kWh
     * @param pricePerKwh Price per kWh from tariff
     * @param startFee    Fixed start fee from tariff
     * @return Total cost rounded to 2 decimal places
     */
    public BigDecimal calculate(BigDecimal energyKwh, BigDecimal pricePerKwh, BigDecimal startFee) {
        BigDecimal energyCost = energyKwh.multiply(pricePerKwh);
        BigDecimal totalCost = energyCost.add(startFee);
        return totalCost.setScale(2, RoundingMode.HALF_UP);
    }
}
