# CotaSeguro

Insurance quotation and policy issuance REST API.

CotaSeguro lets you register customers, generate insurance quotes from simple
business rules, turn an approved quote into a policy, and track policies. The
project is backend-first and documented with Swagger.

## Tech stack

- Java 21
- Spring Boot 3 (Spring Web)
- Spring Data JPA + Hibernate
- PostgreSQL
- Flyway (database migrations)
- RabbitMQ (asynchronous policy issuance)
- Redis (query caching)
- Spring Boot Actuator (health check)
- springdoc-openapi (Swagger UI)
- Maven (via Maven Wrapper)

## Requirements

- Java 21
- Docker (PostgreSQL and RabbitMQ run in containers)

Maven is not required: the project ships with the Maven Wrapper, which downloads
the right Maven version automatically.

## Database

PostgreSQL runs through Docker Compose. Start it before running the API:

```bash
docker compose up -d
```

Flyway applies the schema migrations automatically when the API starts. In the
`dev` profile a demo data seed is also applied so there are sample customers to
work with. Stop the database with:

```bash
docker compose down
```

Default connection settings can be overridden with the `DB_URL`, `DB_USERNAME`
and `DB_PASSWORD` environment variables.

## Asynchronous policy issuance

Issuing a policy is asynchronous. `POST /api/v1/policies` validates the request
(the quote must exist, be approved and not already have a policy) and, when
valid, publishes a message to RabbitMQ and returns `202 Accepted`. A consumer
then creates the policy idempotently. Poll `GET /api/v1/policies` to see the
issued policy. RabbitMQ also runs through Docker Compose; its management UI is
available at `http://localhost:15673` (user/password `cotaseguro`).

## Caching

Customer lookups by id are cached in Redis (`GET /api/v1/customers/{id}`) and the
cached entry is evicted when the customer is updated or deleted. Redis runs
through Docker Compose. In the test profile an in-memory cache is used instead.

## Running

Linux / macOS / Git Bash:

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The API starts on `http://localhost:8088`.

## Useful endpoints

- Swagger UI: `http://localhost:8088/swagger-ui`
- OpenAPI document: `http://localhost:8088/api-docs`
- Health check: `http://localhost:8088/actuator/health`
- Application info: `http://localhost:8088/actuator/info`
- Metrics (Prometheus, authenticated): `http://localhost:8088/actuator/prometheus`
- Login: `POST http://localhost:8088/api/v1/auth/login`

## Authentication

The API is secured with JWT. Send credentials to `POST /api/v1/auth/login` to
receive a token, then send it on protected requests as
`Authorization: Bearer <token>`.

In the `dev` profile an admin user is seeded on startup if it does not exist
(username `admin`, password `admin123` by default). These values can be
overridden with the `app.admin.username` and `app.admin.password` properties.
The JWT secret and expiration come from the `JWT_SECRET` and `JWT_EXPIRATION_MS`
environment variables.

Creating new users is restricted to admins through
`POST /api/v1/auth/register`.

The login endpoint is rate limited per client IP to slow down brute-force
attempts (`429 Too Many Requests` once the limit is reached). The limit is
configurable through the `LOGIN_RATE_LIMIT_MAX_ATTEMPTS` and
`LOGIN_RATE_LIMIT_WINDOW_SECONDS` environment variables.

## Observability

- Every response carries an `X-Request-Id` header (generated or taken from the
  request), and that id is added to the logs through the MDC for correlation.
- Custom business metrics are exposed via Micrometer: `cotaseguro.quotes.generated`
  and `cotaseguro.policies.issued`.
- Health and info are public; metrics and Prometheus require authentication.
- In the `prod` profile logs are emitted as structured JSON (ECS format).

## Profiles

Configuration is split by Spring profile. The active profile defaults to `dev`
and can be overridden with the `SPRING_PROFILES_ACTIVE` environment variable.

- `dev`: verbose logging, detailed health output and demo data seed.
- `prod`: structured JSON logging and no health details.
- `test`: used by the test suite.

## Docker image and full stack

A multi-stage `Dockerfile` builds a small runtime image that runs as a non-root
user. Build it with:

```bash
docker build -t cotaseguro:latest .
```

The whole stack (API plus PostgreSQL, RabbitMQ and Redis) can be started with the
`app` Compose profile, which builds the image and wires the API to the other
services:

```bash
docker compose --profile app up -d --build
```

The API runs with the `prod` profile and is available at
`http://localhost:8088`. Without the profile, `docker compose up -d` starts only
the supporting services for local development.

## Build and test

The tests run against a real PostgreSQL database, so start it first:

```bash
docker compose up -d
./mvnw verify
```

Test coverage is measured with JaCoCo. Running `verify` produces an HTML report
at `target/site/jacoco/index.html` and fails the build if line coverage drops
below the configured minimum.
