# Customer Support Hub (Spring Microservice)

Customer Support Hub is a Spring Boot REST API for managing customer profiles and support tickets with role-based access control.

## Tech Stack

- Java 25
- Spring Boot 4.0.5
- Spring Security + OAuth2 Resource Server (JWT)
- Spring Data JPA
- H2 (default local DB)
- MySQL 8 (profile-based / Docker)
- Gradle 9.x

## Main Capabilities

- OAuth-style token endpoint: `POST /oauth/token`
- Role model: `ADMIN`, `AGENT`, `CUSTOMER`
- Customer profile management
- Ticket creation and search
- Validation on incoming requests (Bean Validation)
- Consistent JSON error responses with appropriate HTTP status codes

## Prerequisites

- JDK 25 installed
- Docker Desktop (optional, for containerized run)
- MySQL 8 running separately (required for MySQL profile and current Docker Compose setup)
  - Can be a local MySQL installation, or
  - A separate Docker container running on the host machine

## Build

Windows PowerShell:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.2'
$env:Path="$env:JAVA_HOME\\bin;$env:Path"
.\gradlew.bat clean build
```

Linux/macOS:

```bash
./gradlew clean build
```

## Run Locally

### Default (H2 in-memory)

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.2'
$env:Path="$env:JAVA_HOME\\bin;$env:Path"
.\gradlew.bat bootRun
```

App starts on `http://localhost:8080`.

### MySQL profile

```powershell
$env:SPRING_PROFILES_ACTIVE='mysql'
$env:SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/customer_service_hub?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:SPRING_DATASOURCE_USERNAME='developer'
$env:SPRING_DATASOURCE_PASSWORD='java11'
.\gradlew.bat bootRun
```

## Run with Docker

Build image:

```powershell
docker build -t customer-service-hub .
```

Run container:

```powershell
docker run --rm -p 8080:8080 customer-service-hub
```

### Docker Compose

```powershell
docker compose up --build
```

Important: current `docker-compose.yml` does not start MySQL for you.

It expects a separate MySQL 8 instance already running on host machine at `host.docker.internal:3306` with:

- username: `developer`
- password: `java11`
- database: `customer_service_hub`

## Test

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.2'
$env:Path="$env:JAVA_HOME\\bin;$env:Path"
.\gradlew.bat test
```

## Authentication Quick Start

Request token:

```http
POST /oauth/token
Content-Type: application/json

{
  "username": "customer",
  "password": "java11"
}
```

Example default users:

- `admin` / `admin11` (ADMIN)
- `developer` / `developer11` (AGENT)
- `agent` / `agent11` (AGENT)
- `customer` / `java11` (CUSTOMER)

Use returned access token as:

```http
Authorization: Bearer <access_token>
```

## Health Endpoint

- `GET /alive` -> `200 OK`

## Error Handling

The API returns structured JSON errors with human-readable messages.

Common statuses:

- `200 OK` - success
- `400 Bad Request` - validation/malformed request
- `401 Unauthorized` - authentication required/failed
- `403 Forbidden` - authenticated but not allowed
- `404 Not Found` - resource not found
- `409 Conflict` - duplicate/conflicting data

Typical error payload:

```json
{
  "timestamp": "2026-03-28T20:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/oauth/token",
  "details": [
    "username: username is required"
  ]
}
```
