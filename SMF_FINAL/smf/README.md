# Smooth Monitoring & Fortification (SMF)

A Spring Boot application providing secure monitoring and fortification services with JWT authentication, PostgreSQL persistence, and containerised deployment.

## Prerequisites

Ensure you have the following installed:

- **Docker & Docker Compose** (recommended for hassle-free setup)
- **Java Development Kit (JDK) 21** (required for local development only)
- **Apache Maven** (required for local development only)
- **PostgreSQL** (only if running the database locally without Docker)

## Getting Started

### Quick Start with Docker (Recommended)

The easiest way to get up and running:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Youssef-codin/smf.git
   cd smf
   ```

2. **Start the application:**
   ```bash
   docker compose up --build
   ```
   This command will build and start both the PostgreSQL database and Spring Boot application containers.

3. **Access the application:**
   - Application: [http://localhost:8080](http://localhost:8080)
   - API Documentation: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

4. **Stop the application:**
   ```bash
   docker compose down
   ```
   To remove all data volumes as well:
   ```bash
   docker compose down -v
   ```

### Local Development Setup

#### Option 1: Fully Local (Database + Server)

Requires PostgreSQL installed locally.

1. **Create and configure the database:**
   ```bash
   createdb smf
   ```

2. **Update `src/main/resources/application.properties`:**
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/smf
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
   **Note:** The application runs on port **8000** locally.
   - Access: [http://localhost:8000](http://localhost:8000)

#### Option 2: Hybrid (Dockerized Database + Local Server)

Best for local development and debugging with minimal setup.

1. **Start only the database container:**
   ```bash
   docker compose up db -d
   ```

2. **Update `src/main/resources/application.properties`:**
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5431/smf
   spring.datasource.username=smf
   spring.datasource.password=smf
   ```

3. **Run the application locally:**
   ```bash
   mvn spring-boot:run
   ```
   **Note:** The application runs on port **8000** locally.
   - Access: [http://localhost:8000](http://localhost:8000)

   View database logs with:
   ```bash
   docker compose logs -f db
   ```

## Project Structure

Core application logic is organised under `src/main/java/com/smf`:

- **`controller`** – REST endpoints handling HTTP requests
- **`dto`** – Data Transfer Objects organized by module (api, auth, device)
- **`model`** – JPA entities mapped to database tables
- **`repo`** – Spring Data JPA repository interfaces
- **`security`** – JWT utilities, authentication, and authorization
- **`service`** – Business logic and service layer orchestration
- **`util`** – Utility classes including `AppError` for global exception handling

## Database Migrations

Database schema migrations are managed with [Flyway](https://flywaydb.org/). Migration scripts are located in `src/main/resources/db/migration/`.

**Note:** Flyway is configured, however, we are not using it in development. The application is currently set to use Hibernate's `create-drop` feature for schema management during development.

## API Documentation
The API specification is available in OpenAPI 3.0 format:
- **Docker:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)
- **Local:** [http://localhost:8000/api-docs](http://localhost:8000/api-docs)

You can view the raw JSON schema directly in your browser or import it into tools like Postman, Insomnia, or any OpenAPI-compatible client for interactive API exploration and testing.

## Development Standards

1. **API Responses** – All responses must use the `ApiResponse` class
2. **API Prefix** – All controllers must be mapped under `/api/v1`
3. **Exception Handling** – All exceptions must be handled by `GlobalExceptionHandler` using `AppError`
4. **Branching** – Always work on a new feature branch, never commit directly to `main`
5. **Commit Messages** – Keep them clear and descriptive
   - Example: `feature: add device registration`
6. **Code Review** – Contact Youssef for review before merging to `main`
