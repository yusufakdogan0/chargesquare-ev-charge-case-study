package com.yusufakdogan.session_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StopSessionRequest {

    @NotNull(message = "Energy kWh is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Energy kWh must be positive")
    private BigDecimal energyKwh;
}
