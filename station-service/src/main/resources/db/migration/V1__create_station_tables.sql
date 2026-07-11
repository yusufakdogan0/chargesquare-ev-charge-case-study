-- =============================================
-- Station Service Schema
-- =============================================

-- Tariffs: pricing rules for connectors
CREATE TABLE tariffs (
    id            BIGSERIAL    PRIMARY KEY,
    price_per_kwh NUMERIC(10,4) NOT NULL,
    start_fee     NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    currency      VARCHAR(3)    NOT NULL DEFAULT 'TRY'
);

-- Stations: physical charging locations
CREATE TABLE stations (
    id   BIGSERIAL    PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Connectors (EVSE): individual charge points at a station
CREATE TABLE connectors (
    id         BIGSERIAL    PRIMARY KEY,
    station_id BIGINT       NOT NULL REFERENCES stations(id),
    tariff_id  BIGINT       NOT NULL REFERENCES tariffs(id),
    type       VARCHAR(30)  NOT NULL,
    power_kw   NUMERIC(6,1) NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE'
        CHECK (status IN ('AVAILABLE', 'OCCUPIED'))
);
