package com.yusufakdogan.session_service.controller;

import com.yusufakdogan.session_service.dto.SessionResponse;
import com.yusufakdogan.session_service.dto.StartSessionRequest;
import com.yusufakdogan.session_service.dto.StopSessionRequest;
import com.yusufakdogan.session_service.dto.WalletTopUpRequest;
import com.yusufakdogan.session_service.facade.SessionFacade;
import com.yusufakdogan.session_service.security.JwtAuthenticationFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "Charging session operations")
public class SessionController {

    private final SessionFacade sessionFacade;

    @PostMapping("/start")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Start new charging session",
            description = "Creates new charging session, marks connector as occupied")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session started successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Connector not found"),
            @ApiResponse(responseCode = "409", description = "Connector already occupied")
    })
    public SessionResponse startSession(
            @Valid @RequestBody StartSessionRequest request,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        JwtAuthenticationFilter.AuthenticatedUser user = 
                (JwtAuthenticationFilter.AuthenticatedUser) authentication.getPrincipal();

        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }

        return sessionFacade.startSession(user.userId, request.getConnectorId(), token);
    }

    @PostMapping("/{id}/stop")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Stop charging session",
            description = "Stops an active session, calculates cost, debits wallet, releases connector")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session stopped successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "409", description = "Session not active")
    })
    public SessionResponse stopSession(
            @PathVariable Long id,
            @Valid @RequestBody StopSessionRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }

        return sessionFacade.stopSession(id, request.getEnergyKwh(), token);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get session by ID",
            description = "Returns a charging session with all details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public SessionResponse getSession(@PathVariable Long id) {
        return sessionFacade.getSession(id);
    }
}
