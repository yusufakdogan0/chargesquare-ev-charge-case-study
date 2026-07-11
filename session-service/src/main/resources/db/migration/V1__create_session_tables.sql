-- =============================================
-- Session Service Schema
-- =============================================

-- Users: drivers with wallet balances
CREATE TABLE users (
    id             BIGSERIAL     PRIMARY KEY,
    username       VARCHAR(50)   NOT NULL UNIQUE,
    password       VARCHAR(255)  NOT NULL,
    role           VARCHAR(20)   NOT NULL DEFAULT 'VIEWER',
    wallet_balance NUMERIC(12,2) NOT NULL DEFAULT 0.00
);

-- Charging sessions: the core domain object
CREATE TABLE charging_sessions (
    id                   BIGSERIAL              PRIMARY KEY,
    user_id              BIGINT                 NOT NULL REFERENCES users(id),
    connector_id         BIGINT                 NOT NULL,
    status               VARCHAR(20)            NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'COMPLETED')),
    started_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at             TIMESTAMP WITH TIME ZONE,
    energy_kwh           NUMERIC(10,4),
    cost                 NUMERIC(12,2),
    currency             VARCHAR(3),
    tariff_price_per_kwh NUMERIC(10,4),
    tariff_start_fee     NUMERIC(10,2),
    tariff_currency      VARCHAR(3)    NOT NULL
);
