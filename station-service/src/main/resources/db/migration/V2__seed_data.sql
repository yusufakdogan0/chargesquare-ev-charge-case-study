-- =============================================
-- Seed Data
-- =============================================

-- Tariffs
INSERT INTO tariffs (id, price_per_kwh, start_fee, currency) VALUES
    (1, 8.5000, 2.00, 'TRY'),
    (2, 5.0000, 0.00, 'TRY');

-- Stations
INSERT INTO stations (id, name) VALUES
    (1, 'ChargeSquare Downtown');

-- Connectors
INSERT INTO connectors (id, station_id, tariff_id, type, power_kw, status) VALUES
    (1, 1, 1, 'CCS2-DC',   60.0, 'AVAILABLE'),
    (2, 1, 2, 'Type2-AC',  22.0, 'AVAILABLE');

-- Reset sequences to avoid PK collisions on future inserts
SELECT setval('tariffs_id_seq',    (SELECT MAX(id) FROM tariffs));
SELECT setval('stations_id_seq',   (SELECT MAX(id) FROM stations));
SELECT setval('connectors_id_seq', (SELECT MAX(id) FROM connectors));
