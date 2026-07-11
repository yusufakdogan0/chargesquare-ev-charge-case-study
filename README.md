# ChargeSquare EV Charging — Case Study

A backend system for EV charging session management, built with **Java 21** and **Spring Boot 3.5**.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.16 |
| Security | Spring Security + JWT |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Gradle 8.14 |
| Containers | Docker / Docker Compose |
| Testing | JUnit 5, Testcontainers, AssertJ |

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
- **Session Service** (`session` schema, port `8082`) — charging sessions, users, wallets, and authentication. Calls Station Service over REST.

## Authentication & Authorization

The system uses **JWT (JSON Web Tokens)** for stateless authentication with two roles:

- **ADMIN** — Full access to all endpoints
- **VIEWER** — Read-only access

### Default Users

| Username | Password | Role | Wallet Balance |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | 500.00 |
| `viewer` | `viewer123` | VIEWER | 500.00 |

### Getting a Token

Send a POST request to Session Service's login endpoint:

```bash
curl -X POST http://localhost:8082/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Using the Token

Include the JWT in the `Authorization` header for protected endpoints:

```bash
curl http://localhost:8081/stations \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Swagger UI with JWT

Both services' Swagger UIs support JWT authentication:
1. Open `http://localhost:8081/swagger-ui.html` or `http://localhost:8082/swagger-ui.html`
2. Click the **Authorize** button
3. Enter your JWT token and click **Authorize**
4. All subsequent requests will include the token

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

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| GET | `/actuator/health` | Health check (Actuator) | No |
| GET | `/swagger-ui.html` | Swagger UI | No |
| GET | `/v3/api-docs` | OpenAPI spec (JSON) | No |
| GET | `/stations` | List all stations | Yes |
| GET | `/stations/{id}` | Get station by ID with connectors | Yes |
| GET | `/stations/{id}/connectors` | List connectors for a station | Yes |
| GET | `/connectors/{id}` | Get connector by ID with tariff | Yes |

### Session Service (`http://localhost:8082`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| GET | `/actuator/health` | Health check (Actuator) | No |
| GET | `/swagger-ui.html` | Swagger UI | No |
| GET | `/v3/api-docs` | OpenAPI spec (JSON) | No |
| POST | `/auth/login` | Login and get JWT token | No |

## Configuration

All configuration is driven by environment variables with sensible defaults for local development. See [.env.example](.env.example) for the full list.

| Variable | Default | Description |
|---|---|---|
| `POSTGRES_USER` | `postgres` | PostgreSQL username |
| `POSTGRES_PASSWORD` | — | PostgreSQL password |
| `JWT_SECRET` | — | JWT signing key (at least 256 bits for production) |
| `JWT_EXPIRATION` | `3600000` | JWT expiration in milliseconds (default 1 hour) |
| `SERVER_PORT` | `8081` / `8082` | Service port |
| `STATION_SERVICE_URL` | `http://localhost:8081` | Station Service base URL (Session Service only) |

## Database

Single PostgreSQL instance with schema-based separation:

- `station` schema — managed by Station Service's Flyway migrations
- `session` schema — managed by Session Service's Flyway migrations

Flyway runs automatically on startup, creating and migrating tables from scratch.

### Station Schema

| Table | Description |
|---|---|
| `tariffs` | Pricing rules — `price_per_kwh`, `start_fee`, `currency` |
| `stations` | Physical charging locations |
| `connectors` | Individual EVSEs at a station — `type`, `power_kw`, `status` |

Seed data: 1 station ("ChargeSquare Downtown"), 2 tariffs, 2 connectors (CCS2-DC 60kW, Type2-AC 22kW).

### Session Schema

| Table | Description |
|---|---|
| `users` | Drivers — `username`, `password` (BCrypt), `role`, `wallet_balance` |
| `charging_sessions` | Core domain — lifecycle state, timestamps, cost, tariff snapshot |

## Security Implementation Details

- **Stateless Authentication**: No server-side sessions, all state is in the JWT
- **BCrypt Password Encoding**: All passwords are securely hashed
- **JWT Claims**: Contains `userId`, `username`, `role`, `iat` (issued at), and `exp` (expiration)
- **CSRF Disabled**: Not needed for stateless APIs
- **CORS Configured**: Ready for frontend integration
- **Role-based Access Control**: Endpoints can be secured with `@PreAuthorize` annotations

## Testing

Tests use **Testcontainers** to run against a real PostgreSQL instance — no mocks, no H2.

### Run all tests

```bash
# Station Service
cd station-service
./gradlew test

# Session Service
cd session-service
./gradlew test
```

### Test coverage

| Service | Test Class | Tests | What's covered |
|---|---|---|---|
| Station | `TariffRepositoryTest` | 3 | CRUD, seed data, decimal precision (NUMERIC) |
| Station | `StationRepositoryTest` | 2 | CRUD, seed data |
| Station | `ConnectorRepositoryTest` | 7 | CRUD, `findAllByStationId`, enum as STRING, decimal precision, lazy loading |
| Station | `StationServiceTest` | 3 | Business logic, mock repository, 404 handling |
| Station | `ConnectorServiceTest` | 4 | Business logic, mock repository, 404 handling |
| Station | `StationControllerTest` | 6 | `@WebMvcTest`, mock facade, valid response JSON, ProblemDetail mapping |
| Station | `ConnectorControllerTest` | 2 | `@WebMvcTest`, mock facade, valid response JSON, ProblemDetail mapping |
| Station | `StationServiceApplicationTests` | 1 | Spring context loads |
| Session | `UserRepositoryTest` | 8 | CRUD, `findByUsername`, `existsByUsername`, role enum, wallet precision, unique constraint |
| Session | `ChargingSessionRepositoryTest` | 9 | CRUD, `findAllByUserId`, status enum, decimal precision, timestamps, lazy loading |
| Session | `SessionServiceApplicationTests` | 1 | Spring context loads |
| Session | `JwtServiceTest` | 6 | Token generation, validation, claims extraction |
| **Total** | | **52** | |
