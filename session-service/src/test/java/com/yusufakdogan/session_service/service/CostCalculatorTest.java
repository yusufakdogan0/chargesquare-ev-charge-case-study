package com.yusufakdogan.session_service.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CostCalculatorTest {

    private final CostCalculator costCalculator = new CostCalculator();

    @Test
    void shouldCalculateCostWithRequiredWorkedExample() {
        // Required worked example from case study: 12.5 kWh × 8.50/kWh + 2.00 = 108.25
        BigDecimal energyKwh = new BigDecimal("12.5");
        BigDecimal pricePerKwh = new BigDecimal("8.50");
        BigDecimal startFee = new BigDecimal("2.00");

        BigDecimal result = costCalculator.calculate(energyKwh, pricePerKwh, startFee);

        assertThat(result).isEqualByComparingTo("108.25");
    }

    @Test
    void shouldCalculateCostWithZeroStartFee() {
        BigDecimal energyKwh = new BigDecimal("10.0");
        BigDecimal pricePerKwh = new BigDecimal("5.00");
        BigDecimal startFee = BigDecimal.ZERO;

        BigDecimal result = costCalculator.calculate(energyKwh, pricePerKwh, startFee);

        assertThat(result).isEqualByComparingTo("50.00");
    }

    @Test
    void shouldCalculateCostWithZeroEnergy() {
        BigDecimal energyKwh = BigDecimal.ZERO;
        BigDecimal pricePerKwh = new BigDecimal("8.50");
        BigDecimal startFee = new BigDecimal("2.00");

        BigDecimal result = costCalculator.calculate(energyKwh, pricePerKwh, startFee);

        assertThat(result).isEqualByComparingTo("2.00");
    }

    @Test
    void shouldRoundToTwoDecimalPlacesWithHalfUp() {
        // Test rounding: 12.345 kWh × 8.50/kWh + 2.00 = 106.9325 → 106.93
        BigDecimal energyKwh = new BigDecimal("12.345");
        BigDecimal pricePerKwh = new BigDecimal("8.50");
        BigDecimal startFee = new BigDecimal("2.00");

        BigDecimal result = costCalculator.calculate(energyKwh, pricePerKwh, startFee);

        assertThat(result).isEqualByComparingTo("106.93");
    }

    @Test
    void shouldRoundUpWhenThirdDecimalIsFiveOrMore() {
        // Test rounding up: 9.0 kWh × 8.505/kWh + 2.00 = 78.55 → 78.55 (no change needed)
        BigDecimal energyKwh = new BigDecimal("9.0");
        BigDecimal pricePerKwh = new BigDecimal("8.505");
        BigDecimal startFee = new BigDecimal("2.00");

        BigDecimal result = costCalculator.calculate(energyKwh, pricePerKwh, startFee);

        assertThat(result).isEqualByComparingTo("78.55");
    }

    @Test
    void shouldHandleLargeEnergyValues() {
        BigDecimal energyKwh = new BigDecimal("100.0");
        BigDecimal pricePerKwh = new BigDecimal("8.50");
        BigDecimal startFee = new BigDecimal("2.00");

        BigDecimal result = costCalculator.calculate(energyKwh, pricePerKwh, startFee);

        assertThat(result).isEqualByComparingTo("852.00");
    }
}
