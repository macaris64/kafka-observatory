# Kafka Observatory

Kafka Observatory is a tool for monitoring and analyzing Kafka clusters.

## Project Structure

- `backend/`: Kotlin + Spring Boot modular monolith.
- `frontend/`: Vite + React + TypeScript + MobX.

## Developer Experience (Makefile)

The project uses a root-level `Makefile` to orchestrate common tasks across frontend and backend.

### Building
- **Build Backend**: `make build-local-be`
- **Build Frontend**: `make build-local-fe`

### Testing & Coverage
We enforce a minimum of **70% coverage** for both frontend and backend.
- **Run all tests**: `make test-local`
- **Generate coverage**: `make test-local-coverage`
- **Verify coverage**: `make test-local-coverage-verify` (Fails if < 70%)

### Running Locally
To start the entire environment (Kafka + Backend + Frontend):
```bash
make run-local
```
- **Backend API**: `http://localhost:8080`
- **Frontend UI**: `http://localhost:5173`
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`

To stop and cleanup:
```bash
make down-local
```

## Documentation

- **Development Guide**: See `docs/` for architecture and models.
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
