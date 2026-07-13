# Station Service

This service manages charging stations, connectors, and tariffs.

## Prerequisites
- Java 21
- Docker & Docker Compose

## Running the Service Locally
1. Start the entire stack from the root directory using Docker Compose:
   ```bash
   docker-compose up --build -d
   ```
2. Alternatively, run only the database via docker and start the service locally:
   ```bash
   ./gradlew bootRun
   ```

## Endpoints
| Method | Endpoint | Description | Authentication Required |
|--------|----------|-------------|-------------------------|
| GET | /actuator/health | Health check | No |
| GET | /swagger-ui.html | Swagger UI | No |
| GET | /v3/api-docs | OpenAPI spec | No |
| GET | /stations | List all stations | Yes |
| GET | /stations/{id} | Get station by ID with connectors | Yes |
| GET | /stations/{id}/connectors | List connectors for a station | Yes |
| GET | /connectors/{id} | Get connector by ID with tariff | Yes |
| PATCH | /connectors/{id}/occupy | Mark connector as occupied (Admin only) | Yes |
| PATCH | /connectors/{id}/release | Mark connector as available (Admin only) | Yes |

## Configuration
All configuration via environment variables (see root README's .env.example file).

## Testing
Run tests with:
```bash
./gradlew test
```
