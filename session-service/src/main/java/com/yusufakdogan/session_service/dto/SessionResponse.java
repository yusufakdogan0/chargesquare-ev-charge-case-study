package com.yusufakdogan.session_service.dto;

import com.yusufakdogan.session_service.entity.enums.SessionStatus;

import java.time.Instant;

public record SessionResponse(
        Long sessionId,
        SessionStatus status,
        Long connectorId,
        Instant startedAt,
        TariffSnapshotDto tariff
) {
}
