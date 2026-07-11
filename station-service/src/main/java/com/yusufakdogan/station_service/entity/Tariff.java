package com.yusufakdogan.station_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tariffs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "price_per_kwh", nullable = false, precision = 10, scale = 4)
    private BigDecimal pricePerKwh;

    @Column(name = "start_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal startFee;

    @Column(nullable = false, length = 3)
    private String currency;
}
