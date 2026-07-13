-- =============================================
-- Seed Data
-- =============================================

-- Tariffs
INSERT INTO tariffs (id, price_per_kwh, start_fee, currency) VALUES
    (1, 8.50, 2.00, 'TRY'),   -- Fast DC
    (2, 5.00, 0.00, 'TRY'),   -- Standard AC
    (3, 9.75, 5.00, 'TRY'),   -- Ultra Fast DC
    (4, 6.25, 1.50, 'TRY');   -- Premium AC

-- Stations
INSERT INTO stations (id, name) VALUES
    (1, 'ChargeSquare Downtown'),
    (2, 'ChargeSquare Airport'),
    (3, 'ChargeSquare University'),
    (4, 'ChargeSquare Shopping Mall');

-- Connectors
INSERT INTO connectors (id, station_id, tariff_id, type, power_kw, status) VALUES
    -- Downtown
    (1, 1, 1, 'CCS2-DC', 60.0,  'AVAILABLE'),
    (2, 1, 2, 'Type2-AC', 22.0, 'AVAILABLE'),
    (3, 1, 2, 'Type2-AC', 11.0, 'AVAILABLE'),

    -- Airport
    (4, 2, 3, 'CCS2-DC', 150.0, 'AVAILABLE'),
    (5, 2, 3, 'CCS2-DC', 120.0, 'AVAILABLE'),
    (6, 2, 2, 'Type2-AC', 22.0, 'AVAILABLE'),

    -- University
    (7, 3, 2, 'Type2-AC', 11.0, 'AVAILABLE'),
    (8, 3, 4, 'Type2-AC', 22.0, 'AVAILABLE'),
    (9, 3, 1, 'CCS2-DC', 50.0,  'AVAILABLE'),

    -- Shopping Mall
    (10, 4, 1, 'CCS2-DC', 60.0, 'AVAILABLE'),
    (11, 4, 4, 'Type2-AC', 22.0, 'AVAILABLE'),
    (12, 4, 2, 'Type2-AC', 11.0, 'AVAILABLE');

-- Reset sequences to avoid PK collisions on future inserts
SELECT setval('tariffs_id_seq', (SELECT MAX(id) FROM tariffs));
SELECT setval('stations_id_seq', (SELECT MAX(id) FROM stations));
SELECT setval('connectors_id_seq', (SELECT MAX(id) FROM connectors));