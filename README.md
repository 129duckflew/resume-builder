# Resume Builder

A modern, privacy-first resume builder that separates content from design. Write in Markdown, pick a theme, and export to PDF or HTML — all running locally.

## Features

| Feature | Description |
|---|---|
| **Markdown Editor** | Write resumes in Markdown with live preview. Focus on content, not formatting. |
| **Real-time A4 Preview** | See exactly how your resume looks on A4 paper as you type. Resizable three-column layout (editor / preview / sections). |
| **13 Built-in Themes** | Classic, Modern, Minimal, Sidebar, Stack Overflow, Elegant, Compact, Sidebar Right, Header Bar, Jake's, Academic CV, Swiss, Harvard — switch with one click. |
| **Smart One-Page PDF** | Automatically adjusts font size, line height, and spacing to fit your resume on one page during PDF export. Toggle on/off. |
| **Section Drag & Drop** | Reorder sections (Experience, Education, etc.) by dragging in the sidebar. Click a section to jump the editor to it. |
| **Keyboard Shortcuts** | `Cmd+S` to save, `Cmd+Z` / `Cmd+Shift+Z` for undo/redo. Auto-save drafts to localStorage. |
| **PDF & HTML Export** | Server-side PDF generation via Playwright (headless Chromium) for pixel-perfect output. Standalone HTML export also supported. |
| **Docker Deploy** | One-command deployment with Docker Compose: PostgreSQL + Spring Boot + Nginx. |
| **Version History** | Server-side snapshots on every save. Browse, restore previous versions (up to 50 retained). |
| **Shareable Links** | Generate public read-only links with optional desensitization (hide name/phone/email/company). |
| **Desensitized Export** | Configurable rules to redact sensitive fields in PDF/HTML export and shared links. |
| **AI Assistant** | LLM-powered content rewrite and JD-based suggestions. Users manage their own API key. |
| **Custom Section Templates** | Create reusable section templates with built-in presets (experience, education, skills, projects). |
| **JSON Resume Import/Export** | Full round-trip support for the jsonresume.org schema. |
| **Style Persistence** | Font size, line height, and spacing customizations preserved when switching themes. |
| **Styled Delete Dialog** | Custom confirmation dialog replacing native `confirm()` for destructive actions. |
| **MCP Server** | Model Context Protocol server enabling AI assistants to manage resumes and themes via SSE/STDIO. |

## Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Spring Boot 4.1 / Java 25 |
| **API** | REST (Spring MVC) |
| **ORM** | Spring Data JPA + Hibernate |
| **Database** | PostgreSQL 16 |
| **Markdown** | commonmark (org.commonmark) |
| **PDF Engine** | Playwright for Java (headless Chromium) |
| **Frontend** | React 18 / TypeScript / Vite |
| **Components** | shadcn/ui + Tailwind CSS |
| **Editor** | @uiw/react-md-editor (CodeMirror 6) |
| **Drag & Drop** | @dnd-kit/core + @dnd-kit/sortable |
| **State** | Zustand |
| **Deployment** | Docker Compose |

## Architecture

```
resume-builder/
├── backend/                          # Spring Boot 3.2
│   ├── pom.xml
│   ├── Dockerfile                    # Playwright Java base image
│   └── src/main/java/com/resume/
│       ├── ResumeApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java    # JWT + public path rules
│       │   ├── JwtAuthFilter.java
│       │   ├── JwtUtil.java
│       │   ├── CorsConfig.java
│       │   └── PlaywrightConfig.java  # Chromium singleton
│       ├── controller/
│       │   ├── AuthController.java             # /api/auth (register, login)
│       │   ├── ResumeController.java           # CRUD + preview + export + import + styles
│       │   ├── ThemeController.java            # theme list + CSS
│       │   ├── SectionTemplateController.java  # section templates CRUD
│       │   ├── ResumeVersionController.java    # version snapshots + restore
│       │   ├── ShareLinkController.java        # share links + public /s/{token}
│       │   ├── AiController.java               # AI rewrite + suggest
│       │   ├── DesensitizeController.java      # desensitization rules
│       │   └── UserSettingsController.java     # AI API key management
│       ├── entity/                   # Resume, Theme, User, SectionTemplate, ResumeStyle, ResumeVersion, ShareLink, DesensitizeRule
│       ├── repository/               # 8 JpaRepository interfaces
│       ├── service/
│       │   ├── ResumeService.java
│       │   ├── ThemeService.java
│       │   ├── MarkdownService.java            # md → HTML
│       │   ├── ExportService.java              # HTML generation
│       │   ├── SmartOnePageService.java        # Playwright auto-fit
│       │   ├── PdfGenerationService.java       # Playwright PDF
│       │   ├── SectionTemplateService.java
│       │   ├── ResumeStyleService.java
│       │   ├── JsonResumeConverter.java
│       │   ├── ResumeVersionService.java
│       │   ├── ShareLinkService.java
│       │   ├── AiService.java
│       │   ├── DesensitizeService.java
│       │   └── UserService.java
│       └── dto/                      # ResumeDTO, JsonResumeDTO
├── frontend/                         # React 18 + Vite
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.ts
│   ├── nginx.conf                    # SPA + API proxy
│   ├── Dockerfile                    # Node build → Nginx serve
│   └── src/
│       ├── pages/
│       │   ├── HomePage.tsx          # Resume list
│       │   ├── EditorPage.tsx        # Three-column editor
│       │   ├── PreviewPage.tsx       # Full A4 preview
│       │   ├── LoginPage.tsx
│       │   └── RegisterPage.tsx
│       ├── components/
│       │   ├── Layout.tsx
│       │   ├── ui/                   # shadcn: button, dialog, toast, dropdown-menu, confirm-dialog
│       │   └── editor/
│       │       ├── SectionDragList.tsx
│       │       ├── SortableSection.tsx
│       │       ├── SectionTemplatePicker.tsx
│       │       ├── ThemeSelector.tsx
│       │       ├── ExportPanel.tsx
│       │       ├── VersionPanel.tsx
│       │       ├── SharePanel.tsx
│       │       ├── AiAssistant.tsx
│       │       └── DesensitizeSettings.tsx
│       ├── stores/                   # authStore, resumeStore, historyStore
│       ├── types/                    # resume.ts, sectionTemplate.ts, desensitize.ts
│       ├── hooks/                    # useKeyboardShortcuts, useDraftBackup, use-toast
│       ├── lib/                      # api.ts, markdown.ts, utils.ts
│       └── e2e/                      # Playwright E2E tests (Docker, baseURL http://frontend:80)
├── docker-compose.yml                # PostgreSQL + Backend + Frontend
├── AGENTS.md                         # TDD development guidelines
├── ROADMAP.md                        # Project roadmap
├── progress.md                       # Development progress
├── .env.example
└── LICENSE
```

## Quick Start (Local Development)

```bash
# Prerequisites: Java 25+, Node.js 20+, PostgreSQL 16 (via Homebrew)

# 1. Start PostgreSQL (macOS via Homebrew)
brew services start postgresql@16

# 2. Start backend (port 8080)
cd backend && mvn spring-boot:run

# 3. Start frontend (port 3000, proxies /api to 8080)
cd frontend && npm install && npm run dev

# Open http://localhost:3000
```

> **Playwright / PDF export (optional):** For PDF export to work locally, Chromium must be installed:
> ```bash
> cd frontend && npx playwright install chromium
> ```

## Docker Deployment

```bash
# Build and start all services
docker compose up --build -d

# Frontend: http://localhost:3000
# API:      http://localhost:8081/api/...  (port 8080 may be occupied)
# DB:       localhost:5432
```

Services:

| Container | Image | Port |
|---|---|---|
| `resume-backend` | resume-builder-backend | 8081 → 8080 |
| `resume-frontend` | resume-builder-frontend | 3000 → 80 |

## Kubernetes Deployment (Colima k3s — GitOps)

Prerequisites: [Colima](https://github.com/abiosoft/colima) with Kubernetes enabled, 4+ CPUs, 8GB RAM, Terraform CLI, ArgoCD CLI.

### Architecture

```
GitHub (CI) → ghcr.io (images) → ArgoCD (GitOps CD) → k3s cluster
                                     ↑
                  Terraform (infra) ──┘  Vault (secrets)
```

| Layer | Tool | Purpose |
|---|---|---|
| CI | GitHub Actions | Test → build images → push to ghcr.io → update manifests |
| CD | ArgoCD | Auto-sync K8s resources from Git |
| Infrastructure | Terraform + Helm | KEDA, Prometheus, Grafana, ArgoCD, Vault |
| Secrets | Vault | All application secrets (DB, JWT, API keys) |
| Images | ghcr.io | Container image registry |

### Quick Start

```bash
# 1. Start k3s
./scripts/k8s-start.sh

# 2. Provision infrastructure (KEDA, Prometheus, Grafana, ArgoCD, Vault)
./scripts/k8s-terraform-init.sh

# 3. Initialize Vault secrets
./scripts/k8s-vault-init.sh

# 4. Bootstrap ArgoCD (auto-sync from Git)
./scripts/k8s-argocd-bootstrap.sh

# 5. Verify
./scripts/k8s-smoke-test.sh
```

### End-to-End Flow

1. `git push main` → GitHub Actions runs tests
2. Tests pass → build backend/frontend images → push to ghcr.io
3. CI updates image tags in `k8s/app/` → commits back to repo
4. ArgoCD detects changes → auto-syncs to k3s
5. Vault Agent Injector injects secrets into pods

### Services

| Service | URL |
|---|---|
| Frontend (SPA) | http://resume.local |
| API | http://resume.local/api/* |
| Grafana | http://localhost:30000 |
| ArgoCD UI | http://localhost:30080 |
| Vault | `kubectl port-forward -n vault svc/vault 8200:8200` |

### Directory Structure

```
infra/
├── terraform/          # Infrastructure as Code (Helm releases)
│   ├── main.tf         # Provider config
│   ├── keda.tf         # KEDA + HTTP Add-on
│   ├── monitoring.tf   # kube-prometheus-stack
│   ├── argocd.tf       # ArgoCD
│   ├── vault.tf        # HashiCorp Vault
│   └── vault-auth.tf   # Vault K8s auth
└── argocd/             # ArgoCD Application manifests
    ├── app-of-apps.yaml
    └── applications/   # Per-component Application CRDs

k8s/app/                # ArgoCD-managed K8s manifests
├── backend/            # Deployment + Service
├── frontend/           # Deployment + Service
├── config/             # ConfigMap + Secret (placeholder)
├── ingress/            # Traefik IngressRoute
└── scaling/            # KEDA HTTPScaledObjects
```

### Scripts

| Script | Purpose | Status |
|---|---|---|
| `k8s-start.sh` | Start Colima k3s | Active |
| `k8s-terraform-init.sh` | `terraform init + apply` | **New** |
| `k8s-vault-init.sh` | Initialize Vault secrets | **New** |
| `k8s-argocd-bootstrap.sh` | Deploy ArgoCD app-of-apps | **New** |
| `k8s-smoke-test.sh` | Verify scale-from-zero | Active |
| `k8s-delete.sh` | Delete application resources | Active |
| `k8s-apply.sh` | Manual kubectl apply | Deprecated (ArgoCD) |
| `k8s-build-push.sh` | Build images locally | Deprecated (CI) |
| `k8s-install-keda.sh` | Install KEDA | Deprecated (Terraform) |

### Scale-from-zero (KEDA HTTP Add-on)

| Deployment | Min | Max | Cold start | Scaledown |
|---|---|---|---|---|
| `resume-backend` | 0 | 3 | ~18s | 300s idle |
| `resume-frontend` | 0 | 3 | ~8s | 300s idle |

### Infrastructure Management

```bash
# Update infrastructure (KEDA, Prometheus, etc.)
cd infra/terraform
terraform plan
terraform apply

# Destroy infrastructure
terraform destroy

# Update Vault secrets
kubectl exec -n vault vault-0 -- vault kv put secret/resume-builder DB_USER=resume DB_PASS=newpass

# Force ArgoCD sync
argocd app sync resume-backend
```

## Themes

13 built-in themes are available. They are seeded into the database via Flyway migration on first startup and can be customized through the UI or MCP tools.

| Theme | ID | Style | Colors |
|---|---|---|---|
| Classic | `classic` | Traditional corporate, serif | Black / White |
| Modern | `modern` | Clean sans-serif, tech | Blue `#2563eb` |
| Minimal | `minimal` | Ultra-minimalist, academic | Gray `#999` |
| Sidebar | `sidebar` | Two-column with colored sidebar | Navy `#1a365d` |
| Stack Overflow | `stackoverflow` | Developer community style | Orange `#f48024` |
| Elegant | `elegant` | Refined business, serif | Green `#1b4332` |
| Compact | `compact` | Dense layout, experienced pros | Dark `#555` |
| Sidebar Right | `sidebar-right` | Two-column sidebar on right | Navy `#1a365d` |
| Header Bar | `header-bar` | Top header with colored background | Navy `#1a365d` |
| Jake's Resume | `jake` | ATS-optimized sans-serif | Navy `#1a3a5c` |
| Academic CV | `academic` | Scholarly serif, hanging indent | Dark `#2d2d2d` |
| Swiss | `swiss` | Swiss minimalist with grid | Red `#c8102e` |
| Harvard | `harvard` | MBA-style with uppercase | Black |

## Testing

```bash
# Backend (117 tests)
cd backend && mvn test

# Frontend (72 tests)
cd frontend && npm test

# Total: 189 tests

# E2E (Playwright, runs inside Docker)
docker compose up --build -d
cd frontend/e2e && npx playwright test
```

Test stack:
- Backend: JUnit 5, Mockito, Spring MockMvc, AssertJ
- Frontend: Vitest, @testing-library/react, @testing-library/user-event
- E2E: Playwright (headless Chromium, runs in Docker)

## API Overview

| Method | Path | Description |
|---|---|---|
| **AuthController** (`/api/auth`) — **public** | |
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Login, returns JWT |
| **ResumeController** (`/api/resumes`) | |
| `GET` | `/api/resumes` | List current user's resumes |
| `POST` | `/api/resumes` | Create a new resume |
| `GET` | `/api/resumes/{id}` | Get a single resume |
| `PUT` | `/api/resumes/{id}` | Update resume (partial, null fields ignored) |
| `DELETE` | `/api/resumes/{id}` | Delete a resume |
| `POST` | `/api/resumes/{id}/preview` | Get rendered HTML preview |
| `POST` | `/api/resumes/{id}/export/html` | Download standalone HTML |
| `POST` | `/api/resumes/{id}/export/pdf` | Download PDF (optional `?smartOnePage=true`) |
| `POST` | `/api/resumes/import/json` | Import from JSON Resume format |
| `GET` | `/api/resumes/{id}/export/json` | Export to JSON Resume format |
| `GET` | `/api/resumes/{id}/styles` | Get custom style overrides |
| `PUT` | `/api/resumes/{id}/styles` | Update style overrides (preserved across theme switches) |
| **ThemeController** (`/api/themes`) — **public** | |
| `GET` | `/api/themes` | List available themes |
| `GET` | `/api/themes/{id}` | Get theme metadata |
| `GET` | `/api/themes/{id}/css` | Get theme CSS content |
| **SectionTemplateController** (`/api/section-templates`) | |
| `GET` | `/api/section-templates` | List templates (built-in + user's custom) |
| `POST` | `/api/section-templates` | Create custom template |
| `PUT` | `/api/section-templates/{id}` | Update template |
| `DELETE` | `/api/section-templates/{id}` | Delete template |
| **ResumeVersionController** (`/api/resumes/{resumeId}/versions`) | |
| `GET` | `/api/resumes/{resumeId}/versions` | List version snapshots |
| `GET` | `/api/resumes/{resumeId}/versions/{version}` | Get a specific version |
| `POST` | `/api/resumes/{resumeId}/versions/{version}/restore` | Restore a version |
| **ShareLinkController** — **mixed** | |
| `GET` | `/api/resumes/{resumeId}/shares` | List share links |
| `POST` | `/api/resumes/{resumeId}/shares` | Create share link |
| `DELETE` | `/api/shares/{linkId}` | Delete share link |
| `GET` | `/s/{token}` | Public read-only access (no JWT) |
| **AiController** (`/api/resumes/{resumeId}/ai`) | |
| `POST` | `/api/resumes/{resumeId}/ai/rewrite` | AI rewrite current content |
| `POST` | `/api/resumes/{resumeId}/ai/suggest` | AI suggestions based on JD |
| **DesensitizeController** (`/api/users`) | |
| `GET` | `/api/users/desensitize-rules` | Get desensitization rules |
| `PUT` | `/api/users/desensitize-rules` | Update desensitization rules |
| **UserSettingsController** (`/api/users`) | |
| `GET` | `/api/users/api-key` | Check if AI API key is set |
| `PUT` | `/api/users/api-key` | Set/update AI API key |
| **MCP Server** (`/mcp`) — **public** (API key auth) | |
| `GET` | `/mcp/sse` | SSE endpoint for MCP clients |
| `POST` | `/mcp/message` | Message endpoint for MCP clients |

## License

[MIT](LICENSE)

## MCP Client Configuration

### SSE (remote)

```json
{
  "mcpServers": {
    "resume-builder": {
      "url": "http://localhost:8080/mcp/sse",
      "headers": {
        "Authorization": "Bearer <MCP_API_KEY>"
      }
    }
  }
}
```

### STDIO (local)

```json
{
  "mcpServers": {
    "resume-builder": {
      "command": "java",
      "args": ["-jar", "backend/target/resume-builder.jar", "--spring.ai.mcp.server.stdio=true"],
      "env": {
        "MCP_API_KEY": "<your-key>"
      }
    }
  }
}
```

### Available MCP Tools

| Tool | Description |
|---|---|
| `list_resumes` | List all resumes, optionally filtered by user ID |
| `get_resume` | Get a resume by its ID |
| `create_resume` | Create a new resume for a user |
| `update_resume` | Update resume fields (partial) |
| `delete_resume` | Delete a resume permanently |
| `list_versions` | List version history for a resume |
| `get_version` | Get a specific snapshot version |
| `restore_version` | Restore a previous version |
| `diff_versions` | Compare two versions and return a structured diff |
| `list_themes` | List all themes (built-in and custom) |
| `get_theme` | Get theme metadata, CSS, variables, and layout |
| `update_theme` | Update any theme including built-in (super admin only) |
