# Kubernetes Migration Design — Resume Builder

**Date**: 2026-07-14
**Status**: Draft

## 1. Context

Resume Builder is a Spring Boot 3.2 + React 18 + PostgreSQL 16 application, currently deployed via Docker Compose. This design covers migration to a local **minikube** Kubernetes cluster to enable dynamic scaling (HPA), load balancing (Ingress), and production-like deployment patterns.

### Current Deployment (Docker Compose)

| Service | Container | Port |
|---|---|---|
| PostgreSQL 16 | resume-postgres | 5432 |
| Backend (Spring Boot) | resume-backend | 8081 → 8080 |
| Frontend (Nginx SPA) | resume-frontend | 3000 → 80 |

### Goals

- Single-namespace K8s deployment with native YAML manifests
- Horizontal Pod Autoscaler (HPA) for backend and frontend
- Ingress-NGINX for TLS-termination-ready load balancing
- ConfigMap/Secret separation from images
- Liveness/Readiness probes for self-healing
- Simplified observability (Prometheus + Grafana)
- CI/CD: GitHub Actions validates manifests, local scripts deploy to minikube

### Non-Goals

- Cloud production deployment (EKS/GKE/AKS) — this is a local test environment
- GitOps (Flux/ArgoCD) — deferred
- NetworkPolicy / RBAC — omitted for local simplicity

## 2. Architecture

### Namespace

All resources live in a single namespace: `resume-builder`.

### Component Overview

| Layer | K8s Resources | Purpose |
|---|---|---|
| Config | ConfigMap `app-config`, Secret `app-secrets` | Non-sensitive + sensitive configuration |
| Data | PVC, StatefulSet `postgres`, ClusterIP `postgres-service` | Persistent PostgreSQL 16 |
| App Backend | Deployment `resume-backend` + Service `backend-service` + HPA | Spring Boot API (incl. Playwright PDF) |
| App Frontend | Deployment `resume-frontend` + Service `frontend-service` + HPA | Nginx serving React SPA |
| Ingress | Ingress `resume-ingress` (Ingress-NGINX controller) | TLS-ready load balancing |
| Observability | Prometheus + Grafana Deployments | Resource metrics dashboards |

### Directory Layout

```
k8s/
├── 00-namespace/         # namespace.yaml
├── 01-config/            # configmap.yaml, secret.yaml
├── 02-storage/           # pvc.yaml
├── 03-database/          # postgres-statefulset.yaml, postgres-service.yaml
├── 04-backend/           # backend-deployment.yaml, backend-service.yaml, backend-hpa.yaml
├── 05-frontend/          # frontend-deployment.yaml, frontend-service.yaml, frontend-hpa.yaml
├── 06-ingress/           # ingress.yaml
└── 07-observability/     # prometheus.yaml, grafana.yaml

scripts/
├── minikube-start.sh     # Start minikube + required addons
├── minikube-build-push.sh # Build and push images to minikube registry
├── k8s-apply.sh          # Apply all manifests in order
├── k8s-delete.sh         # Delete all resources
└── k8s-smoke-test.sh     # Basic connectivity tests
```

## 3. Resource Specifications

### 3.1 Config `01-config/`

**ConfigMap `app-config`** (non-sensitive)

| Key | Value |
|---|---|
| DB_HOST | postgres-service |
| DB_PORT | 5432 |
| DB_NAME | resume_db |
| APP_THEMES_PATH | classpath:themes/ |
| EXPORT_CHROME_PATH | /usr/bin/chromium |

**Secret `app-secrets`** (base64)

| Key | Value |
|---|---|
| DB_USER | resume |
| DB_PASS | resume123 |
| JWT_SECRET | <randomly-generated> |

> `application.yml` will be updated to read `JWT_SECRET` from environment variable with a dev-only fallback.

### 3.2 Storage `02-storage/`

**PersistentVolumeClaim `postgres-data-pvc`**
- StorageClass: `standard` (minikube default)
- AccessModes: `ReadWriteOnce`
- Resources: `5Gi`

### 3.3 Database `03-database/`

**StatefulSet `postgres`**
- Image: `postgres:16-alpine`
- Replicas: 1
- Env: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` from ConfigMap/Secret
- VolumeMount: `postgres-data-pvc` → `/var/lib/postgresql/data`
- Resources: requests `100m` CPU / `256Mi` mem; limits `500m` CPU / `512Mi` mem
- Liveness/Readiness: `pg_isready -U $(POSTGRES_USER) -d $(POSTGRES_DB)`

**Service `postgres-service`**
- Type: ClusterIP
- Port: `5432`
- TargetPort: `5432`

### 3.4 Backend `04-backend/`

**Deployment `resume-backend`**
- Image: `localhost:5000/resume-builder-backend:latest`
- Replicas: 2
- EnvFrom: ConfigMap `app-config` + Secret `app-secrets`
- Resources: requests `300m` CPU / `512Mi` mem; limits `1000m` CPU / `1.5Gi` mem
- Liveness: `GET /actuator/health/liveness`
- Readiness: `GET /actuator/health/readiness`
- ImagePullPolicy: `Always`

> Prerequisite: Spring Boot Actuator must be in `pom.xml`. If not, add `spring-boot-starter-actuator`.

**Service `backend-service`**
- Type: ClusterIP
- Port: `8080`
- TargetPort: `8080`

**HPA `backend-hpa`**
- Min: 2, Max: 5
- CPU: target 70%
- Memory: target 80%

### 3.5 Frontend `05-frontend/`

**Deployment `resume-frontend`**
- Image: `localhost:5000/resume-builder-frontend:latest`
- Replicas: 2
- Resources: requests `50m` CPU / `64Mi` mem; limits `200m` CPU / `128Mi` mem
- Liveness: `wget -qO- http://127.0.0.1:80`
- Readiness: `wget -qO- http://127.0.0.1:80`
- ImagePullPolicy: `Always`

> Nginx config change: remove `proxy_pass http://backend:8080` from `/api/` location. Frontend JS will call `/api/*` directly (spelled out in the Ingress). The healthcheck in Docker Compose is not needed in K8s.

**Service `frontend-service`**
- Type: ClusterIP
- Port: `80`
- TargetPort: `80`

**HPA `frontend-hpa`**
- Min: 2, Max: 5
- CPU: target 70%

### 3.6 Ingress `06-ingress/`

**Ingress `resume-ingress`**
- IngressClass: `nginx`
- Host: `resume.local`
- Rules:
  - `/api` → `backend-service:8080`
  - `/s` → `backend-service:8080` (public share links)
  - `/` → `frontend-service:80`
- Annotations: `nginx.ingress.kubernetes.io/ssl-redirect: "false"` (no TLS for local)
- TLS skipped for local test environment

> Ingress-NGINX controller is installed as a minikube addon (`minikube addons enable ingress`), not as a manifest in this repo. The Ingress resource only defines routing rules.

### 3.7 Observability `07-observability/`

**Prometheus**
- `prometheus/prometheus:latest`
- Config: scrape backend actuator `/actuator/prometheus` (if Micrometer + micrometer-registry-prometheus is added) and nginx-ingress-controller metrics
- Access: port-forward or NodePort

> Backend needs `micrometer-registry-prometheus` in `pom.xml` for Prometheus metrics. If not present, this is deferred to a future step — Prometheus can initially only scrape Ingress and node metrics.

**Grafana**
- `grafana/grafana:latest`
- Dashboards: Pod CPU/Memory, Ingress request rate/latency, HPA events
- Access: port-forward or NodePort
- Admin credentials: `admin/admin` (local only)

**metrics-server**
- Included in minikube by default (`minikube addons enable metrics-server` if not enabled)
- Required for HPA to function

## 4. Data Flow

### User Access Flow

```
User → http://resume.local/ → Ingress → frontend-service → frontend Pod
  → JS calls /api/resumes → Ingress → backend-service → backend Pod
    → postgres-service:5432 → PostgreSQL Pod
```

### PDF Export Flow

```
User clicks export → frontend POST /api/resumes/{id}/export/pdf
  → Ingress → backend Pod
    → Playwright (in-cluster Chromium) renders PDF
    → Returns PDF bytes
```

### Configuration Injection Flow

```
kubectl apply -f 01-config/
  → Secret/ConfigMap objects created
  → backend Pod reads via envFrom at startup
  → Spring Boot resolves ${VAR} placeholders
```

### HPA Scaling Flow

```
Pod CPU > 70% (60s sustained)
  → metrics-server reports metrics
  → HPA controller calculates desired replicas
  → Deployment scaled up
  → New Pod receives traffic via Service + Ingress
```

## 5. Error Handling & Resilience

### Health Probes

| Component | Liveness | Readiness | Behaviour |
|---|---|---|---|
| PostgreSQL | `pg_isready` | `pg_isready` | Restart on hang; don't route while starting |
| Backend | `/actuator/health/liveness` | `/actuator/health/readiness` | Restart on hang; don't route until DB connected |
| Frontend | HTTP 200 on `:80` | HTTP 200 on `:80` | Restart on failure; don't route until serving |

### Startup Ordering

- Kubernetes does not guarantee Pod order. Backend should handle DB start-up via:
  - Spring Boot + HikariCP retry (built-in, no extra config needed)
  - Optionally, an init container that polls `pg_isready` (deferred — retry is sufficient)

### Self-Healing

- Failed liveness probes → container restart
- Failed readiness probes → traffic removed from Service endpoints
- Node failure → Pods rescheduled on remaining nodes

### Data Safety

- PostgreSQL data written to PVC, survives Pod restart
- Local minikube PVC is on host disk; data should persist across cluster restarts (assumes `minikube delete` is not run)
- A `pg-dump` CronJob can be added in a future iteration

## 6. Testing Strategy

### Verification Checklist

| Scenario | Verification |
|---|---|
| All Pods Running | `kubectl get pods -n resume-builder` — all Ready |
| Frontend Accessible | `curl -s http://resume.local` returns HTML |
| API Working | `curl -s http://resume.local/api/themes` returns theme list |
| Register/Login | Smoke script creates user, obtains JWT, creates resume |
| PDF Export | Export a resume via API, verify non-empty response |
| Share Link | Create share link, access via `/s/{token}` |
| HPA Scaling | Run load test, observe Pod count increase |
| Pod Failure Recovery | Delete backend Pod, verify new Pod replaces it |

### Available Scripts

- `scripts/k8s-wait-ready.sh` — wait for all namespace Pods to be Ready
- `scripts/k8s-smoke-test.sh` — full smoke test suite
- `scripts/k8s-delete.sh` — tear down all resources

### CI/CD Preservation

- Existing CI jobs (backend-test, frontend-test, e2e) remain unmodified
- New CI step: `kubectl apply --dry-run=client -f k8s/` to validate YAML syntax on push/PR
- No CI K8s cluster — deployment is local only

## 7. Key Changes from Current Setup

| Aspect | Docker Compose | Kubernetes |
|---|---|---|
| **Database** | Named volume `postgres_data` | PVC `postgres-data-pvc` bound to StatefulSet |
| **Backend** | `ports: 8081:8080` at compose level | ClusterIP Service + HPA with resource requests/limits |
| **Frontend** | `ports: 3000:80` + healthcheck | ClusterIP Service + HPA + health probes |
| **Nginx proxy** | `proxy_pass http://backend:8080` inside nginx.conf | Removed — frontend JS calls `/api` via Ingress |
| **Config** | Hardcoded in docker-compose env | ConfigMap + Secret |
| **JWT secret** | Hardcoded `resume-builder-secret-for-dev` | Moved to Secret, read from env var |
| **PDF engine** | Playwright in backend container | Same — no change needed |
| **Access** | `localhost:3000` | `resume.local` via Ingress-NGINX |
| **Scaling** | Manual (docker compose scale) | HPA (automated, CPU+memory based) |
| **Rolling updates** | N/A (stop + start) | Deployment rolling update with PDB (optional) |
| **Health checking** | Docker healthcheck | Liveness + Readiness probes |

## 8. CI/CD

### GitHub Actions (`.github/workflows/ci.yml` addition)

```yaml
  validate-k8s-manifests:
    name: Validate K8s Manifests
    runs-on: ubuntu-latest
    needs: [backend-test, frontend-test]
    steps:
      - uses: actions/checkout@v4
      - name: Create target namespace for dry-run
        run: kubectl create namespace resume-builder --dry-run=client -o yaml | kubectl apply -f -
      - name: Validate K8s manifests
        run: |
          for f in $(find k8s -name '*.yaml' | sort); do
            kubectl apply --dry-run=client -f "$f" || exit 1
          done
```

> The namespace is pre-created so that dry-run can resolve `namespace: resume-builder` references in child resources.

### Local Deployment Scripts

**`scripts/minikube-start.sh`**
```bash
#!/bin/bash
minikube start --cpus=4 --memory=8g
minikube addons enable ingress
minikube addons enable metrics-server
minikube addons enable registry
```

**`scripts/k8s-apply.sh`**
```bash
#!/bin/bash
set -e

# Apply namespace first (cluster-scoped, no -n flag)
kubectl apply -f k8s/00-namespace/

# Apply remaining namespaced resources
for dir in k8s/01-config k8s/02-storage k8s/03-database k8s/04-backend k8s/05-frontend k8s/06-ingress k8s/07-observability; do
  kubectl apply -f "$dir" -n resume-builder
done

# Wait for rollouts
kubectl rollout status deployment/resume-backend -n resume-builder
kubectl rollout status deployment/resume-frontend -n resume-builder
```

## 9. Dependencies to Add

| File | Change | Reason |
|---|---|---|
| `backend/pom.xml` | Add `spring-boot-starter-actuator` | Liveness/Readiness probes |
| `backend/pom.xml` | Add `micrometer-registry-prometheus` (optional) | Prometheus metrics |
| `backend/src/main/resources/application.yml` | `jwt.secret: ${JWT_SECRET:fallback-dev-secret}` | Read JWT secret from env |
| `frontend/nginx.conf` | Remove `/api/` proxy_pass block | Let Ingress route /api/ |
| `docker-compose.yml` | Unchanged (still used for E2E CI) | Retain for CI compatibility |

## 10. Open Questions / Future Work

- [ ] Actuator + Micrometer: verify current `pom.xml` already includes them
- [ ] pg_dump CronJob: defer to post-migration
- [ ] TLS via cert-manager + Let's Encrypt: defer to cloud migration
- [ ] Cloud production (EKS/GKE/AKS): separate design
