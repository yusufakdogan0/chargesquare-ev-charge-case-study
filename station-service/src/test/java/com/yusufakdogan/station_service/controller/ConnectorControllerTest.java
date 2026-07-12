package com.yusufakdogan.station_service.controller;

import com.yusufakdogan.station_service.dto.ConnectorResponse;
import com.yusufakdogan.station_service.dto.TariffResponse;
import com.yusufakdogan.station_service.entity.enums.ConnectorStatus;
import com.yusufakdogan.station_service.exception.GlobalExceptionHandler;
import com.yusufakdogan.station_service.exception.ResourceNotFoundException;
import com.yusufakdogan.station_service.facade.StationFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConnectorController.class)
@Import(GlobalExceptionHandler.class)
@org.springframework.security.test.context.support.WithMockUser
class ConnectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StationFacade stationFacade;

    @MockitoBean
    private com.yusufakdogan.station_service.security.JwtService jwtService;

    // ── GET /connectors/{id} ──

    @Test
    void getConnectorById_shouldReturn200WithConnector() throws Exception {
        TariffResponse tariff = new TariffResponse(
                1L, new BigDecimal("8.5000"), new BigDecimal("2.00"), "TRY");
        ConnectorResponse connector = new ConnectorResponse(
                1L, 1L, "CCS2-DC", new BigDecimal("60.0"), ConnectorStatus.AVAILABLE, tariff);
        when(stationFacade.getConnectorById(1L)).thenReturn(connector);

        mockMvc.perform(get("/connectors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.stationId", is(1)))
                .andExpect(jsonPath("$.type", is("CCS2-DC")))
                .andExpect(jsonPath("$.powerKw", is(60.0)))
                .andExpect(jsonPath("$.status", is("AVAILABLE")))
                .andExpect(jsonPath("$.tariff.pricePerKwh", is(8.5000)))
                .andExpect(jsonPath("$.tariff.startFee", is(2.00)))
                .andExpect(jsonPath("$.tariff.currency", is("TRY")));
    }

    @Test
    void getConnectorById_shouldReturn404_whenNotFound() throws Exception {
        when(stationFacade.getConnectorById(999L))
                .thenThrow(new ResourceNotFoundException("Connector not found with id: 999"));

        mockMvc.perform(get("/connectors/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Resource Not Found")))
                .andExpect(jsonPath("$.detail", is("Connector not found with id: 999")));
    }
}
