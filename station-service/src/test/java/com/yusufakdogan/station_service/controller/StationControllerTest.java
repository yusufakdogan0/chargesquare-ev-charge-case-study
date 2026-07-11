package com.yusufakdogan.station_service.controller;

import com.yusufakdogan.station_service.dto.ConnectorResponse;
import com.yusufakdogan.station_service.dto.StationDetailResponse;
import com.yusufakdogan.station_service.dto.StationResponse;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StationController.class)
@Import(GlobalExceptionHandler.class)
class StationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StationFacade stationFacade;

    private final TariffResponse tariff = new TariffResponse(
            1L, new BigDecimal("8.5000"), new BigDecimal("2.00"), "TRY");

    private final ConnectorResponse connector = new ConnectorResponse(
            1L, 1L, "CCS2-DC", new BigDecimal("60.0"), ConnectorStatus.AVAILABLE, tariff);

    // ── GET /stations ──

    @Test
    void getAllStations_shouldReturn200WithStationList() throws Exception {
        List<StationResponse> stations = List.of(
                new StationResponse(1L, "Downtown"),
                new StationResponse(2L, "Airport")
        );
        when(stationFacade.getAllStations()).thenReturn(stations);

        mockMvc.perform(get("/stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Downtown")))
                .andExpect(jsonPath("$[1].name", is("Airport")));
    }

    @Test
    void getAllStations_shouldReturn200WithEmptyList() throws Exception {
        when(stationFacade.getAllStations()).thenReturn(List.of());

        mockMvc.perform(get("/stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── GET /stations/{id} ──

    @Test
    void getStationById_shouldReturn200WithStationDetail() throws Exception {
        StationDetailResponse detail = new StationDetailResponse(
                1L, "Downtown", List.of(connector));
        when(stationFacade.getStationById(1L)).thenReturn(detail);

        mockMvc.perform(get("/stations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Downtown")))
                .andExpect(jsonPath("$.connectors", hasSize(1)))
                .andExpect(jsonPath("$.connectors[0].type", is("CCS2-DC")))
                .andExpect(jsonPath("$.connectors[0].tariff.pricePerKwh", is(8.5000)));
    }

    @Test
    void getStationById_shouldReturn404_whenNotFound() throws Exception {
        when(stationFacade.getStationById(999L))
                .thenThrow(new ResourceNotFoundException("Station not found with id: 999"));

        mockMvc.perform(get("/stations/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Resource Not Found")))
                .andExpect(jsonPath("$.detail", is("Station not found with id: 999")));
    }

    // ── GET /stations/{id}/connectors ──

    @Test
    void getConnectorsByStationId_shouldReturn200WithConnectorList() throws Exception {
        when(stationFacade.getConnectorsByStationId(1L)).thenReturn(List.of(connector));

        mockMvc.perform(get("/stations/1/connectors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].stationId", is(1)))
                .andExpect(jsonPath("$[0].type", is("CCS2-DC")))
                .andExpect(jsonPath("$[0].status", is("AVAILABLE")))
                .andExpect(jsonPath("$[0].tariff.currency", is("TRY")));
    }

    @Test
    void getConnectorsByStationId_shouldReturn404_whenStationNotFound() throws Exception {
        when(stationFacade.getConnectorsByStationId(999L))
                .thenThrow(new ResourceNotFoundException("Station not found with id: 999"));

        mockMvc.perform(get("/stations/999/connectors"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Resource Not Found")));
    }
}
