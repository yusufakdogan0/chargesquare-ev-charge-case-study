package com.yusufakdogan.station_service.service;

import com.yusufakdogan.station_service.entity.Connector;
import com.yusufakdogan.station_service.entity.Station;
import com.yusufakdogan.station_service.entity.Tariff;
import com.yusufakdogan.station_service.entity.enums.ConnectorStatus;
import com.yusufakdogan.station_service.exception.ResourceNotFoundException;
import com.yusufakdogan.station_service.repository.ConnectorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectorServiceTest {

    @Mock
    private ConnectorRepository connectorRepository;

    @InjectMocks
    private ConnectorService connectorService;

    private Connector createConnector(Long id) {
        Station station = new Station();
        station.setId(1L);
        station.setName("Downtown");

        Tariff tariff = new Tariff();
        tariff.setId(1L);
        tariff.setPricePerKwh(new BigDecimal("8.5000"));
        tariff.setStartFee(new BigDecimal("2.00"));
        tariff.setCurrency("TRY");

        Connector connector = new Connector();
        connector.setId(id);
        connector.setStation(station);
        connector.setTariff(tariff);
        connector.setType("CCS2-DC");
        connector.setPowerKw(new BigDecimal("60.0"));
        connector.setStatus(ConnectorStatus.AVAILABLE);
        return connector;
    }

    @Test
    void findAllByStationId_shouldReturnConnectors() {
        Connector c1 = createConnector(1L);
        Connector c2 = createConnector(2L);
        c2.setType("Type2-AC");
        when(connectorRepository.findAllByStationId(1L)).thenReturn(List.of(c1, c2));

        List<Connector> result = connectorService.findAllByStationId(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    void findAllByStationId_shouldReturnEmptyList_whenNoConnectors() {
        when(connectorRepository.findAllByStationId(999L)).thenReturn(List.of());

        List<Connector> result = connectorService.findAllByStationId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findById_shouldReturnConnector_whenExists() {
        Connector connector = createConnector(1L);
        when(connectorRepository.findById(1L)).thenReturn(Optional.of(connector));

        Connector result = connectorService.findById(1L);

        assertThat(result.getType()).isEqualTo("CCS2-DC");
        assertThat(result.getStatus()).isEqualTo(ConnectorStatus.AVAILABLE);
    }

    @Test
    void findById_shouldThrowResourceNotFoundException_whenNotExists() {
        when(connectorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> connectorService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Connector not found with id: 999");
    }
}
