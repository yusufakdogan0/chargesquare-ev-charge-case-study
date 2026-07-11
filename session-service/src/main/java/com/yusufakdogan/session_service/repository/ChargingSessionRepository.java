package com.yusufakdogan.session_service.repository;

import com.yusufakdogan.session_service.entity.ChargingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChargingSessionRepository extends JpaRepository<ChargingSession, Long> {

    List<ChargingSession> findAllByUserId(Long userId);
}
