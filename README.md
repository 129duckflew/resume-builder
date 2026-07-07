# Resume Builder

A modern, privacy-first resume builder that separates content from design. Write in Markdown, pick a theme, and export to PDF or HTML — all running locally.

## Features

| Feature | Description |
|---|---|
| **Markdown Editor** | Write resumes in Markdown with live preview. Focus on content, not formatting. |
| **Real-time A4 Preview** | See exactly how your resume looks on A4 paper as you type. Resizable three-column layout (editor / preview / sections). |
| **7 Built-in Themes** | Classic, Modern, Minimal, Sidebar, Stack Overflow, Elegant, Compact — switch with one click. |
| **Smart One-Page PDF** | Automatically adjusts font size, line height, and spacing to fit your resume on one page during PDF export. Toggle on/off. |
| **Section Drag & Drop** | Reorder sections (Experience, Education, etc.) by dragging in the sidebar. Click a section to jump the editor to it. |
| **Keyboard Shortcuts** | `Cmd+S` to save, `Cmd+Z` / `Cmd+Shift+Z` for undo/redo. Auto-save drafts to localStorage. |
| **PDF & HTML Export** | Server-side PDF generation via Playwright (headless Chromium) for pixel-perfect output. Standalone HTML export also supported. |
| **Docker Deploy** | One-command deployment with Docker Compose: PostgreSQL + Spring Boot + Nginx. |

## Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Spring Boot 3.2 / Java 17 |
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
│       │   ├── CorsConfig.java
│       │   └── PlaywrightConfig.java  # Chromium singleton
│       ├── controller/
│       │   ├── ResumeController.java  # CRUD + preview + export
│       │   └── ThemeController.java   # theme list + CSS
│       ├── entity/
│       │   ├── Resume.java
│       │   └── Theme.java
│       ├── repository/
│       ├── service/
│       │   ├── ResumeService.java
│       │   ├── MarkdownService.java   # md → HTML
│       │   ├── ExportService.java     # HTML generation
│       │   ├── SmartOnePageService.java  # Playwright auto-fit
│       │   ├── PdfGenerationService.java # Playwright PDF
│       │   └── ThemeService.java
│       └── dto/
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
│       │   └── PreviewPage.tsx       # Full A4 preview
│       ├── components/
│       │   ├── ui/                   # shadcn components
│       │   └── editor/               # Editor-specific
│       ├── stores/resumeStore.ts     # Zustand
│       ├── hooks/                    # useKeyboardShortcuts, useDraftBackup
│       └── lib/                      # api.ts, markdown.ts
├── docker-compose.yml                # PostgreSQL + Backend + Frontend
├── AGENTS.md                         # TDD development guidelines
├── progress.md                       # Development roadmap
└── .env.example
```

## Quick Start (Local Development)

```bash
# Prerequisites: Java 17+, Node.js 20+, Docker (for PostgreSQL)

# 1. Start PostgreSQL
docker compose up -d postgres

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
| `resume-postgres` | postgres:16-alpine | 5432 |
| `resume-backend` | resume-builder-backend | 8081 → 8080 |
| `resume-frontend` | resume-builder-frontend | 3000 → 80 |

## Themes

| Theme | ID | Style | Colors |
|---|---|---|---|
| Classic | `classic` | Traditional corporate, serif | Black / White |
| Modern | `modern` | Clean sans-serif, tech | Blue `#2563eb` |
| Minimal | `minimal` | Ultra-minimalist, academic | Gray `#999` |
| Sidebar | `sidebar` | Two-column with colored sidebar | Navy `#1a365d` |
| Stack Overflow | `stackoverflow` | Developer community style | Orange `#f48024` |
| Elegant | `elegant` | Refined business, serif | Green `#1b4332` |
| Compact | `compact` | Dense layout, experienced pros | Dark `#555` |

Add a new theme by creating a directory in `backend/src/main/resources/themes/{id}/` with:
- `theme.json` — name, description, sort order
- `style.css` — CSS with selectors for `h1`, `h2`, `h3`, `p`, `ul`/`li`, `strong`, `em`, `a`, `@page`, `.resume-page`, `@media print`

## Testing

```bash
# Backend (54 tests)
cd backend && mvn test

# Frontend (44 tests)
cd frontend && npm test

# Total: 98 tests
```

Test stack:
- Backend: JUnit 5, Mockito, Spring MockMvc, AssertJ
- Frontend: Vitest, @testing-library/react, @testing-library/user-event

## API Overview

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/resumes` | List all resumes |
| `POST` | `/api/resumes` | Create a new resume |
| `GET` | `/api/resumes/{id}` | Get a single resume |
| `PUT` | `/api/resumes/{id}` | Update resume (partial) |
| `DELETE` | `/api/resumes/{id}` | Delete a resume |
| `POST` | `/api/resumes/{id}/preview` | Get rendered HTML preview |
| `POST` | `/api/resumes/{id}/export/html` | Download standalone HTML |
| `POST` | `/api/resumes/{id}/export/pdf?smartOnePage=true` | Download PDF |
| `GET` | `/api/themes` | List available themes |
| `GET` | `/api/themes/{id}/css` | Get theme CSS content |

## License

[MIT](LICENSE)
