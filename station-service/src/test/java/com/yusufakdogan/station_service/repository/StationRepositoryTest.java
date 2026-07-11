package com.yusufakdogan.station_service.repository;

import com.yusufakdogan.station_service.TestDatabaseConfig;
import com.yusufakdogan.station_service.entity.Station;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDatabaseConfig.class)
class StationRepositoryTest {

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldLoadSeedData() {
        Optional<Station> station = stationRepository.findById(1L);

        assertThat(station).isPresent();
        assertThat(station.get().getName()).isEqualTo("ChargeSquare Downtown");
    }

    @Test
    void shouldSaveAndFindStation() {
        Station station = new Station();
        station.setName("ChargeSquare Airport");

        Station saved = stationRepository.save(station);
        entityManager.flush();
        entityManager.clear();

        Optional<Station> found = stationRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getName()).isEqualTo("ChargeSquare Airport");
    }
}
