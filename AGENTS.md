<!--
Auto-generated AGENTS.md — guidance for AI coding agents working on this repo.
Keep this file concise and actionable. Update if project scaffolding changes.
-->
# AGENTS: How to be productive in this codebase

Purpose: short, hands-on guidance for AI assistants to make safe, correct edits and to run the project locally.

1) Big picture (What this code is & Task constraints)
- Application: **Fawry Payment Routing Engine**. A smart payment routing system that recommends the optimal payment gateway based on commissions, daily limits, processing time, and availability.
- Core Reference: For all functional requirements, constraints, and bonus scenarios, refer strictly to the **`PaymentRouting_AssociateSWE_Task.pdf`** located in the project root. This PDF is the source of truth for gateways data, scoring logic, split mechanisms, and expected API endpoints.
- Tech Stack: Java 17, Spring Boot 3.x, Spring Data JPA, Spring Security, Validation, PostgreSQL.
- Goal: Implement dynamic Gateway configurations, Smart Routing (with split handling), Transaction logging, and JWT-secured REST APIs.

2) Key files and locations (examples)
- `pom.xml` — dependency and build configuration. Note: spring-boot parent version 4.0.6 and Java 17.
- `src/main/java/com/example/fawrypaymentrouting/FawryPaymentRoutingApplication.java` — application entry point.
- `src/main/resources/application.yaml` — Spring configuration (currently only application.name).
- `compose.yaml` — Docker Compose fragment defining a `postgres` service used by developers (name is `compose.yaml`, not `docker-compose.yml`).
- `HELP.md` — project-specific notes (Docker Compose, Maven parent overrides).

3) Important project-specific patterns & gotchas
- Lombok is used (declared in `pom.xml`) and annotation processing is enabled via the maven-compiler-plugin. Generated code (getters/setters/etc.) may not appear in source — treat Lombok-annotated classes as providing those members.
- `spring-boot-maven-plugin` is configured to exclude Lombok from the repackaged image (see plugin `<excludes>`). Do not rely on Lombok being present at runtime inside the fat jar.
- The project includes `spring-boot-docker-compose` (dev-services) dependency — Spring Boot may auto-start services from `compose.yaml` during local runs if DevServices is enabled. The compose file exposes a `postgres` service with env vars in `compose.yaml`.
- Test dependencies are present as `*-test` starter artifacts (test slices). Expect Spring Boot test support and usual test annotations.

4) How to build, run, test, and debug (concrete commands)
- Build (recommended via wrapper):
  - `./mvnw -DskipTests package` — produce jar
- Run app locally (dev):
  - `./mvnw spring-boot:run` — runs the Spring Boot app
  - If you want Spring DevServices to bring up Postgres from `compose.yaml`, run the app normally; DevServices will use the provided compose file (dependency is already present in `pom.xml`).
- Run tests:
  - `./mvnw test`
- Build OCI image via spring-boot plugin:
  - `./mvnw spring-boot:build-image`
- Debug remotely (JVM remote debug port 5005):
  - `./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"`
- Run composed Postgres manually (if you prefer Docker CLI):
  - `docker compose -f compose.yaml up -d postgres`
  - Connection details from compose.yaml: POSTGRES_DB=mydatabase, POSTGRES_USER=myuser, POSTGRES_PASSWORD=secret

5) Integration points & external dependencies
- Postgres: compose.yaml provides a development Postgres image. The project currently has no `spring.datasource.*` config — code may use DevServices' auto-configuration or expect environment-specific configuration to be added.
- Spring Security: present as a dependency. If adding endpoints, check for expected security configuration (none is present yet).

6) Typical code layout (Domain-Driven Design)
- The project follows a modular, package-by-feature (Domain-Driven) structure.
- Add bounded contexts under `com.example.fawrypaymentrouting.<context_name>` (e.g., `gateway`, `payment`, `biller`).
- Inside each context, place parts into `api` (controllers), `dto`, `model` (entities), `repository`, and `service`.
- Cross-cutting concerns go into `com.example.fawrypaymentrouting.shared` (e.g., `shared.config`).
- Tests live under `src/test/java/...` mirroring the domain layout.

7) Minimal examples & snippets (to be used by agents)
- Run the app from source: `./mvnw spring-boot:run` (entry point `FawryPaymentRoutingApplication`).
- Start Postgres for local dev: `docker compose -f compose.yaml up -d postgres` then set `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mydatabase` and run the app.

8) What to look for when making changes
- Respect Lombok-generated members — do not remove Lombok unless you also add equivalent getters/setters/constructors.
- Keep the base package `com.example.fawrypaymentrouting` so Spring Boot's default component scan finds new components.
- When adding DB settings, prefer configuration in `src/main/resources/application.yaml` or environment variables (for CI/containers) rather than hardcoding credentials.

9) Where to update this doc
- If you scaffold controllers, repositories, or change compose filename or Spring Boot version, update `AGENTS.md` to reflect the new structure.

----
File created by automation. If something essential is missing (e.g. a new module or CI/CD pipeline), open an issue and add the missing integration notes here.

--- START AGENTS.md APPEND ---
## Core Architecture: Exception Handling & API Responses
This project strictly follows a **Base Exception Hierarchy** and uses a centralized **Global Exception Handler**.
1. **NO Entity-Specific Exceptions:** DO NOT create exceptions like `DuplicateGatewayException` or `GatewayNotFoundException`.
2. **NO Generic Throws:** DO NOT throw `RuntimeException`, `Exception`, or `ResponseStatusException` in the Service layer.
3. **Categorical Throws ONLY:** Use exceptions extending `BaseBusinessException` (e.g., `ResourceConflictException`, `ResourceNotFoundException`).
4. **NO Controller Try-Catch:** Controllers must be completely ignorant of exception handling.
5. **The Global Choke Point:** `GlobalExceptionHandler.java` catches `BaseBusinessException` to guarantee a consistent JSON format: `{ "timestamp": "...", "status": 400, "error": "...", "message": "..." }`
--- END AGENTS.md APPEND ---
