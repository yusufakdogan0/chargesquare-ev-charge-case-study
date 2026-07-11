package com.yusufakdogan.station_service.repository;

import com.yusufakdogan.station_service.entity.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TariffRepository extends JpaRepository<Tariff, Long> {
}
