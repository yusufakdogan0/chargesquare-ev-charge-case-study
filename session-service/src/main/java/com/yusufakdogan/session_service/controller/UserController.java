package com.yusufakdogan.session_service.controller;

import com.yusufakdogan.session_service.dto.SessionResponse;
import com.yusufakdogan.session_service.dto.WalletTopUpRequest;
import com.yusufakdogan.session_service.facade.SessionFacade;
import com.yusufakdogan.session_service.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User operations")
public class UserController {

    private final SessionFacade sessionFacade;
    private final WalletService walletService;

    @GetMapping("/{userId}/sessions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user's sessions",
            description = "Returns all charging sessions for a specific user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public List<SessionResponse> getUserSessions(@PathVariable Long userId) {
        return sessionFacade.getUserSessions(userId);
    }

    @PutMapping("/{userId}/wallet/top-up")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Top up user wallet",
            description = "Adds funds to a user's wallet (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet topped up successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public String topUpWallet(
            @PathVariable Long userId,
            @Valid @RequestBody WalletTopUpRequest request
    ) {
        BigDecimal newBalance = walletService.topUp(userId, request.getAmount());
        return "Wallet topped up successfully. New balance: " + newBalance;
    }
}
