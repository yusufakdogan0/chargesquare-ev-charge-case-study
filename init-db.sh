#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "chargesquare" <<-EOSQL
    CREATE SCHEMA IF NOT EXISTS station;
    CREATE SCHEMA IF NOT EXISTS session;
EOSQL
