package com.yusufakdogan.station_service.repository;

import com.yusufakdogan.station_service.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<Station, Long> {
}
