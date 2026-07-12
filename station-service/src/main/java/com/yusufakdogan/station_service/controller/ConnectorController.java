package com.yusufakdogan.station_service.controller;

import com.yusufakdogan.station_service.dto.ConnectorResponse;
import com.yusufakdogan.station_service.facade.StationFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/connectors")
@RequiredArgsConstructor
@Tag(name = "Connectors", description = "Connector operations")
public class ConnectorController {

    private final StationFacade stationFacade;

    @GetMapping("/{id}")
    @Operation(summary = "Get connector by ID",
            description = "Returns a single connector with its tariff information.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Connector retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Connector not found")
    })
    public ConnectorResponse getConnectorById(@PathVariable Long id) {
        return stationFacade.getConnectorById(id);
    }

    @PatchMapping("/{id}/occupy")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Occupy connector",
            description = "Marks a connector as OCCUPIED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Connector occupied successfully"),
            @ApiResponse(responseCode = "404", description = "Connector not found"),
            @ApiResponse(responseCode = "409", description = "Connector already occupied")
    })
    public ConnectorResponse occupyConnector(@PathVariable Long id) {
        return stationFacade.occupyConnector(id);
    }
}
