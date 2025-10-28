# Configuration and Secrets (overview)

This document explains how this project manages configuration, where to put values, and secure ways to supply secrets for development, CI and production.

## Files and purpose

- `application.yml` — base defaults (checked into the repo). Use this for safe, non-sensitive defaults used across all environments.
- `application-dev.yml` — local / developer overrides (checked in). Can contain developer conveniences like H2 console, `ddl-auto: update`, etc.
- `application-prod.yml` — production overrides. Prefer to keep this minimal and reference environment variables for anything sensitive.
- `.env.example` — template of environment variables for developers. Copy to `.env` and fill with local values.

## Recommended secrets practice

- Do NOT commit real secrets in the repository (database passwords, real API keys, tokens).
- Use environment variables for production config. Example in `application-prod.yml` already uses `${DB_URL}`, `${DB_USER}`, `${DB_PASS}` — continue this pattern.
- For CI/CD, configure secrets in the pipeline (GitHub Actions Secrets, GitLab CI variables, Azure DevOps variable groups, etc.).


## Local development

1. Copy the template:

```powershell
cp .env.example .env
```

2. Edit `.env` and set local values (dev API keys, rate limits, CORS origins, etc.).
	 - Note: the dev H2 file path is now configured directly in `src/main/resources/application-dev.yml`.
		 If you need to change the H2 file location for local development, edit that file instead of relying
		 on an environment variable.
3. Run the app with the `dev` profile (PowerShell examples):

```powershell
# Set profile environment variable and run with Maven
$env:SPRING_PROFILES_ACTIVE = 'dev'; .\mvnw spring-boot:run

# Or run the packaged WAR (example)
$env:SPRING_PROFILES_ACTIVE = 'dev'; java -jar target/chat-service.war
```

Notes:
- The project `.gitignore` should include `.env` to avoid accidental commits.
- `application-dev.yml` intentionally enables H2 console and `ddl-auto: update` — do not use these in production.

## Docker (containers) — prod-only by default

- The repository is configured so Docker runs the application with the `prod` profile by default. Use the root `docker-compose.yml` when starting containers in development/CI: it targets Postgres and `SPRING_PROFILES_ACTIVE=prod`.
- Do NOT run the application in a container with the `dev` profile. H2 and other developer conveniences are only intended for local JVM runs and tests, not for containers.
- When running via Docker compose, the service uses Postgres (no H2). The Postgres credentials are read from environment variables — do not commit real credentials into the repo.
- If you need a local developer H2-backed container for experimentation, create a separate developer-only compose file (not checked into the production flow) and ensure it is not used by CI or production deploys.

## Production

- Inject `DB_URL`, `DB_USER`, `DB_PASS`, and any other secrets via environment variables from your deployment platform. Example container runtime:

```powershell
# Example (docker-compose or K8s workloads should reference secrets or env vars)
$env:DB_URL = 'jdbc:postgresql://db:5432/chat'
$env:DB_USER = 'chat'
$env:DB_PASS = '...'
```

- Keep `application-prod.yml` minimal; prefer environment overrides rather than committing full production config.

## Spring Boot property precedence (useful reminder)

Order of precedence (top wins):
1. Command line arguments
2. OS environment variables
3. `application-{profile}.yml` (profile-specific)
4. `application.yml` (classpath)
5. Default values in code

This means environment variables will override values in the checked-in YAML files — use that to keep secrets out of VCS.

## Tests and Flyway

- Unit tests use an in-memory H2 database and Flyway migrations. Integration tests that start the full Spring context will apply migrations. If you need separation, put slow/integration tests behind the `integration` profile and use Maven Failsafe to run them separately.

## Quick checklist

- [ ] Confirm `.env` is listed in `.gitignore` (prevent commits)
- [ ] Copy `.env.example` -> `.env` locally and fill dev secrets
- [ ] Use CI/CD secret stores for production credentials


## OpenAPI / API documentation (prod-safe delivery)

This project provides an OpenAPI JSON file packaged as a static asset so production deployments do not expose the interactive Swagger UI at runtime.

- Static artifact location (packaged in the WAR): `src/main/resources/static/openapi.json` — served at `/openapi.json`.
- Static ReDoc UI (optional) is available at `/docs/index.html` and points to `/openapi.json`.

Why static?
- Packaging the generated OpenAPI JSON into the WAR avoids exposing the dynamically generated `/v3/api-docs` endpoint in production.

How to regenerate `openapi.json`

There are two reasonable approaches. The build-time plugin method is ideal but can be brittle depending on plugin versions; a deterministic runtime-fetch fallback is reliable and simple to automate in CI.

1) Build-time (preferred if you can make the plugin work in your environment)

- Add a maven plugin to generate the OpenAPI JSON during `prepare-package` and output it to `src/main/resources/static/openapi.json` or `target/classes/static/openapi.json` so it is packaged into the WAR.
- Example (conceptual): add `org.springdoc:springdoc-openapi-maven-plugin` at a version that resolves in your environment and configure it to write the artifact to `src/main/resources/static`.

Note: earlier attempts to add this plugin in this repo failed due to a plugin-coordinate/version resolution issue; if you want, I can continue to research a working plugin coordinate and integrate it into the Maven lifecycle.

2) Runtime-fetch fallback (what we implemented successfully)

- Start the application locally with the `dev` profile on a known port (the dev profile allows access to `/v3/api-docs` behind the API key).
- Poll `/actuator/health` until UP. Then GET `/v3/api-docs` with a valid `X-API-KEY` and save the JSON to `src/main/resources/static/openapi.json`.

Local PowerShell example (used successfully during development):

```powershell
$proc = Start-Process -FilePath .\mvnw -ArgumentList 'spring-boot:run','-Dspring-boot.run.profiles=dev','-Dspring-boot.run.arguments="--server.port=8085"' -PassThru
Write-Host "STARTED PID $($proc.Id)"
$ready=$false
for ($i=0; $i -lt 90; $i++) {
	try {
		$r = Invoke-RestMethod -Uri http://localhost:8085/actuator/health -UseBasicParsing -TimeoutSec 3
		if ($r.status -eq 'UP') { Write-Host 'HEALTHY'; $ready=$true; break }
	} catch { Start-Sleep -Seconds 2; Write-Host "waiting... $i" }
}
if ($ready) {
	Write-Host 'Fetching /v3/api-docs'
	& curl.exe -s -H "X-API-KEY: dev-key" -H "Accept: application/json" http://localhost:8085/v3/api-docs -o src\main\resources\static\openapi.json
	if (Test-Path src\main\resources\static\openapi.json) { Write-Host 'OPENAPI SAVED' }
	else { Write-Host 'FAILED TO SAVE OPENAPI' }
} else { Write-Host 'APP DID NOT BECOME READY' }
Try { Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue; Write-Host 'STOPPED PROCESS' } Catch { Write-Host 'FAILED TO STOP PROCESS' }
```

CI-friendly sketch (GitHub Actions): start the app, wait for health, fetch `/v3/api-docs`, then persist the file as a build artifact or copy into the build output prior to packaging.

Example (conceptual steps):
- Run `mvn -DskipTests spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments="-Dserver.port=8085"` in background
- Poll `http://localhost:8085/actuator/health` until UP
- Curl `http://localhost:8085/v3/api-docs` with header `X-API-KEY: <ci-temporary-key>` and save as `target/classes/static/openapi.json`
- Continue the build/package step so the static file is included in the WAR

Security
- Ensure the API key used to fetch the docs in CI is a temporary or CI-specific key with limited scope and stored in the CI secret store. Do NOT hard-code production API keys in CI logs or artifacts.

Packaging/deployment
- Once `src/main/resources/static/openapi.json` (or `target/classes/static/openapi.json`) exists, it will be packaged into the WAR and served statically by the application at `/openapi.json`.
- The static ReDoc page at `/docs/index.html` will work with the packaged JSON file without exposing `/v3/api-docs` to public access.

If you'd like, I can:
- Commit the generated `src/main/resources/static/openapi.json` to the repo so your current WAR contains the spec. (Tell me if you want that; I can create a branch/PR.)
- Add a GitHub Actions workflow that runs the runtime-fetch approach and saves the generated JSON into the build output so CI packages it automatically.

## API contract details (quick reference)

These notes capture a few small but important request/response shapes that are easy to miss when calling the APIs from shells or scripts.

- Message sender values: when creating a message the `sender` field is an enum and must be one of: `USER`, `ASSISTANT`, `SYSTEM` (uppercase). Using other values will trigger validation errors and a 400 response.

- Favorite endpoint shape: to mark/unmark a session as favorite call `PUT /api/v1/sessions/{id}/favorite` with a JSON body:

	```json
	{ "favorite": true }
	```

	Sending an empty request body may be rejected as malformed JSON by some clients; include an explicit JSON object.

- Malformed JSON -> 400: if the request body is not valid JSON or fails validation (missing required fields, invalid enum values, size too large), the API returns HTTP 400 with an RFC 7807 Problem JSON body describing the error (for example: {"title":"Malformed JSON","detail":"Invalid JSON payload."}).

- Authentication: all `/api/**` endpoints require an `X-API-KEY` header containing a valid API key. Missing or invalid keys return 401/403 depending on the request flow.

Add these to your API client tests or Postman collections to avoid common pitfalls.
