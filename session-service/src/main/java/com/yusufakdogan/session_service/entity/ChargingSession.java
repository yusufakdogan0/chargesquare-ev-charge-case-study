package com.yusufakdogan.session_service.entity;

import com.yusufakdogan.session_service.entity.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "charging_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChargingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "connector_id", nullable = false)
    private Long connectorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "energy_kwh", precision = 10, scale = 4)
    private BigDecimal energyKwh;

    @Column(precision = 12, scale = 2)
    private BigDecimal cost;

    @Column(length = 3)
    private String currency;

    // ── Tariff snapshot (captured at session start) ──

    @Column(name = "tariff_price_per_kwh", precision = 10, scale = 4)
    private BigDecimal tariffPricePerKwh;

    @Column(name = "tariff_start_fee", precision = 10, scale = 2)
    private BigDecimal tariffStartFee;

    @Column(name = "tariff_currency", length = 3, nullable = false)
    private String tariffCurrency;
}
