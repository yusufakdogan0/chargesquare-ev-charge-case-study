# ChargeSquare EV Charging — Case Study

A backend system for EV charging session management, built with **Java 21** and **Spring Boot 3.5**.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.16 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Gradle 8.14 |
| Containers | Docker / Docker Compose |

## Project Structure

```
chargesquare-ev-charge-case-study/
├── station-service/          # Stations, connectors, tariffs
├── session-service/          # Charging sessions, users, wallets
├── docker-compose.yml        # PostgreSQL + services
├── init-db.sh                # Creates DB schemas (station, session)
├── .env.example              # Environment variable template
└── README.md
```

## Architecture

Two Spring Boot services share a single PostgreSQL database (`chargesquare`), each owning its own schema:

- **Station Service** (`station` schema, port `8081`) — source of truth for charging stations, connectors, and tariffs.
- **Session Service** (`session` schema, port `8082`) — charging sessions, users, and wallets. Calls Station Service over REST.

## Prerequisites

- Java 21
- Docker & Docker Compose

## Getting Started

### 1. Set up environment variables

```bash
cp .env.example .env
# Edit .env with your values (defaults work for local dev)
```

### 2. Start PostgreSQL

```bash
docker compose up postgres -d
```

This creates the `chargesquare` database with `station` and `session` schemas.

### 3. Start Station Service

```bash
cd station-service
./gradlew bootRun
```

Starts on `http://localhost:8081`.

### 4. Start Session Service

```bash
cd session-service
./gradlew bootRun
```

Starts on `http://localhost:8082`.

## Available Endpoints

### Station Service (`http://localhost:8081`)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/actuator/health` | Health check (Actuator) |
| GET | `/swagger-ui.html` | Swagger UI |
| GET | `/v3/api-docs` | OpenAPI spec (JSON) |

### Session Service (`http://localhost:8082`)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/actuator/health` | Health check (Actuator) |
| GET | `/swagger-ui.html` | Swagger UI |
| GET | `/v3/api-docs` | OpenAPI spec (JSON) |

## Configuration

All configuration is driven by environment variables with sensible defaults for local development. See [.env.example](.env.example) for the full list.

| Variable | Default | Description |
|---|---|---|
| `DB_USER` | `postgres` | PostgreSQL username |
| `DB_PASSWORD` | — | PostgreSQL password |
| `JWT_SECRET` | — | JWT signing key |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/chargesquare` | JDBC URL |
| `SERVER_PORT` | `8081` / `8082` | Service port |
| `STATION_SERVICE_URL` | `http://localhost:8081` | Station Service base URL (Session Service only) |

## Database

Single PostgreSQL instance with schema-based separation:

- `station` schema — managed by Station Service's Flyway migrations
- `session` schema — managed by Session Service's Flyway migrations

Flyway runs automatically on startup, creating and migrating tables from scratch.
