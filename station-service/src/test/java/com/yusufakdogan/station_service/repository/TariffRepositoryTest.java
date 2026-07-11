package com.yusufakdogan.station_service.repository;

import com.yusufakdogan.station_service.TestDatabaseConfig;
import com.yusufakdogan.station_service.entity.Tariff;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDatabaseConfig.class)
class TariffRepositoryTest {

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldLoadSeedData() {
        Optional<Tariff> tariff = tariffRepository.findById(1L);

        assertThat(tariff).isPresent();
        assertThat(tariff.get().getPricePerKwh()).isEqualByComparingTo(new BigDecimal("8.5000"));
        assertThat(tariff.get().getStartFee()).isEqualByComparingTo(new BigDecimal("2.00"));
        assertThat(tariff.get().getCurrency()).isEqualTo("TRY");
    }

    @Test
    void shouldSaveAndFindTariff() {
        Tariff tariff = new Tariff();
        tariff.setPricePerKwh(new BigDecimal("12.3456"));
        tariff.setStartFee(new BigDecimal("3.50"));
        tariff.setCurrency("EUR");

        Tariff saved = tariffRepository.save(tariff);
        entityManager.flush();
        entityManager.clear();

        Optional<Tariff> found = tariffRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getCurrency()).isEqualTo("EUR");
    }

    @Test
    void shouldPersistDecimalPrecision() {
        Tariff tariff = new Tariff();
        tariff.setPricePerKwh(new BigDecimal("7.1234"));
        tariff.setStartFee(new BigDecimal("1.99"));
        tariff.setCurrency("TRY");

        tariffRepository.save(tariff);
        entityManager.flush();
        entityManager.clear();

        Tariff found = tariffRepository.findById(tariff.getId()).orElseThrow();

        // NUMERIC(10,4) should preserve 4 decimal places
        assertThat(found.getPricePerKwh()).isEqualByComparingTo(new BigDecimal("7.1234"));
        // NUMERIC(10,2) should preserve 2 decimal places
        assertThat(found.getStartFee()).isEqualByComparingTo(new BigDecimal("1.99"));
    }
}
