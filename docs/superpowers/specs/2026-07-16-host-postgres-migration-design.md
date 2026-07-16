# Remove PG from k8s, migrate to host-installed PostgreSQL

## Motivation

Remove the PostgreSQL StatefulSet from the k3s cluster and the postgres service from docker-compose, replacing both with PostgreSQL 16 installed directly on the macOS host via Homebrew. The backend in k8s connects via `host.docker.internal`; local development connects via `localhost`.

## Architecture

```
Before:
  k8s: postgres StatefulSet (pod) → backend pod
  docker-compose: postgres service → backend container
  CI: docker-compose postgres service

After:
  Host (brew postgresql@16) → k8s backend pod (via host.docker.internal:5432)
  Host (brew postgresql@16) → local backend (via localhost:5432)
  CI: GitHub Actions postgres service container → backend container
```

## Changes

### 1. Delete k8s database manifests

- `k8s/03-database/` — delete entire directory (postgres-statefulset.yaml, postgres-service.yaml)

### 2. Update k8s ConfigMap

- `k8s/01-config/configmap.yaml` — change `DB_HOST` from `postgres-service` to `host.docker.internal`
- `k8s/01-config/secret.yaml` — unchanged (DB_USER/DB_PASS remain the same)

### 3. Update docker-compose.yml

- Remove `postgres` service block entirely
- Remove `postgres` from backend's `depends_on`
- Remove `postgres` from e2e's `depends_on`
- Change backend `DB_HOST: postgres` to `DB_HOST: host.docker.internal` (container needs to reach host brew PG)
- Remove `postgres_data` named volume

### 4. Update apply script

- `scripts/k8s-apply.sh` — remove `echo "Applying database..."` and `kubectl apply -f k8s/03-database/ -n resume-builder`

### 5. Update CI workflow

- `.github/workflows/ci.yml` — e2e job:
  - Add `services.postgres` block with image `postgres:16-alpine`, env `POSTGRES_DB/POSTGRES_USER/POSTGRES_PASSWORD`, ports `5432:5432`
  - Change `docker compose up --build -d postgres backend frontend` to:
    ```yaml
    - name: Build and start services (backend, frontend)
      run: docker compose up --build -d backend frontend
      env:
        DB_HOST: postgres
    ```
    The GitHub Actions service container is reachable via hostname `postgres`; passing `DB_HOST=postgres` as step env overrides the backend's DB_HOST to point at it.

### 6. Update documentation

- `README.md`:
  - Quick Start: replace "Docker (for PostgreSQL)" with "PostgreSQL 16 (via Homebrew)"
  - Quick Start: replace `docker compose up -d postgres` with brew install/service/createdb instructions
  - Docker Deployment table: remove `resume-postgres` row
  - K8s services table: remove `Database | StatefulSet | postgres-service:5432` row
- `progress.md`: update `docker compose up -d postgres` line

### 7. Host setup (one-time)

```bash
brew install postgresql@16
brew services start postgresql@16
createuser resume -P    # password: resume123
createdb resume_db -O resume
```

## Verification

1. `brew services restart postgresql@16 && pg_isready -U resume -d resume_db`
2. `kubectl delete -f k8s/03-database/ -n resume-builder` (cleanup running PG pod)
3. `./scripts/k8s-apply.sh` (re-apply config + backend)
4. Backend pod should start and connect to host PG
5. `docker compose up --build -d backend frontend` should work without postgres service
6. CI e2e tests should pass with service container PG
