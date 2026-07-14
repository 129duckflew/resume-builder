# Kubernetes Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate Resume Builder from Docker Compose to local minikube Kubernetes with HPA, Ingress load balancing, ConfigMap/Secret separation, health probes, and basic observability.

**Architecture:** Single namespace (`resume-builder`) with layer-by-layer YAML manifests under `k8s/`. All infrastructure uses plain K8s resources without Helm/Kustomize. CI validates manifests via dry-run; local deployment via scripts.

**Tech Stack:** Kubernetes YAML, minikube, Ingress-NGINX, Spring Boot Actuator, Prometheus, Grafana

## Global Constraints

- All K8s manifests go under `k8s/`, organized by numeric directory prefix (00-07)
- All scripts go under `scripts/`
- JWT secret must be read from env var `JWT_SECRET`, with dev-only fallback
- Frontend nginx.conf must NOT proxy /api to backend (Ingress handles routing)
- Backend must have `spring-boot-starter-actuator` for liveness/readiness probes
- All YAML must pass `kubectl apply --dry-run=client`
- Images use tag `:latest` with `imagePullPolicy: Always` for local development

---

### Task 1: Add K8s readiness support (Actuator + env-based JWT)

**Files:**
- Modify: `backend/pom.xml` (add actuator dependency)
- Modify: `backend/src/main/resources/application.yml` (JWT secret from env)

**Interfaces:**
- Consumes: existing Spring Boot app
- Produces: backend with `/actuator/health/liveness` and `/actuator/health/readiness` endpoints; JWT secret overridable via env var

- [ ] **Step 1: Add actuator dependency to pom.xml**

Insert after the `spring-boot-starter-security` dependency (or any logical place):

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
```

- [ ] **Step 2: Change JWT secret to env-based in application.yml**

```yaml
    secret: ${JWT_SECRET:resume-builder-secret-for-dev-only-change-in-prod}
```

- [ ] **Step 3: Run backend tests to verify nothing broke**

Run: `cd backend && mvn test`
Expected: All tests pass (222 tests)

- [ ] **Step 4: Commit**

```bash
git add backend/pom.xml backend/src/main/resources/application.yml
git commit -m "feat: add actuator and env-based JWT secret for k8s probes"
```

---

### Task 2: Update frontend nginx.conf to remove backend proxy

**Files:**
- Modify: `frontend/nginx.conf`

**Interfaces:**
- Consumes: existing nginx.conf
- Produces: nginx serving SPA only; no backend proxy (Ingress handles /api/)

- [ ] **Step 1: Remove the `/api/` proxy_pass block**

Remove lines 12-19 from `frontend/nginx.conf`:

The `/api/` location block to delete:
```nginx
    # API proxy
    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 60s;
    }
```

Resulting nginx.conf:
```nginx
server {
    listen 80;
    server_name localhost;

    root /usr/share/nginx/html;
    index index.html;

    # Gzip
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml text/javascript;

    # SPA routing
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Cache static assets
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

- [ ] **Step 2: Build frontend to verify no breakage**

Run: `cd frontend && npm run build`
Expected: Build succeeds, dist/ directory is produced

- [ ] **Step 3: Commit**

```bash
git add frontend/nginx.conf
git commit -m "chore: remove backend proxy from nginx.conf for k8s ingress routing"
```

---

### Task 3: Create K8s namespace + config manifests

**Files:**
- Create: `k8s/00-namespace/namespace.yaml`
- Create: `k8s/01-config/configmap.yaml`
- Create: `k8s/01-config/secret.yaml`

**Interfaces:**
- Consumes: design doc specifications
- Produces: `resume-builder` namespace, ConfigMap `app-config`, Secret `app-secrets`

- [ ] **Step 1: Create `k8s/00-namespace/namespace.yaml`**

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: resume-builder
```

- [ ] **Step 2: Create `k8s/01-config/configmap.yaml`**

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: resume-builder
data:
  DB_HOST: postgres-service
  DB_PORT: "5432"
  DB_NAME: resume_db
  APP_THEMES_PATH: classpath:themes/
  EXPORT_CHROME_PATH: /usr/bin/chromium
```

- [ ] **Step 3: Create `k8s/01-config/secret.yaml`**

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
  namespace: resume-builder
type: Opaque
stringData:
  DB_USER: resume
  DB_PASS: resume123
  JWT_SECRET: resume-builder-k8s-secret-change-in-prod
```

- [ ] **Step 4: Validate all YAML syntax**

Run:
```bash
kubectl create namespace resume-builder --dry-run=client -o yaml | kubectl apply -f -
kubectl apply --dry-run=client -f k8s/00-namespace/namespace.yaml
kubectl apply --dry-run=client -f k8s/01-config/configmap.yaml
kubectl apply --dry-run=client -f k8s/01-config/secret.yaml
```
Expected: All return `created` or `unchanged` (dry-run)

- [ ] **Step 5: Commit**

```bash
git add k8s/00-namespace/ k8s/01-config/
git commit -m "feat(k8s): add namespace and config resources"
```

---

### Task 4: Create K8s storage + database manifests

**Files:**
- Create: `k8s/02-storage/pvc.yaml`
- Create: `k8s/03-database/postgres-statefulset.yaml`
- Create: `k8s/03-database/postgres-service.yaml`

**Interfaces:**
- Consumes: namespace `resume-builder`, ConfigMap `app-config`, Secret `app-secrets`
- Produces: PVC `postgres-data-pvc`, StatefulSet `postgres`, Service `postgres-service`

- [ ] **Step 1: Create `k8s/02-storage/pvc.yaml`**

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgres-data-pvc
  namespace: resume-builder
spec:
  storageClassName: standard
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 5Gi
```

- [ ] **Step 2: Create `k8s/03-database/postgres-service.yaml`**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: postgres-service
  namespace: resume-builder
spec:
  clusterIP: None
  selector:
    app: postgres
  ports:
    - port: 5432
      targetPort: 5432
```

- [ ] **Step 3: Create `k8s/03-database/postgres-statefulset.yaml`**

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgres
  namespace: resume-builder
spec:
  serviceName: postgres-service
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
        - name: postgres
          image: postgres:16-alpine
          env:
            - name: POSTGRES_DB
              valueFrom:
                configMapKeyRef:
                  name: app-config
                  key: DB_NAME
            - name: POSTGRES_USER
              valueFrom:
                secretKeyRef:
                  name: app-secrets
                  key: DB_USER
            - name: POSTGRES_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: app-secrets
                  key: DB_PASS
          ports:
            - containerPort: 5432
          resources:
            requests:
              cpu: 100m
              memory: 256Mi
            limits:
              cpu: 500m
              memory: 512Mi
          livenessProbe:
            exec:
              command:
                - sh
                - -c
                - pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"
            initialDelaySeconds: 15
            periodSeconds: 10
          readinessProbe:
            exec:
              command:
                - sh
                - -c
                - pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"
            initialDelaySeconds: 5
            periodSeconds: 5
          volumeMounts:
            - name: postgres-storage
              mountPath: /var/lib/postgresql/data
  volumeClaimTemplates:
    - metadata:
        name: postgres-storage
      spec:
        storageClassName: standard
        accessModes:
          - ReadWriteOnce
        resources:
          requests:
            storage: 5Gi
```

- [ ] **Step 4: Validate YAML**

Run:
```bash
kubectl apply --dry-run=client -f k8s/02-storage/pvc.yaml
kubectl apply --dry-run=client -f k8s/03-database/postgres-service.yaml
kubectl apply --dry-run=client -f k8s/03-database/postgres-statefulset.yaml
```
Expected: All return success

- [ ] **Step 5: Commit**

```bash
git add k8s/02-storage/ k8s/03-database/
git commit -m "feat(k8s): add PVC and PostgreSQL StatefulSet"
```

---

### Task 5: Create K8s backend manifests

**Files:**
- Create: `k8s/04-backend/backend-deployment.yaml`
- Create: `k8s/04-backend/backend-service.yaml`
- Create: `k8s/04-backend/backend-hpa.yaml`

**Interfaces:**
- Consumes: namespace `resume-builder`, ConfigMap `app-config`, Secret `app-secrets`, Service `postgres-service`
- Produces: Deployment `resume-backend`, Service `backend-service`, HPA `backend-hpa`

- [ ] **Step 1: Create `k8s/04-backend/backend-service.yaml`**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: backend-service
  namespace: resume-builder
  labels:
    app: resume-backend
spec:
  selector:
    app: resume-backend
  ports:
    - port: 8080
      targetPort: 8080
```

- [ ] **Step 2: Create `k8s/04-backend/backend-hpa.yaml`**

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: backend-hpa
  namespace: resume-builder
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: resume-backend
  minReplicas: 2
  maxReplicas: 5
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
```

- [ ] **Step 3: Create `k8s/04-backend/backend-deployment.yaml`**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: resume-backend
  namespace: resume-builder
  labels:
    app: resume-backend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: resume-backend
  template:
    metadata:
      labels:
        app: resume-backend
    spec:
      containers:
        - name: backend
          image: localhost:5000/resume-builder-backend:latest
          imagePullPolicy: Always
          envFrom:
            - configMapRef:
                name: app-config
            - secretRef:
                name: app-secrets
          ports:
            - containerPort: 8080
          resources:
            requests:
              cpu: 300m
              memory: 512Mi
            limits:
              cpu: 1000m
              memory: 1.5Gi
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 15
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 15
            periodSeconds: 5
```

- [ ] **Step 4: Validate YAML**

```bash
kubectl apply --dry-run=client -f k8s/04-backend/backend-service.yaml
kubectl apply --dry-run=client -f k8s/04-backend/backend-hpa.yaml
kubectl apply --dry-run=client -f k8s/04-backend/backend-deployment.yaml
```

- [ ] **Step 5: Commit**

```bash
git add k8s/04-backend/
git commit -m "feat(k8s): add backend deployment, service, and HPA"
```

---

### Task 6: Create K8s frontend + ingress manifests

**Files:**
- Create: `k8s/05-frontend/frontend-deployment.yaml`
- Create: `k8s/05-frontend/frontend-service.yaml`
- Create: `k8s/05-frontend/frontend-hpa.yaml`
- Create: `k8s/06-ingress/ingress.yaml`

**Interfaces:**
- Consumes: namespace `resume-builder`, Service `backend-service`, Service `frontend-service`
- Produces: Deployment `resume-frontend`, Service `frontend-service`, HPA `frontend-hpa`, Ingress `resume-ingress`

- [ ] **Step 1: Create `k8s/05-frontend/frontend-service.yaml`**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: frontend-service
  namespace: resume-builder
  labels:
    app: resume-frontend
spec:
  selector:
    app: resume-frontend
  ports:
    - port: 80
      targetPort: 80
```

- [ ] **Step 2: Create `k8s/05-frontend/frontend-hpa.yaml`**

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: frontend-hpa
  namespace: resume-builder
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: resume-frontend
  minReplicas: 2
  maxReplicas: 5
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

- [ ] **Step 3: Create `k8s/05-frontend/frontend-deployment.yaml`**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: resume-frontend
  namespace: resume-builder
  labels:
    app: resume-frontend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: resume-frontend
  template:
    metadata:
      labels:
        app: resume-frontend
    spec:
      containers:
        - name: frontend
          image: localhost:5000/resume-builder-frontend:latest
          imagePullPolicy: Always
          ports:
            - containerPort: 80
          resources:
            requests:
              cpu: 50m
              memory: 64Mi
            limits:
              cpu: 200m
              memory: 128Mi
          livenessProbe:
            exec:
              command:
                - wget
                - -qO-
                - http://127.0.0.1:80
            initialDelaySeconds: 10
            periodSeconds: 10
          readinessProbe:
            exec:
              command:
                - wget
                - -qO-
                - http://127.0.0.1:80
            initialDelaySeconds: 5
            periodSeconds: 5
```

- [ ] **Step 4: Create `k8s/06-ingress/ingress.yaml`**

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: resume-ingress
  namespace: resume-builder
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "false"
spec:
  ingressClassName: nginx
  rules:
    - host: resume.local
      http:
        paths:
          - path: /api
            pathType: Prefix
            backend:
              service:
                name: backend-service
                port:
                  number: 8080
          - path: /s
            pathType: Prefix
            backend:
              service:
                name: backend-service
                port:
                  number: 8080
          - path: /
            pathType: Prefix
            backend:
              service:
                name: frontend-service
                port:
                  number: 80
```

- [ ] **Step 5: Validate YAML**

```bash
kubectl apply --dry-run=client -f k8s/05-frontend/frontend-service.yaml
kubectl apply --dry-run=client -f k8s/05-frontend/frontend-hpa.yaml
kubectl apply --dry-run=client -f k8s/05-frontend/frontend-deployment.yaml
kubectl apply --dry-run=client -f k8s/06-ingress/ingress.yaml
```

- [ ] **Step 6: Commit**

```bash
git add k8s/05-frontend/ k8s/06-ingress/
git commit -m "feat(k8s): add frontend deployment, HPA, and ingress"
```

---

### Task 7: Create K8s observability manifests

**Files:**
- Create: `k8s/07-observability/prometheus.yaml`
- Create: `k8s/07-observability/grafana.yaml`

**Interfaces:**
- Consumes: namespace `resume-builder`
- Produces: Prometheus + Grafana deployments and services

- [ ] **Step 1: Create `k8s/07-observability/prometheus.yaml`**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: prometheus-service
  namespace: resume-builder
spec:
  selector:
    app: prometheus
  ports:
    - port: 9090
      targetPort: 9090
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: prometheus
  namespace: resume-builder
  labels:
    app: prometheus
spec:
  replicas: 1
  selector:
    matchLabels:
      app: prometheus
  template:
    metadata:
      labels:
        app: prometheus
    spec:
      containers:
        - name: prometheus
          image: prom/prometheus:latest
          ports:
            - containerPort: 9090
          resources:
            requests:
              cpu: 100m
              memory: 256Mi
            limits:
              cpu: 500m
              memory: 512Mi
```

- [ ] **Step 2: Create `k8s/07-observability/grafana.yaml`**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: grafana-service
  namespace: resume-builder
spec:
  selector:
    app: grafana
  ports:
    - port: 3000
      targetPort: 3000
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grafana
  namespace: resume-builder
  labels:
    app: grafana
spec:
  replicas: 1
  selector:
    matchLabels:
      app: grafana
  template:
    metadata:
      labels:
        app: grafana
    spec:
      containers:
        - name: grafana
          image: grafana/grafana:latest
          ports:
            - containerPort: 3000
          resources:
            requests:
              cpu: 100m
              memory: 128Mi
            limits:
              cpu: 500m
              memory: 256Mi
```

- [ ] **Step 3: Validate YAML**

```bash
kubectl apply --dry-run=client -f k8s/07-observability/prometheus.yaml
kubectl apply --dry-run=client -f k8s/07-observability/grafana.yaml
```

- [ ] **Step 4: Commit**

```bash
git add k8s/07-observability/
git commit -m "feat(k8s): add prometheus and grafana observability stack"
```

---

### Task 8: Create local deployment scripts

**Files:**
- Create: `scripts/minikube-start.sh`
- Create: `scripts/minikube-build-push.sh`
- Create: `scripts/k8s-apply.sh`
- Create: `scripts/k8s-delete.sh`
- Create: `scripts/k8s-smoke-test.sh`

**Interfaces:**
- Consumes: all k8s/ manifests
- Produces: executable shell scripts under scripts/

- [ ] **Step 1: Create `scripts/minikube-start.sh`**

```bash
#!/bin/bash
set -euo pipefail

echo "Starting minikube..."
minikube start --cpus=4 --memory=8g

echo "Enabling addons..."
minikube addons enable ingress
minikube addons enable metrics-server
minikube addons enable registry

echo "Adding resume.local to /etc/hosts..."
echo "$(minikube ip) resume.local" | sudo tee -a /etc/hosts

echo "minikube ready at http://resume.local"
```

- [ ] **Step 2: Create `scripts/minikube-build-push.sh`**

```bash
#!/bin/bash
set -euo pipefail

echo "Building backend image..."
eval $(minikube docker-env)
docker build -t resume-builder-backend:latest ./backend

echo "Building frontend image..."
docker build -t resume-builder-frontend:latest ./frontend

echo "Tagging and pushing to minikube registry..."
docker tag resume-builder-backend:latest localhost:5000/resume-builder-backend:latest
docker tag resume-builder-frontend:latest localhost:5000/resume-builder-frontend:latest
docker push localhost:5000/resume-builder-backend:latest
docker push localhost:5000/resume-builder-frontend:latest

echo "Images built and pushed."
```

- [ ] **Step 3: Create `scripts/k8s-apply.sh`**

```bash
#!/bin/bash
set -euo pipefail

echo "Creating namespace..."
kubectl apply -f k8s/00-namespace/

echo "Applying config..."
kubectl apply -f k8s/01-config/ -n resume-builder

echo "Applying storage..."
kubectl apply -f k8s/02-storage/ -n resume-builder

echo "Applying database..."
kubectl apply -f k8s/03-database/ -n resume-builder

echo "Applying backend..."
kubectl apply -f k8s/04-backend/ -n resume-builder

echo "Applying frontend..."
kubectl apply -f k8s/05-frontend/ -n resume-builder

echo "Applying ingress..."
kubectl apply -f k8s/06-ingress/ -n resume-builder

echo "Applying observability..."
kubectl apply -f k8s/07-observability/ -n resume-builder

echo "Waiting for rollouts..."
kubectl rollout status deployment/resume-backend -n resume-builder --timeout=120s
kubectl rollout status deployment/resume-frontend -n resume-builder --timeout=120s

echo "All resources deployed successfully!"
```

- [ ] **Step 4: Create `scripts/k8s-delete.sh`**

```bash
#!/bin/bash
set -euo pipefail

echo "Deleting all resources in resume-builder namespace..."
kubectl delete namespace resume-builder --ignore-not-found

echo "Cleanup complete."
```

- [ ] **Step 5: Create `scripts/k8s-smoke-test.sh`**

```bash
#!/bin/bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://resume.local}"

echo "Running smoke tests against $BASE_URL"

echo "1. Frontend responds..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL")
if [ "$HTTP_CODE" -eq 200 ]; then
  echo "   PASS: HTTP $HTTP_CODE"
else
  echo "   FAIL: HTTP $HTTP_CODE"
  exit 1
fi

echo "2. API responds (public themes endpoint)..."
API_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/themes")
if [ "$API_CODE" -eq 200 ]; then
  echo "   PASS: HTTP $API_CODE"
else
  echo "   FAIL: HTTP $API_CODE"
  exit 1
fi

echo "3. Pods are all Running..."
POD_STATUS=$(kubectl get pods -n resume-builder --no-headers 2>&1 | awk '{print $3}' | sort -u)
if [ "$POD_STATUS" = "Running" ]; then
  echo "   PASS: All pods Running"
else
  echo "   FAIL: Some pods not running"
  kubectl get pods -n resume-builder
  exit 1
fi

echo "All smoke tests passed!"
```

- [ ] **Step 6: Make scripts executable**

```bash
chmod +x scripts/*.sh
```

- [ ] **Step 7: Verify scripts exist and are runnable**

```bash
ls -la scripts/*.sh
```

- [ ] **Step 8: Commit**

```bash
git add scripts/
git commit -m "feat(scripts): add k8s deployment scripts for minikube"
```

---

### Task 9: Update CI pipeline and README

**Files:**
- Modify: `.github/workflows/ci.yml`
- Modify: `README.md`

**Interfaces:**
- Consumes: k8s/ manifests
- Produces: CI validates K8s manifests; README has k8s deployment instructions

- [ ] **Step 1: Add validate-k8s-manifests job to `.github/workflows/ci.yml`**

Add after the `e2e` job (before cleanup and `concurrency` is already defined at top):

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

- [ ] **Step 2: Add k8s deployment section to `README.md`**

Add after the existing "Docker Deployment" section (before Testing):

```markdown
## Kubernetes Deployment (minikube)

Prerequisites: [minikube](https://minikube.sigs.k8s.io/docs/start/) with Docker driver, 4+ CPUs, 8GB RAM.

```bash
# 1. Start minikube with required addons
./scripts/minikube-start.sh

# 2. Build and push images to minikube registry
./scripts/minikube-build-push.sh

# 3. Apply all K8s manifests
./scripts/k8s-apply.sh

# 4. Verify deployment
./scripts/k8s-smoke-test.sh

# Access: http://resume.local
```

Services:

| Resource | Type | Address |
|---|---|---|
| Frontend (SPA) | Ingress | `resume.local` → frontend-service:80 |
| API | Ingress | `resume.local/api/*` → backend-service:8080 |
| Shared links | Ingress | `resume.local/s/*` → backend-service:8080 |
| Database | StatefulSet | postgres-service:5432 |

Horizontal Pod Autoscaling:

| Deployment | Min | Max | Metric |
|---|---|---|---|
| `resume-backend` | 2 | 5 | CPU 70%, Memory 80% |
| `resume-frontend` | 2 | 5 | CPU 70% |

Tear down:

```bash
./scripts/k8s-delete.sh
```
```

- [ ] **Step 3: Verify CI YAML is valid**

```bash
# Basic check — verify file is parseable
python3 -c "import yaml; yaml.safe_load(open('.github/workflows/ci.yml'))" 
```

- [ ] **Step 4: Commit**

```bash
git add .github/workflows/ci.yml README.md
git commit -m "docs: add k8s deployment section and CI manifest validation"
```

---

## Self-review checklist

After writing all tasks, verify:
1. **Spec coverage:** Every section of the design doc maps to at least one task (namespace → T3, config → T3, storage/db → T4, backend → T1+T5, frontend → T2+T6, ingress → T6, observability → T7, scripts → T8, CI/README → T9, actuator/env-JWT → T1)
2. **Placeholder check:** No "TBD", "TODO", "implement later" in the plan
3. **No ambiguity:** Each command, file path, and code block is explicit
