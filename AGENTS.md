# AGENTS.md

## Cursor Cloud specific instructions

### Overview

This is **QuarkusShop** — a monolithic e-commerce REST API built with Quarkus 3.29 (Java 21). It exposes REST endpoints for products, categories, customers, carts, orders, payments, and reviews.

### Prerequisites

- **Java 21** (OpenJDK) — already installed on the VM
- **PostgreSQL 16** — required; dev profile connects to `localhost:5432/demo` (user: `developer`, password: `p4SSW0rd`)

### Running the application

```bash
# Start PostgreSQL (if not already running)
sudo pg_ctlcluster 16 main start

# Run in dev mode (port 8080, live reload)
./mvnw quarkus:dev -Dquarkus.analytics.disabled=true
```

Dev UI: http://localhost:8080/q/dev-ui  
Swagger UI: http://localhost:8080/q/swagger-ui  
OpenAPI spec: http://localhost:8080/q/openapi  
Metrics: http://localhost:8080/q/metrics

### Building / Testing

```bash
# Compile only
./mvnw compile

# Full verify (JVM mode — native build requires Docker/GraalVM)
./mvnw verify -B -DskipITs -Dquarkus.native.enabled=false -Dquarkus.package.jar.enabled=true
```

**Important**: The `application.properties` sets `quarkus.native.enabled=true` globally. When running `verify` or `package` outside of `quarkus:dev`, you must override with `-Dquarkus.native.enabled=false` to avoid needing Docker/GraalVM for native image compilation. The `quarkus:dev` goal ignores these production packaging settings.

### Key gotchas

- No test source files exist yet (`src/test/java` is empty), so `mvnw verify` reports "No tests to run" — this is expected.
- The `quarkus.package.type=native` property in `application.properties` is deprecated; Quarkus warns about it but it does not affect dev mode.
- Flyway `migrate-at-start=true` is set but no SQL migration files exist; Hibernate's `database.generation=update` handles schema creation in dev mode.
- POST/PUT/DELETE endpoints for products, categories, and reviews require `admin` role (JWT). Customer endpoints require any authenticated user. GET endpoints for products and categories are public.
- In dev mode, Quarkus configures an in-memory JWT key pair for testing auth flows via `/q/dev-ui`.
