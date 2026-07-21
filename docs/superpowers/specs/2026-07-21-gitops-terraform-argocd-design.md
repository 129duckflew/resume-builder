# GitOps: Terraform + ArgoCD + Vault on Local k3s

**日期:** 2026-07-21
**状态:** 已批准
**目标:** 将 resume-builder 的部署从手动 `kubectl apply` 升级为 GitOps 级别的自动化运维

---

## 架构总览

```
┌─────────────────────────────────────────────────────────────┐
│                        GitHub                               │
│  push main ──→ GitHub Actions CI                            │
│                  ├── backend-test (mvn test)                │
│                  ├── frontend-test (npm test + build)       │
│                  ├── e2e (Docker Compose + Playwright)      │
│                  ├── build+push images → ghcr.io           │
│                  └── update image tags in k8s/app/          │
│                                                             │
│  k8s/app/ changed ──→ ArgoCD detects ──→ sync to k3s       │
└─────────────────────────────────────────────────────────────┘

┌─────────── k3s (Colima) ────────────────────────────────────┐
│                                                              │
│  Terraform (run once, infra provisioning):                  │
│    ├── Helm: KEDA + HTTP Add-on                             │
│    ├── Helm: kube-prometheus-stack (Prometheus + Grafana)   │
│    ├── Helm: ArgoCD                                         │
│    └── Helm: Vault (dev mode) + K8s auth                   │
│                                                              │
│  ArgoCD (continuous, watches Git):                           │
│    ├── Application: resume-backend                          │
│    ├── Application: resume-frontend                         │
│    ├── Application: resume-infra (config + ingress)         │
│    └── Application: resume-scaling (KEDA)                   │
│                                                              │
│  Vault (secrets management):                                │
│    ├── KV v2: secret/resume-builder                         │
│    │    └── DB_USER, DB_PASS, JWT_SECRET, MCP_API_KEY       │
│    └── K8s auth → Agent Injector sidecar                    │
│                                                              │
│  End-to-end flow:                                           │
│    push main → CI test+build → ghcr.io → update manifests   │
│    → ArgoCD sync → Vault inject secrets → pods running      │
└──────────────────────────────────────────────────────────────┘
```

---

## Section 1: Terraform 基础设施层

**职责:** 一次性运行 provision k3s 上的基础设施组件。后续基础设施变更通过 `terraform apply` 管理。

**目录结构:**
```
infra/terraform/
├── main.tf              # provider 配置 (kubernetes + helm)
├── variables.tf         # 输入变量
├── outputs.tf           # 输出值
├── keda.tf              # KEDA + HTTP Add-on (Helm release)
├── monitoring.tf        # kube-prometheus-stack (Helm release)
├── argocd.tf            # ArgoCD (Helm release)
├── vault.tf             # Vault (Helm release)
├── vault-auth.tf        # Vault K8s auth method
└── terraform.tfvars.example
```

**Provider 配置:**
```hcl
provider "kubernetes" {
  config_path    = "~/.kube/config"
  config_context = "colima"
}

provider "helm" {
  kubernetes {
    config_path    = "~/.kube/config"
    config_context = "colima"
  }
}
```

**Helm Releases:**

| 组件 | Chart | Namespace | 关键配置 |
|---|---|---|---|
| KEDA | `kedacore/keda` | `keda` | 标准配置 |
| KEDA HTTP Add-on | `kedacore/keda-add-ons-http` | `keda` | 标准配置 |
| kube-prometheus-stack | `prometheus-community/kube-prometheus-stack` | `monitoring` | Grafana NodePort, Prometheus retention 15d |
| ArgoCD | `argo/argo-cd` | `argocd` | server service NodePort, CLI login |
| Vault | `hashicorp/vault` | `vault` | dev mode, injector enabled |

**设计决策:**
- Terraform 只管基础设施层，不管应用资源
- 使用本地 `~/.kube/config` 连接 k3s，state 文件放本地
- `terraform.tfvars` 不提交到 Git，提供 `.tfvars.example` 作为模板
- 现有 `k8s/07-observability/` 和 `k8s/08-scaling/` 由 Terraform Helm 管理

**与现有脚本的关系:**
- `scripts/k8s-install-keda.sh` → 被 `terraform apply` 替代
- `scripts/k8s-start.sh` → 保留（启动 Colima k3s）
- `scripts/k8s-delete.sh` → 部分保留（删除应用），基础设施用 `terraform destroy`

---

## Section 2: ArgoCD GitOps CD

**职责:** 持续监听 Git 仓库中的 K8s 应用 manifests，自动同步到 k3s 集群。

**目录结构:**
```
infra/argocd/
├── app-of-apps.yaml      # App of Apps 入口
└── applications/
    ├── resume-backend.yaml
    ├── resume-frontend.yaml
    ├── resume-infra.yaml
    └── resume-scaling.yaml

k8s/app/                   # ArgoCD 监听的 manifests
├── backend/
│   ├── deployment.yaml
│   └── service.yaml
├── frontend/
│   ├── deployment.yaml
│   └── service.yaml
├── config/
│   ├── configmap.yaml
│   └── secret.yaml        # 只存 key 名占位，值由 Vault 注入
├── ingress/
│   └── ingress.yaml
└── scaling/
    ├── backend-scaler.yaml
    └── frontend-scaler.yaml
```

**ArgoCD Application 示例:**
```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: resume-backend
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/<user>/resume-builder.git
    targetRevision: main
    path: k8s/app/backend
  destination:
    server: https://kubernetes.default.svc
    namespace: resume-builder
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
      - CreateNamespace=true
```

**同步策略:**

| Application | 监听路径 | 同步行为 |
|---|---|---|
| `resume-backend` | `k8s/app/backend/` | 自动同步 + self-heal |
| `resume-frontend` | `k8s/app/frontend/` | 自动同步 + self-heal |
| `resume-infra` | `k8s/app/config/` + `k8s/app/ingress/` | 自动同步 + self-heal |
| `resume-scaling` | `k8s/app/scaling/` | 自动同步 + self-heal |

**设计决策:**
- **selfHeal: true** — 手动 kubectl edit 会自动恢复为 Git 状态
- **prune: true** — Git 中删除的资源自动从集群清理
- namespace 由 `CreateNamespace=true` 自动创建
- ConfigMap 由 ArgoCD 同步，Secret 只存 key 名，值由 Vault 注入

---

## Section 3: GitHub Actions CI 增强

**在现有 CI 基础上增加镜像构建和推送能力。**

**新增 Job: `build-and-push`:**
```yaml
build-and-push:
  name: Build & Push Images
  runs-on: ubuntu-latest
  needs: [e2e]
  if: github.event_name == 'push' && github.ref == 'refs/heads/main'
  permissions:
    contents: write
    packages: write
  steps:
    - uses: actions/checkout@v4

    - name: Login to GHCR
      uses: docker/login-action@v3
      with:
        registry: ghcr.io
        username: ${{ github.actor }}
        password: ${{ secrets.GITHUB_TOKEN }}

    - name: Build & push backend
      uses: docker/build-push-action@v6
      with:
        context: ./backend
        push: true
        tags: |
          ghcr.io/${{ github.repository }}/backend:${{ github.sha }}
          ghcr.io/${{ github.repository }}/backend:latest

    - name: Build & push frontend
      uses: docker/build-push-action@v6
      with:
        context: ./frontend
        push: true
        tags: |
          ghcr.io/${{ github.repository }}/frontend:${{ github.sha }}
          ghcr.io/${{ github.repository }}/frontend:latest

    - name: Update image tags in manifests
      run: |
        sed -i "s|image: .*backend:.*|image: ghcr.io/${{ github.repository }}/backend:${{ github.sha }}|" \
          k8s/app/backend/deployment.yaml
        sed -i "s|image: .*frontend:.*|image: ghcr.io/${{ github.repository }}/frontend:${{ github.sha }}|" \
          k8s/app/frontend/deployment.yaml

    - name: Commit & push updated manifests
      uses: stefanzweifel/git-auto-commit-action@v5
      with:
        commit_message: "ci: update image tags to ${{ github.sha }}"
        file_pattern: k8s/app/
```

**PR vs Push 行为:**
| 事件 | test | e2e | build-push | 更新 manifests |
|---|---|---|---|---|
| PR to main | ✅ | ✅ | ❌ | ❌ |
| push main | ✅ | ✅ | ✅ | ✅ |

---

## Section 4: Vault 密钥管理

**职责:** 所有应用密钥的单一真实来源，通过 Agent Injector sidecar 注入到 Pod。

**Vault 部署 (Terraform):**
```hcl
resource "helm_release" "vault" {
  name       = "vault"
  repository = "https://helm.releases.hashicorp.com"
  chart      = "vault"
  namespace  = "vault"
  create_namespace = true

  set {
    name  = "server.dev.enabled"
    value = "true"
  }

  set {
    name  = "injector.enabled"
    value = "true"
  }
}
```

**Vault 初始化流程 (`scripts/k8s-vault-init.sh`):**
1. 等待 Vault pod ready
2. 启用 KV v2 secrets engine
3. 写入初始 secrets（从环境变量或交互式输入）
4. 配置 K8s auth method
5. 创建 policy 和 role

**Deployment 中的 Vault 注入:**
```yaml
spec:
  template:
    metadata:
      annotations:
        vault.hashicorp.com/agent-inject: "true"
        vault.hashicorp.com/role: "resume-builder"
        vault.hashicorp.com/agent-inject-secret-db-creds: "secret/data/resume-builder"
        vault.hashicorp.com/agent-inject-template-db-creds: |
          {{- with secret "secret/data/resume-builder" -}}
          export DB_USER="{{ .Data.data.DB_USER }}"
          export DB_PASS="{{ .Data.data.DB_PASS }}"
          export JWT_SECRET="{{ .Data.data.JWT_SECRET }}"
          {{- end }}
    spec:
      containers:
        - name: backend
          command: ["sh", "-c", ". /vault/secrets/db-creds && java -jar app.jar"]
```

**Secret 分层:**
| 类型 | 存储位置 | 示例 |
|---|---|---|
| 应用密钥 | Vault `secret/resume-builder` | DB_USER, DB_PASS, JWT_SECRET, MCP_API_KEY |
| 基础设施密钥 | Terraform variables (不提交) | Grafana admin password |
| CI/CD 密钥 | GitHub Secrets | GHCR token (自动) |

---

## Section 5: 目录重构与迁移

**最终态目录结构:**
```
resume-builder/
├── infra/
│   ├── terraform/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   ├── outputs.tf
│   │   ├── keda.tf
│   │   ├── monitoring.tf
│   │   ├── argocd.tf
│   │   ├── vault.tf
│   │   ├── vault-auth.tf
│   │   └── terraform.tfvars.example
│   └── argocd/
│       ├── app-of-apps.yaml
│       └── applications/
│           ├── resume-backend.yaml
│           ├── resume-frontend.yaml
│           ├── resume-infra.yaml
│           └── resume-scaling.yaml
├── k8s/
│   └── app/
│       ├── backend/
│       │   ├── deployment.yaml
│       │   └── service.yaml
│       ├── frontend/
│       │   ├── deployment.yaml
│       │   └── service.yaml
│       ├── config/
│       │   ├── configmap.yaml
│       │   └── secret.yaml
│       ├── ingress/
│       │   └── ingress.yaml
│       └── scaling/
│           ├── backend-scaler.yaml
│           └── frontend-scaler.yaml
├── scripts/
│   ├── k8s-start.sh             # 保留
│   ├── k8s-terraform-init.sh    # 新增
│   ├── k8s-argocd-bootstrap.sh  # 新增
│   ├── k8s-vault-init.sh        # 新增
│   ├── k8s-smoke-test.sh        # 保留
│   ├── k8s-delete.sh            # 保留
│   ├── k8s-build-push.sh        # 废弃 (CI 接管)
│   ├── k8s-apply.sh             # 废弃 (ArgoCD 接管)
│   └── k8s-install-keda.sh      # 废弃 (Terraform 接管)
├── .github/workflows/ci.yml     # 增强版
└── ... (backend/, frontend/ 不变)
```

**废弃清单:**
| 旧路径 | 替代 | 原因 |
|---|---|---|
| `k8s/00-namespace/` | ArgoCD CreateNamespace=true | 自动创建 |
| `k8s/01-config/` | `k8s/app/config/` | 迁移到 ArgoCD 管理 |
| `k8s/02-storage/` | 删除 | 空目录 |
| `k8s/04-backend/` | `k8s/app/backend/` | 迁移 |
| `k8s/05-frontend/` | `k8s/app/frontend/` | 迁移 |
| `k8s/06-ingress/` | `k8s/app/ingress/` | 迁移 |
| `k8s/07-observability/` | Terraform Helm | 基础设施层 |
| `k8s/08-scaling/` | `k8s/app/scaling/` | 迁移 |
| `scripts/k8s-build-push.sh` | CI build-and-push job | CI 接管 |
| `scripts/k8s-apply.sh` | ArgoCD 自动同步 | GitOps 接管 |
| `scripts/k8s-install-keda.sh` | terraform apply | Terraform 接管 |

**迁移执行顺序:**
1. 创建 `infra/terraform/` + `infra/argocd/` 目录结构
2. 编写 Terraform 配置
3. 编写 ArgoCD Application manifests
4. 重构 `k8s/` → `k8s/app/`（迁移 manifests，调整镜像引用为 ghcr.io）
5. 增强 `.github/workflows/ci.yml`
6. 新增 bootstrap 脚本
7. 废弃旧脚本
8. 更新 README 文档
9. 端到端验证

---

## 端到端部署流程

```
1. git push main
2. GitHub Actions:
   a. backend-test (mvn test)
   b. frontend-test (npm test + build)
   c. e2e (Docker Compose + Playwright)
   d. build-and-push:
      - docker build backend → ghcr.io/backend:<sha>
      - docker build frontend → ghcr.io/frontend:<sha>
      - update image tags in k8s/app/
      - git commit + push
3. ArgoCD detects k8s/app/ changes:
   - resume-backend: sync deployment + service
   - resume-frontend: sync deployment + service
   - resume-infra: sync configmap + ingress
   - resume-scaling: sync KEDA scalers
4. Vault Agent Injector:
   - 注入 DB_USER, DB_PASS, JWT_SECRET 到 backend pods
5. Pods running with latest code + secrets
```

**基础设施变更流程:**
```
1. 修改 infra/terraform/*.tf
2. terraform plan (review changes)
3. terraform apply
4. 基础设施组件更新 (KEDA/Prometheus/Grafana/ArgoCD/Vault)
```
