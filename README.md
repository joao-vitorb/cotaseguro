# CotaSeguro

Insurance quotation and policy issuance REST API.

CotaSeguro lets you register customers, generate insurance quotes from simple
business rules, turn an approved quote into a policy, and track policies. The
project is backend-first and documented with Swagger.

## Tech stack

- Java 21
- Spring Boot 3 (Spring Web)
- Spring Boot Actuator (health check)
- springdoc-openapi (Swagger UI)
- Maven (via Maven Wrapper)

## Requirements

- Java 21

Maven is not required: the project ships with the Maven Wrapper, which downloads
the right Maven version automatically.

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

## Build and test

```bash
./mvnw verify
```
