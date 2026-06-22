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
- Spring Boot Actuator (health check)
- springdoc-openapi (Swagger UI)
- Maven (via Maven Wrapper)

## Requirements

- Java 21
- Docker (PostgreSQL runs in a container, used by the app and the tests)

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

## Running

Linux / macOS / Git Bash:

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The API starts on `http://localhost:8080`.

## Useful endpoints

- Swagger UI: `http://localhost:8080/swagger-ui`
- OpenAPI document: `http://localhost:8080/api-docs`
- Health check: `http://localhost:8080/actuator/health`

## Profiles

Configuration is split by Spring profile. The active profile defaults to `dev`
and can be overridden with the `SPRING_PROFILES_ACTIVE` environment variable.

- `dev`: verbose logging and detailed health output.
- `prod`: minimal logging and no health details.
- `test`: used by the test suite.

## Build and test

The tests run against a real PostgreSQL database, so start it first:

```bash
docker compose up -d
./mvnw verify
```
