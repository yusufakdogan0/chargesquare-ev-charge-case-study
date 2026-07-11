package com.yusufakdogan.station_service.repository;

import com.yusufakdogan.station_service.TestDatabaseConfig;
import com.yusufakdogan.station_service.entity.Connector;
import com.yusufakdogan.station_service.entity.Station;
import com.yusufakdogan.station_service.entity.Tariff;
import com.yusufakdogan.station_service.entity.enums.ConnectorStatus;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDatabaseConfig.class)
class ConnectorRepositoryTest {

    @Autowired
    private ConnectorRepository connectorRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private TestEntityManager entityManager;

    // ── Seed Data ──

    @Test
    void shouldLoadSeedConnectors() {
        List<Connector> connectors = connectorRepository.findAll();

        assertThat(connectors).hasSize(2);
    }

    @Test
    void shouldLoadSeedConnectorWithCorrectFields() {
        Connector connector = connectorRepository.findById(1L).orElseThrow();

        assertThat(connector.getType()).isEqualTo("CCS2-DC");
        assertThat(connector.getPowerKw()).isEqualByComparingTo(new BigDecimal("60.0"));
        assertThat(connector.getStatus()).isEqualTo(ConnectorStatus.AVAILABLE);
    }

    // ── CRUD ──

    @Test
    void shouldSaveAndFindConnector() {
        Station station = stationRepository.findById(1L).orElseThrow();
        Tariff tariff = tariffRepository.findById(1L).orElseThrow();

        Connector connector = new Connector();
        connector.setStation(station);
        connector.setTariff(tariff);
        connector.setType("CHAdeMO");
        connector.setPowerKw(new BigDecimal("50.0"));
        connector.setStatus(ConnectorStatus.AVAILABLE);

        Connector saved = connectorRepository.save(connector);
        entityManager.flush();
        entityManager.clear();

        Optional<Connector> found = connectorRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo("CHAdeMO");
    }

    // ── Query Methods ──

    @Test
    void shouldFindAllByStationId() {
        // Seed data has 2 connectors for station 1
        List<Connector> connectors = connectorRepository.findAllByStationId(1L);

        assertThat(connectors).hasSize(2);
        assertThat(connectors).allSatisfy(c ->
                assertThat(c.getStation().getId()).isEqualTo(1L)
        );
    }

    @Test
    void shouldReturnEmptyListForNonExistentStation() {
        List<Connector> connectors = connectorRepository.findAllByStationId(999L);

        assertThat(connectors).isEmpty();
    }

    // ── Enum Persistence ──

    @Test
    void shouldPersistConnectorStatusAsString() {
        Station station = stationRepository.findById(1L).orElseThrow();
        Tariff tariff = tariffRepository.findById(1L).orElseThrow();

        Connector connector = new Connector();
        connector.setStation(station);
        connector.setTariff(tariff);
        connector.setType("CCS1-DC");
        connector.setPowerKw(new BigDecimal("150.0"));
        connector.setStatus(ConnectorStatus.OCCUPIED);

        connectorRepository.save(connector);
        entityManager.flush();
        entityManager.clear();

        Connector found = connectorRepository.findById(connector.getId()).orElseThrow();

        assertThat(found.getStatus()).isEqualTo(ConnectorStatus.OCCUPIED);

        // Verify it's stored as a string in the database, not an ordinal
        Object rawStatus = entityManager.getEntityManager()
                .createNativeQuery("SELECT status FROM station.connectors WHERE id = :id")
                .setParameter("id", connector.getId())
                .getSingleResult();
        assertThat(rawStatus).isEqualTo("OCCUPIED");
    }

    // ── Decimal Precision ──

    @Test
    void shouldPersistPowerKwPrecision() {
        Station station = stationRepository.findById(1L).orElseThrow();
        Tariff tariff = tariffRepository.findById(1L).orElseThrow();

        Connector connector = new Connector();
        connector.setStation(station);
        connector.setTariff(tariff);
        connector.setType("Type2-AC");
        connector.setPowerKw(new BigDecimal("22.5"));
        connector.setStatus(ConnectorStatus.AVAILABLE);

        connectorRepository.save(connector);
        entityManager.flush();
        entityManager.clear();

        Connector found = connectorRepository.findById(connector.getId()).orElseThrow();

        // NUMERIC(6,1) should preserve 1 decimal place
        assertThat(found.getPowerKw()).isEqualByComparingTo(new BigDecimal("22.5"));
    }

    // ── Lazy Loading ──

    @Test
    void shouldLazyLoadStationAndTariff() {
        entityManager.clear();

        Connector connector = connectorRepository.findById(1L).orElseThrow();

        // ManyToOne with LAZY fetch — should not be initialized yet
        assertThat(Hibernate.isInitialized(connector.getStation())).isFalse();
        assertThat(Hibernate.isInitialized(connector.getTariff())).isFalse();

        // Accessing the field triggers the lazy load within the transaction
        String stationName = connector.getStation().getName();
        assertThat(stationName).isEqualTo("ChargeSquare Downtown");
        assertThat(Hibernate.isInitialized(connector.getStation())).isTrue();

        BigDecimal price = connector.getTariff().getPricePerKwh();
        assertThat(price).isEqualByComparingTo(new BigDecimal("8.5000"));
        assertThat(Hibernate.isInitialized(connector.getTariff())).isTrue();
    }
}
