# TaskFlow API

A task management REST API built with Spring Boot 3.5, Spring Security, JWT, Spring Data JPA, Flyway, and MySQL. The code is organized into web, service, domain, and persistence layers so the API can be maintained and extended as one deployable service.

## Features

- Stateless bearer-token authentication with signed HS256 JWTs.
- Role based access for `USER` and `ADMIN`; public registration always creates a `USER`.
- Task creation, assignment, status changes, comments, filtering, search, and pagination.
- Users can see tasks they created or were assigned. Admins can see every task and manage the user directory.
- Flyway-managed relational schema with foreign keys, indexes, and Hibernate schema validation.
- Request validation, consistent API error responses, optimistic locking, health endpoint, and graceful shutdown.
- Unit and Spring MVC integration tests. Integration tests run the migrations against H2 in MySQL compatibility mode.

## Requirements

- Java 17 or newer
- Docker Desktop or another Docker Compose implementation

The Maven Wrapper downloads the pinned Maven distribution on first use.

## Run locally

Start MySQL:

```powershell
docker compose up -d mysql
```

Set the JWT secret and start the API. Use a unique random value with at least 32 characters in real environments.

```powershell
$env:JWT_SECRET = "replace-this-with-a-unique-random-secret-of-32-characters-or-more"
.\mvnw.cmd spring-boot:run
```

The local database defaults are `taskflow` / `taskflow`. Override them with `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD`. The compose file uses local-development defaults; change them before exposing the database outside your machine.

The API starts at `http://localhost:8080`. Health is available at `GET /actuator/health`.

To create the first admin account, set `ADMIN_EMAIL` and `ADMIN_PASSWORD` before application startup. The password must contain at least 12 characters. Bootstrap creates the admin only if that email is not already registered. Public registration never accepts a role field.

## Run tests

```powershell
.\mvnw.cmd verify
```

The integration suite uses an isolated in-memory H2 database and checks the HTTP, security, service, and persistence layers together. Run the app against MySQL with Compose to exercise the production database engine locally.

## Authentication example

Register a regular account; the response contains a bearer token and safe user profile fields.

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "alex@example.com",
  "password": "change-this-password",
  "displayName": "Alex"
}
```

Send the returned `accessToken` on protected requests:

```http
Authorization: Bearer <accessToken>
```

Tokens expire after 60 minutes by default. Configure with `JWT_EXPIRATION_MINUTES`. A user can obtain a new token by logging in again.

## API endpoints

All endpoints except registration, login, and health require a bearer token.

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register a user and receive a token |
| POST | `/api/auth/login` | Public | Authenticate and receive a token |
| GET | `/api/users/me` | User | Read your profile |
| PATCH | `/api/users/me` | User | Update your display name |
| GET | `/api/users` | Admin | List user profiles |
| GET | `/api/users/{userId}` | Admin | Read a user profile |
| POST | `/api/tasks` | User | Create a task; optional initial assignee |
| GET | `/api/tasks` | User | List visible tasks with filters and pagination |
| GET | `/api/tasks/{taskId}` | User | Read a visible task |
| PUT | `/api/tasks/{taskId}` | Creator, assignee, admin | Replace editable task fields |
| PATCH | `/api/tasks/{taskId}/status` | Creator, assignee, admin | Change task status |
| PATCH | `/api/tasks/{taskId}/assignment` | Creator, admin | Assign or unassign a task |
| DELETE | `/api/tasks/{taskId}` | Creator, admin | Delete a task |
| GET | `/api/tasks/{taskId}/comments` | User with task access | List task comments |
| POST | `/api/tasks/{taskId}/comments` | User with task access | Add a comment |

`GET /api/tasks` supports `status`, `priority`, `assigneeId`, and case-insensitive `search`, plus standard `page`, `size`, and `sort` parameters. The maximum page size is 100. Supported statuses are `OPEN`, `IN_PROGRESS`, `COMPLETED`, and `ARCHIVED`; priorities are `LOW`, `MEDIUM`, `HIGH`, and `URGENT`.

See [the OpenAPI contract](docs/openapi.yaml) for request/response schemas and [the Postman collection](postman/TaskFlow.postman_collection.json) for runnable requests.

## Error format

Errors use JSON with an ISO-8601 timestamp, HTTP status, code, message, and optional field errors.

```json
{
  "timestamp": "2026-11-18T12:00:00Z",
  "status": 400,
  "error": "validation_failed",
  "message": "One or more fields are invalid",
  "fieldErrors": { "title": "must not be blank" }
}
```

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `JWT_SECRET` | Required | UTF-8 secret, at least 32 characters |
| `JWT_EXPIRATION_MINUTES` | `60` | Access-token lifetime |
| `DB_HOST` / `DB_PORT` | `localhost` / `3306` | MySQL host and port |
| `DB_NAME` | `taskflow` | Database name |
| `DB_USERNAME` / `DB_PASSWORD` | `taskflow` / `taskflow` | Application database credentials |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Unset | Optional first-admin bootstrap |
| `SERVER_PORT` | `8080` | HTTP port |

## Project layout

```text
src/main/java/com/taskflow/
  config/       security, JWT, pagination, bootstrap
  domain/       users, tasks, comments, enums
  dto/          validated API request and response records
  repository/   Spring Data repositories
  service/      business rules and access checks
  web/          REST controllers and exception mapping
src/main/resources/db/migration/  versioned Flyway migrations
src/test/       service unit tests and API integration tests
docs/           OpenAPI contract
postman/        Postman collection
```

## Architecture notes

This is a layered REST service with stateless authentication and clear module boundaries. It is deployable as one application. Task visibility and edit rules live in the service layer, and controllers exchange DTOs rather than exposing JPA entities. The schema separates users, tasks, and comments and uses foreign keys to preserve relationships.

## Suggested resume bullets

Describe only what you have personally verified and can explain in an interview. For example:

- Built a Spring Boot task management REST API with 15 documented endpoints for task creation, assignment, filtering, status updates, comments, and user profiles.
- Added stateless JWT authentication and role based authorization with Spring Security, plus Flyway migrations for a normalized MySQL schema.
- Added JUnit 5 service and API integration tests and a GitHub Actions Maven verification workflow.

## License

MIT. See [LICENSE](LICENSE).
