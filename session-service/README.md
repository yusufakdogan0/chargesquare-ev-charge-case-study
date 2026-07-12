# Session Service

This service manages users, charging sessions, and handles authentication/authorization.

## Prerequisites
- Java 21
- Docker & Docker Compose

## Running the Service Locally
1. Start PostgreSQL from the root directory:
   ```bash
   docker compose up postgres -d
   ```
2. Run the service:
   ```bash
   ./gradlew bootRun
   ```

## Default Users
| Username | Password | Role |
|----------|----------|------|
| admin    | admin123 | ADMIN |
| viewer   | viewer123 | VIEWER |

## Endpoints
| Method | Endpoint | Description | Authentication Required |
|--------|----------|-------------|-------------------------|
| GET | /actuator/health | Health check | No |
| GET | /swagger-ui.html | Swagger UI | No |
| GET | /v3/api-docs | OpenAPI spec | No |
| POST | /auth/login | Authenticate and obtain JWT | No |
| POST | /sessions/start | Start a new charging session (Admin only) | Yes |
| POST | /sessions/{id}/stop | Stop charging session, calculate cost, debit wallet (Admin only) | Yes |
| GET | /sessions/{id} | Get session by ID | Yes |
| GET | /users/{userId}/sessions | Get all sessions for a user | Yes |
| PUT | /users/{userId}/wallet/top-up | Top up user wallet (Admin only) | Yes |

## How to Authenticate
1. Send a POST to /auth/login with username and password to get accessToken
2. Use the accessToken in the Authorization header of subsequent requests:
   ```
   Authorization: Bearer <accessToken>
   ```

## Configuration
All configuration via environment variables (see root README's .env.example file).

## Testing
Run tests with:
```bash
./gradlew test
```
