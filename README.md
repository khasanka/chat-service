### Chat Service  RAG chat-session storage microservice

Small Spring Boot (Java 21) service that stores chat sessions and messages used by a RAG/chat UI or LLM orchestration layer. Key features:

- Spring Boot 3 (Java 21), WAR packaging
- JPA (Postgres in prod, H2 for local dev)
- Flyway DB migrations
- API-key authentication (header: `X-API-KEY`)
- Static OpenAPI JSON + ReDoc for safe production docs
- Docker / docker-compose setup for prod-like runs

Quick links (when running locally)

- H2 console (dev profile): /h2-console
- ReDoc (static API docs): /docs/  (serves `openapi.json` at `/openapi.json`)
- Health: /actuator/health

Prerequisites

- JDK 21
- Maven (or use the bundled `mvnw`)
- Docker & Docker Compose (for containerized runs)

1) Build (local)

Open PowerShell in the repo root and build the artifact (skip tests if you want):

```powershell
cd "<your_folder_location>\chat-service"
.\mvnw -DskipTests package
```

2) Run locally (dev profile  H2 file DB, H2 console enabled)

The `dev` profile enables the H2 console and the dynamic OpenAPI UI (useful for development). Example using PowerShell environment variables:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:API_KEYS = 'dev-key'
.\mvnw spring-boot:run
```

Or run the packaged WAR:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:API_KEYS = 'dev-key'
java -jar target/chat-service.war
```

Notes:
- Dev JDBC URL (H2 file): `jdbc:h2:file:./data/chatdb` (configured in `application-dev.yml`).
- The dynamic Swagger UI is available in dev at `/swagger-ui` (only enabled in the `dev` profile).

3) Run in Docker (production-like)

This repo includes a `Dockerfile` and `docker-compose.yml` configured to run the app in `prod` profile with Postgres. The compose file sets `SPRING_PROFILES_ACTIVE=prod` and `API_KEYS=prod-key` by default (see `docker-compose.yml`).

Build the image and start the stack:

```powershell
# build (optional  docker compose will build when needed)

docker compose build

# start services in foreground

docker compose up

# or run detached

docker compose up -d
# view logs

docker compose logs -f app
```

What compose brings up (defaults in repo):

- `app`  the chat-service application (exposed on host `:8080`)
- `db`  Postgres (image: `postgres:14`), DB port 5432
- `adminer`  Adminer UI on host `:8081` for quick DB browsing (optional)

Environment variables (important ones)

- `API_KEYS`  comma-separated API keys that will be accepted in `X-API-KEY` header (required for `/api/**`)
- `DB_URL` / `DB_USER` / `DB_PASS`  Postgres JDBC URL and credentials
- `SPRING_PROFILES_ACTIVE`  `prod` (default in compose) or `dev`
- `CORS_ALLOWED_ORIGINS`  comma-separated allowed origins

See `.env.example` for a full list of supported environment variables.

APIs (summary)

- Create session: POST /api/v1/sessions  (body: `{ "userId": "...", "title": "..." }`)  returns 201 Created
- List sessions: GET /api/v1/sessions?userId=...&page=&size=
- Rename session: PATCH /api/v1/sessions/{id}/rename (body: `{ "title": "..." }`)
- Favorite (toggle): PUT /api/v1/sessions/{id}/favorite (body: `{ "favorite": true }`)
- Delete session: DELETE /api/v1/sessions/{id}
- Append message: POST /api/v1/sessions/{sessionId}/messages (body: `{ "sender": "USER|ASSISTANT|SYSTEM", "content": "...", "contextJson": "..." }`)  returns 201 Created
- Get message history: GET /api/v1/sessions/{sessionId}/messages?page=&size=

Important API notes

- The `sender` value is an enum and must be one of `USER`, `ASSISTANT`, or `SYSTEM` (uppercase).
- Malformed JSON or validation failures return HTTP 400 with a Problem JSON body.
- All `/api/**` endpoints require an `X-API-KEY` header matching one of the keys in `API_KEYS`.

OpenAPI / Postman

- Static OpenAPI JSON is expected at `src/main/resources/static/openapi.json` and is served at `/openapi.json`.
- ReDoc static UI is available at `/docs/` (it references `/openapi.json`).
- Postman collection and environment are included in `docs/postman/`  import and set `baseUrl` and `apiKey` variables before running.

Regenerating the OpenAPI spec (developer workflow)

Two approaches are documented in `docs/CONFIG.md`:

- Build-time plugin (recommended for CI): configure a maven plugin to run during prepare-package and emit the `openapi.json` into `target/classes/static` so it becomes part of the WAR.
- Runtime-fetch fallback: start the app in `dev` profile locally (or in CI), poll `/actuator/health`, then GET `/v3/api-docs` with a valid `X-API-KEY` and save the output to `src/main/resources/static/openapi.json`.

Testing

- Unit tests: `.\mvnw test`
- Integration suggestions: add Testcontainers-based integration tests that start Postgres and validate Flyway migrations and basic CRUD flows.

Security & production notes

- Ensure `API_KEYS` are kept secret and rotated. Use your deployment platform's secret store for `DB_PASS` and keys.
- In production the dynamic Swagger UI is disabled and only the static `openapi.json` + ReDoc is served.
- Configure TLS termination (reverse proxy / ingress) and ensure access to admin endpoints is restricted (Adminer should not be exposed in production).

Troubleshooting

- If `/openapi.json` returns a 403 in prod, the static file may be missing from the WAR  regenerate it and include in the build (see `docs/CONFIG.md`).
- If Flyway fails at startup, check DB connectivity env vars `DB_URL`, `DB_USER`, `DB_PASS` and Postgres logs.

Contact / contributions

Open issues or PRs with improvements (CI generation of OpenAPI, integration tests, better image builds) are welcome.
