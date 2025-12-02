# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**PLD-MARS** is a multi-tier Java Enterprise application demonstrating Service-Oriented Architecture (SOA). The project
consists of 5 Gradle modules implementing a 4-layer architecture where each layer is a separate service communicating
via HTTP/JSON.

**Key Architecture Pattern**: Each layer runs as an independent Tomcat instance on different ports, communicating
through REST APIs.

## Essential Commands

### Start Services (Must be in order)

Services must be started sequentially due to dependencies:

```bash
# 1. Start MySQL (required first)
docker compose up -d

# 2. Start OM-Account service (Port 8091)
cd om-account && ./gradlew appRun

# 3. Start OM-Address service (Port 8092) - in a new terminal
cd om-address && ./gradlew appRun

# 4. Start SMA service (Port 8081) - in a new terminal
cd sma && ./gradlew appRun

# 5. Start IHM frontend (Port 8080) - in a new terminal
cd ihm && ./gradlew appRun
```

### Build Commands

```bash
# Build entire multi-module project from root
./gradlew build

# Build specific module
cd <module-name> && ./gradlew build

# Clean build artifacts
./gradlew clean

# Build without tests
./gradlew build -x test
```

### Stop Services

```bash
# Stop Tomcat instances: Ctrl+C in each terminal
# Stop MySQL container
docker compose down
```

## Module Structure & Communication Flow

### The 5 Modules

1. **common/** - Shared library (no server)
    - JsonHttpClient: HTTP/JSON communication between services
    - JsonServletHelper: Servlet response utilities
    - DBConnection, MicroCasFilter, custom exceptions

2. **ihm/** - Interface Homme-Machine (Presentation Layer) - Port 8080
    - Entry point: IhmServlet
    - Delegates to SMA via HTTP

3. **sma/** - Service Métier Applicatif (Business Logic Layer) - Port 8081
    - Entry point: SmaServlet
    - Orchestrates calls to multiple OMs via HTTP

4. **om-account/** - Objet Métier Account (Data Layer) - Port 8091
    - Entry point: AccountServlet
    - JPA entities, DAO pattern, direct MySQL access
    - Account domain entity

5. **om-address/** - Objet Métier Address (Data Layer) - Port 8092
    - Entry point: AddressServlet
    - JPA entities, DAO pattern, direct MySQL access
    - Address domain entity with foreign key to Account

### Request Flow Pattern

```
Browser → IHM (8080) → SMA (8081) → OM Services (8091, 8092) → MySQL (3306)
          [HTTP/JSON]   [HTTP/JSON]      [JPA/JDBC]
```

**Critical Detail**: Each layer has:

- Servlet (Front Controller pattern)
- Action classes (Command pattern)
- Service classes (business logic/HTTP delegation)
- Vue classes (JSON response rendering)

### Compile-time vs Runtime Dependencies

- **Compile-time**: All upper layers depend on lower layer JARs for domain objects
- **Runtime**: Layers communicate via HTTP/JSON only (loose coupling)
- **URLs configured in**: `.env` file (SOM_ACCOUNT_URL, SOM_ADDRESS_URL, SMA_URL)

## Key Architectural Patterns

### Front Controller Pattern

Each module has a single servlet handling all requests:

- `IhmServlet` routes via `?action=<actionName>`
- `SmaServlet` routes via `?SMA=<smaName>`
- `AccountServlet` and `AddressServlet` route via `?SOM=<somName>`

### Command Pattern

Actions encapsulate request processing logic:

- Each action implements an `execute()` method
- Actions are instantiated based on query parameters
- Example: `ListAccountsAction`, `AddAccountAction`

### Model-View-Controller (MVC)

- **Model**: Domain objects (Account, Address)
- **View**: Vue classes serialize responses to JSON
- **Controller**: Servlet + Action classes

### Data Access Object (DAO)

- `AccountDAO` and `AddressDAO` abstract JPA operations
- Services use DAOs, not direct EntityManager

### Thread-Local EntityManager

- `JpaUtil` manages EntityManager lifecycle per thread
- Transaction boundaries in Service classes

## Database Configuration

### JPA Configuration

- **Persistence unit name**: `com.lukamaret.pld_mars_account` (shared across all OMs)
- **Provider**: EclipseLink JPA 5.0.0-B11
- **Driver**: MySQL Connector/J 9.5.0
- **Schema generation**: `drop-and-create` (development mode - recreates tables on each startup)

### Database Credentials

All database configuration is in `.env` file at root:

- `DATABASE_NAME`, `DATABASE_USER`, `DATABASE_PASSWORD`
- `DATABASE_URL=jdbc:mysql://localhost:3306/pld-mars`

JpaUtil reads these at runtime via `System.getProperty()` (loaded by Gretty from .env).

### MySQL Setup

- Runs in Docker via `docker compose up -d`
- Image: mysql:9
- Port: 3306
- Data persisted in `./lib/mysql` volume

## Adding New Features

### Adding a New Business Object Module (OM)

1. Create new Gradle module in `settings.gradle`
2. Copy structure from `om-account` or `om-address`
3. Define JPA entity in `domain/` package with `@Entity` annotation
4. Add entity class to `persistence.xml`
5. Create DAO in `infrastructure/` package
6. Create Service in `service/` package with transaction management
7. Create Actions in `model/` package
8. Create Servlet in `controller/` package
9. Create Vue classes in `vue/` package for JSON responses
10. Configure new port in `.env` (e.g., `SOM_NEWENTITY_PORT=8093`)
11. Update Gretty config in `build.gradle` to use the port

### Adding New Endpoint to Existing Module

1. Create new Action class implementing execute()
2. Add Action instantiation in Servlet's doGet/doPost based on parameter
3. Create corresponding Vue class if custom JSON format needed
4. For SMA: Add orchestration method in ServiceMetierApplicatif
5. For IHM: Add orchestration method in IhmService

### Important: URL Routing Conventions

- **IHM**: `GET/POST /api?action=<actionName>` (e.g., `?action=listAccounts`)
- **SMA**: `POST /api?SMA=<smaName>` (e.g., `?SMA=listAccounts`)
- **OM**: `POST /api?SOM=<somName>` (e.g., `?SOM=listAccounts`)

Action names should match across layers for consistency.

## Common Development Patterns

### Adding HTTP Communication Between Layers

Use `JsonHttpClient` from common module:

```java
JsonHttpClient client = new JsonHttpClient(BASE_URL);
Type resultType = new TypeToken<List<Account>>() {
}.getType();
List<Account> accounts = client.post(
        "?SOM=listAccounts",
        Collections.emptyMap(),
        resultType
);
```

### Transaction Management in OM Services

```java
public void createAccount(Account account) {
    JpaUtil.beginTransaction();
    try {
        accountDAO.create(account);
        JpaUtil.commitTransaction();
    } catch (Exception e) {
        JpaUtil.rollbackTransaction();
        throw e;
    }
}
```

### Reading Environment Variables

Environment variables from `.env` are loaded by Gretty as JVM system properties:

```java
String dbUrl = System.getProperty("DATABASE_URL");
```

## Testing

### Manual API Testing

```bash
# Test IHM layer (user-facing API)
curl "http://localhost:8080/api?action=listAccounts"

# Test SMA layer directly
curl -X POST "http://localhost:8081/sma/api?SMA=listAccounts"

# Test OM layer directly
curl -X POST "http://localhost:8091/om-account/api?SOM=listAccounts"
```

### Testing Web UI

Access the main interface at: `http://localhost:8080`

## Critical Architectural Constraints

1. **Layer Communication**: Upper layers MUST only communicate with immediate lower layer via HTTP
    - IHM → SMA only
    - SMA → OMs only
    - Never skip layers (e.g., IHM cannot call OM directly)

2. **Transaction Boundaries**: Only OM layer services should manage JPA transactions

3. **EntityManager Lifecycle**: Always use JpaUtil, never create EntityManager manually

4. **Startup Order**: Services have hard dependencies on lower layers being available

5. **Schema Recreation**: Current JPA config drops and recreates tables on each OM startup - all data is lost

6. **Port Configuration**: Each service has a fixed port defined in .env - conflicts will prevent startup

## Technology Stack

- **Java**: Jakarta EE 11
- **Build Tool**: Gradle 8.x (multi-project setup)
- **Servlet Container**: Tomcat 11 (via Gretty plugin 5.0.0)
- **Database**: MySQL 9.0
- **ORM**: EclipseLink JPA 5.0.0-B11
- **HTTP Client**: Apache HttpComponents Client5 5.5.1
- **JSON**: Gson 2.13.2

## Module Dependencies

```
ihm → depends on: common, sma, om-account, om-address (compile)
sma → depends on: common, om-account, om-address (compile)
om-account → depends on: common
om-address → depends on: common
common → no dependencies
```

Note: Despite compile-time dependencies, runtime communication is HTTP-only.