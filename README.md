# Fawry Payment Routing Engine

## Overview
The Fawry Payment Routing Engine is a comprehensive smart routing and transaction logging application built with **Spring Boot 3 (Java 17)** and **Angular 17**. It evaluates and optimally routes payments to various gateways based on complex criteria such as commissions, processing time limits, daily biller quotas, max/min configurations, and strict availability windows.

It includes a fully decoupled SPA dashboard featuring JWT authentication, allowing administrators to filter and view daily transaction aggregates natively.

## Project Structure
```text
fawry-payment-routing/
├── frontend/               # Angular 17 Standalone App (Tailwind CSS)
├── src/main/java/.../
│   ├── biller/             # Biller management bounded context
│   ├── gateway/            # Gateway configuration bounded context
│   ├── payment/            # Core routing algorithm and transaction logging
│   └── shared/             # Global exceptions, configurations, JWT Security
├── compose.yaml            # Local PostgreSQL Docker setup
└── pom.xml                 # Maven configuration
```

## How to Run

### Prerequisites
* **Java 17** setup in your environment.
* **Node.js** (v18+) and npm.
* **Docker** running locally (for the PostgreSQL database).

### 1. Start the Backend (Spring Boot)
The application leverages Spring Boot DevServices. If Docker is running, simply starting the app will automatically parse `compose.yaml` and spin up the PostgreSQL database in an ephemeral container.
```bash
./mvnw clean compile spring-boot:run
```
*The backend API runs on `http://localhost:8080`.*

### 2. Start the Frontend (Angular)
Open a new terminal window, step into the frontend sub-directory, install the node modules, and fire up the webpack server.
```bash
cd frontend
npm install
npm start
```
*The SPA UI serves live on `http://localhost:4200`.*

**Login Credentials:**
When the frontend fetches data and triggers an HTTP 401, a login modal will appear. Use the in-memory backend credentials:
* **Username:** `admin`
* **Password:** `admin123`

## Technical Assumptions, Architectural Choices, and Trade-offs (Technical Debt)

The following architectural choices and technical trade-offs were consciously made in order to prioritize MVP delivery while maintaining a robust foundation for future scalability.

### Security and Authentication Trade-offs
*   **In-Memory User Management**: The `fawry-admin` user is identically hardcoded into RAM utilizing Spring Security's `InMemoryUserDetailsManager` for MVP simplicity. In a production environment, this would be backed by a persistent database and managed via a dedicated Identity and Access Management (IAM) bounded context but not used here as the entities imposed in the task description didn't mention any users.
*   **Hardcoded JWT Secret**: The 256-bit signing key is kept in the `application.yaml` file for the ease of local evaluation. A production deployment must inject this secret dynamically via environment variables or a secure key management system such as AWS Secrets Manager or HashiCorp Vault.
*  **No Role-Based Access Control (RBAC)**: For the sake of MVP focus, all authenticated users have full access to the transaction logging and reporting features. A production system would implement fine-grained RBAC to restrict access based on user roles and permissions but the assumption was made that the system is for admins only so ALL users are admins.
### Database and Persistence
*   **Automatic Schema Generation**: The application leverages Hibernate's `ddl-auto: update` feature to instantly construct the database schema and constraints on startup. Production environments would disable this and utilize version-controlled database migrations via Flyway or Liquibase.
*   **Database-Level Aggregations**: To prevent OutOfMemory (OOM) errors and optimize footprint, daily transaction totals and gateway breakdowns are calculated directly in PostgreSQL using JPQL `GROUP BY` and `SUM` projections. This is heavily preferred rather than pulling massive datasets into the JVM heap to aggregate via Java Streams.

### Architecture and Project Layout
*   **Nested Monolith Structure**: The Angular application lives in a subfolder (`/frontend`) inside the Spring Boot root directory. This single-repository approach was chosen deliberately for reviewer convenience. In production, the frontend and backend would live in completely separate Git repositories with independent deployment pipelines.
*   **Domain Context Separation**: To prevent tight coupling, the transaction logging engine sits inside its own bounded context. It references billers strictly by a `billerId` UUID rather than using heavy JPA object mappings back to a separate Biller profile domain.

### Frontend Design Choices
*   **Client-Side Filter Scope**: The Gateway selection dropdown filters the current page of logs directly within the browser interface. While ideal for a responsive UI overview, deep production pagination would explicitly require passing this exact filter value back to the server as an optional request parameter.
*  **CRUD Functionality Omission**: The frontend focuses solely on displaying transaction logs and aggregates *as requested from the task description* . It does not include any interfaces for creating or managing billers, gateways, or routing rules. This is a conscious MVP scope decision to prioritize the core routing and logging features.