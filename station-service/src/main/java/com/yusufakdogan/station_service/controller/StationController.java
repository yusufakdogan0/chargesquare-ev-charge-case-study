package com.yusufakdogan.station_service.controller;

import com.yusufakdogan.station_service.dto.ConnectorResponse;
import com.yusufakdogan.station_service.dto.StationDetailResponse;
import com.yusufakdogan.station_service.dto.StationResponse;
import com.yusufakdogan.station_service.facade.StationFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stations")
@RequiredArgsConstructor
@Tag(name = "Stations", description = "Charging station read operations")
public class StationController {

    private final StationFacade stationFacade;

    @GetMapping
    @Operation(summary = "List all stations", description = "Returns all charging stations.")
    @ApiResponse(responseCode = "200", description = "Stations retrieved successfully")
    public List<StationResponse> getAllStations() {
        return stationFacade.getAllStations();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get station by ID", description = "Returns a station with its connectors.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Station retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Station not found")
    })
    public StationDetailResponse getStationById(@PathVariable Long id) {
        return stationFacade.getStationById(id);
    }

    @GetMapping("/{id}/connectors")
    @Operation(summary = "List connectors for a station",
            description = "Returns all connectors belonging to the specified station.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Connectors retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Station not found")
    })
    public List<ConnectorResponse> getConnectorsByStationId(@PathVariable Long id) {
        return stationFacade.getConnectorsByStationId(id);
    }
}
