# ChargeSquare EV Charging — Case Study

A backend system for EV charging session management, built with **Java 21** and **Spring Boot 3.5**.

## Tech Stack & Why

| Layer | Technology | Reason |
|---|---|---|
| Language | Java 21 | Modern LTS release with records and virtual threads |
| Framework | Spring Boot 3.5.16 | Industry standard, robust ecosystem, rapid development |
| Security | Spring Security + JWT | Stateless, highly scalable authentication without sessions |
| Database | PostgreSQL 16 | ACID compliant, reliable relational data store |
| Migrations | Flyway | Predictable, version-controlled database schema evolution |
| API Docs | SpringDoc OpenAPI | Auto-generated interactive Swagger UI |
| Build | Gradle 8.14 | Fast, declarative, and flexible build tool |
| Containers | Docker / Compose | Consistent environments across dev, test, and prod |
| Testing | Testcontainers | High-fidelity testing against real database instances |

## Project Structure

```
chargesquare-ev-charge-case-study/
├── station-service/          # Stations, connectors, tariffs (own README.md)
├── session-service/          # Charging sessions, users, auth (own README.md)
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
| `admin1` | `admin123` | ADMIN | 500.00 |
| `admin2` | `admin123` | ADMIN | 1000.00 |
| `admin3` | `admin123` | ADMIN | 150.00 |
| `viewer1` | `viewer123` | VIEWER | 50.00 |
| `viewer2` | `viewer123` | VIEWER | 200.00 |

### Getting a Token

Send a POST request to Session Service's login endpoint:

```bash
curl -X POST http://localhost:8082/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin1", "password": "admin123"}'
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Using the Token & End-to-End Flow

Here is a 3-step end-to-end flow to authenticate, start a session, and stop it.

1. **Login** to get the JWT:
```bash
curl -X POST http://localhost:8082/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin1", "password": "admin123"}'
```
*(Copy the `accessToken` from the response)*

2. **Start a Charging Session**:
```bash
curl -X POST "http://localhost:8082/sessions/start?userId=4&connectorId=1" \
  -H "Authorization: Bearer <your_access_token>"
```
*(Note the `id` of the created session in the response)*

3. **Stop the Charging Session**:
```bash
# Assuming the session ID is 1, and 12.5 kWh was consumed
curl -X POST "http://localhost:8082/sessions/1/stop?energyKwh=12.5" \
  -H "Authorization: Bearer <your_access_token>"
```

### Swagger UI with JWT

Both services' Swagger UIs support JWT authentication:
1. Open `http://localhost:8081/swagger-ui.html` or `http://localhost:8082/swagger-ui.html`
2. Click the **Authorize** button
3. Enter your JWT token and click **Authorize**
4. All subsequent requests will include the token

## Prerequisites

- Docker & Docker Compose (for running the entire stack)
- Java 21 (for local development/testing)

## Getting Started

### 1. Set up environment variables

```bash
cp .env.example .env
# Edit .env with your values (defaults work perfectly for local dev)
```

### 2. Start the Entire Stack

The easiest way to run the database and both services is using Docker Compose:

```bash
docker-compose up --build -d
```

This will automatically:
- Start PostgreSQL (`chargesquare` database)
- Build and start the Station Service on `http://localhost:8081`
- Build and start the Session Service on `http://localhost:8082`

To view the logs:
```bash
docker-compose logs -f
```

*(Alternatively, you can start only `postgres` using Docker and run the Spring Boot services locally via `./gradlew bootRun` in their respective directories).*

## Available Endpoints

> **Note:** Detailed request and response bodies are omitted here for brevity. Both services expose full OpenAPI specifications via Swagger UI (`/swagger-ui.html`), where you can inspect schemas and test the endpoints directly.

### Station Service (`http://localhost:8081`)

| Method | Endpoint | Description | Auth Required | HTTP Status Codes |
|---|---|---|---|---|
| GET | `/actuator/health` | Health check (Actuator) | No | **200** (OK) |
| GET | `/swagger-ui.html` | Swagger UI | No | **200** (OK) |
| GET | `/v3/api-docs` | OpenAPI spec (JSON) | No | **200** (OK) |
| GET | `/stations` | List all stations | Yes | **200** (OK), **401** (Unauthorized) |
| GET | `/stations/{id}` | Get station by ID with connectors | Yes | **200** (OK), **401** (Unauthorized), **404** (Not Found) |
| GET | `/stations/{id}/connectors` | List connectors for a station | Yes | **200** (OK), **401** (Unauthorized), **404** (Not Found) |
| GET | `/connectors/{id}` | Get connector by ID with tariff | Yes | **200** (OK), **401** (Unauthorized), **404** (Not Found) |
| PATCH | `/connectors/{id}/occupy` | Mark connector as occupied (Admin only) | Yes | **200** (OK), **401** (Unauthorized), **403** (Forbidden), **404** (Not Found), **409** (Conflict) |
| PATCH | `/connectors/{id}/release` | Mark connector as available (Admin only) | Yes | **200** (OK), **401** (Unauthorized), **403** (Forbidden), **404** (Not Found), **409** (Conflict) |

### Session Service (`http://localhost:8082`)

| Method | Endpoint | Description | Auth Required | HTTP Status Codes |
|---|---|---|---|---|
| GET | `/actuator/health` | Health check (Actuator) | No | **200** (OK) |
| GET | `/swagger-ui.html` | Swagger UI | No | **200** (OK) |
| GET | `/v3/api-docs` | OpenAPI spec (JSON) | No | **200** (OK) |
| POST | `/auth/login` | Login and get JWT token | No | **200** (OK), **401** (Unauthorized for invalid creds) |
| POST | `/sessions/start` | Start new charging session (Admin only) | Yes | **200** (OK), **401** (Unauthorized), **403** (Forbidden), **404** (Not Found), **409** (Conflict) |
| POST | `/sessions/{id}/stop` | Stop charging session, calculate cost, debit wallet (Admin only) | Yes | **200** (OK), **401** (Unauthorized), **403** (Forbidden), **404** (Not Found), **409** (Conflict) |
| GET | `/sessions/{id}` | Get session by ID | Yes | **200** (OK), **401** (Unauthorized), **404** (Not Found) |
| GET | `/users/{userId}/sessions` | Get all sessions for a user | Yes | **200** (OK), **401** (Unauthorized), **404** (Not Found) |
| PUT | `/users/{userId}/wallet/top-up` | Top up user wallet (Admin only) | Yes | **200** (OK), **400** (Bad Request), **401** (Unauthorized), **403** (Forbidden), **404** (Not Found) |

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
| Station | `ConnectorServiceTest` | 7 | Business logic, mock repository, 404 handling, occupy/release connector |
| Session | `ChargingSessionRepositoryTest` | 10 | CRUD, status enum, timezone, decimal precision, lazy loading user |
| Session | `UserRepositoryTest` | 9 | CRUD, unique username, wallet balance precision, role enum |
| Session | `CostCalculatorTest` | 6 | BigDecimal math, zero start fee, zero energy, large numbers, half-up rounding |
| Session | `SessionServiceTest` | 6 | Business logic, non-active session check, zero balance rejection |
| Session | `WalletServiceTest` | 7 | Top up, debit, negative balance check, 404 handling |
| Session | `JwtServiceTest` | 6 | Token generation, validation, role and userId extraction |
| Session | `SessionFacadeTest` | 3 | Full orchestration, stop session calculation, debiting |
| Session | `SessionControllerSecurityTest` | 3 | Unauthorized scenarios, role-based access control, invalid tokens |
| Station | `StationControllerTest` | 6 | `@WebMvcTest`, mock facade, valid response JSON, ProblemDetail mapping |
| Station | `ConnectorControllerTest` | 2 | `@WebMvcTest`, mock facade, valid response JSON, ProblemDetail mapping |
| Station | `StationServiceApplicationTests` | 1 | Spring context loads |
| Session | `UserRepositoryTest` | 8 | CRUD, `findByUsername`, `existsByUsername`, role enum, wallet precision, unique constraint |
| Session | `ChargingSessionRepositoryTest` | 9 | CRUD, `findAllByUserId`, status enum, decimal precision, timestamps, lazy loading |
| Session | `SessionServiceApplicationTests` | 1 | Spring context loads |
| Session | `JwtServiceTest` | 6 | Token generation, validation, claims extraction |
| Session | `SessionServiceTest` | 5 | Session creation, completion, retrieval, user sessions, negative balance check |
| Session | `CostCalculatorTest` | 6 | Cost calculation with rounding, required worked example (12.5 kWh × 8.50/kWh + 2.00 = 108.25) |
| Session | `WalletServiceTest` | 7 | Debit, top-up, get balance, negative balance handling |
| Session | `SessionFacadeTest` | 3 | Stop session flow orchestration, session retrieval |
| Session | `SessionControllerSecurityTest` | 1 | Security configuration test |
| **Total** | | **68** | |

## Assumptions
- **Currency**: Assumed a single currency (`TRY`) for the scope of the case study, though the database stores currency codes.
- **Negative Balances**: Users are allowed to go into debt (negative wallet balance) after a session completes, but cannot start a new session if their balance is negative.
- **Authentication**: JWT tokens are sufficient for this system without a complex revocation strategy (e.g., redis blocklist) to keep it minimal.
- **Connectors**: A connector can only host one session at a time, and a user can only charge at one connector at a time.
- **Money Handling**: Money uses a decimal-safe type (`BigDecimal`) to avoid floating-point inaccuracies, with the final cost strictly rounded to 2 decimals using `RoundingMode.HALF_UP`.

## Time Spent & What I'd Do Next
- **Time Spent**: ~15-20 hours designing the schema, building the two microservices, writing tests, configuring security, and setting up Kubernetes manifests.
*(Note: The provided Kubernetes manifests in `k8s/` were successfully validated with `kubectl apply --dry-run=client`)*
- **What I'd Do Next**:
  - Implement a messaging queue (RabbitMQ/Kafka) for asynchronous communication between services instead of synchronous REST calls.
  - Introduce an API Gateway to route requests and handle authentication centrally.
  - Add integration with an external payment provider (e.g., Stripe) for wallet top-ups.
  - Add comprehensive metrics (Micrometer/Prometheus) and distributed tracing (Zipkin/Jaeger) for better observability.
