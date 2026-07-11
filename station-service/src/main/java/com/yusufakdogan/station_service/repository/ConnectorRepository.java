package com.yusufakdogan.station_service.repository;

import com.yusufakdogan.station_service.entity.Connector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConnectorRepository extends JpaRepository<Connector, Long> {

    List<Connector> findAllByStationId(Long stationId);
}
