package com.yusufakdogan.station_service.service;

import com.yusufakdogan.station_service.entity.Station;
import com.yusufakdogan.station_service.exception.ResourceNotFoundException;
import com.yusufakdogan.station_service.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private StationService stationService;

    @Test
    void findAll_shouldReturnAllStations() {
        Station s1 = new Station();
        s1.setId(1L);
        s1.setName("Downtown");
        Station s2 = new Station();
        s2.setId(2L);
        s2.setName("Airport");
        when(stationRepository.findAll()).thenReturn(List.of(s1, s2));

        List<Station> result = stationService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Downtown");
    }

    @Test
    void findById_shouldReturnStation_whenExists() {
        Station station = new Station();
        station.setId(1L);
        station.setName("Downtown");
        when(stationRepository.findById(1L)).thenReturn(Optional.of(station));

        Station result = stationService.findById(1L);

        assertThat(result.getName()).isEqualTo("Downtown");
    }

    @Test
    void findById_shouldThrowResourceNotFoundException_whenNotExists() {
        when(stationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stationService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Station not found with id: 999");
    }
}
