package com.yusufakdogan.station_service.controller;

import com.yusufakdogan.station_service.dto.ConnectorResponse;
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

@RestController
@RequestMapping("/connectors")
@RequiredArgsConstructor
@Tag(name = "Connectors", description = "Connector read operations")
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
}
