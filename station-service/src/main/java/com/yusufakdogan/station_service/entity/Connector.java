package com.yusufakdogan.station_service.entity;

import com.yusufakdogan.station_service.entity.enums.ConnectorStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "connectors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Connector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(name = "power_kw", nullable = false, precision = 6, scale = 1)
    private BigDecimal powerKw;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConnectorStatus status;
}
