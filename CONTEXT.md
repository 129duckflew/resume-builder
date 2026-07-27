# Context — resume-builder

Domain glossary for the resume-builder project. Defines the ubiquitous language used across all modules.

---

## Core entities

**Resume** — A user's resume document. Owned by a User. Contains Markdown content, references a Theme for visual styling, and carries legacy style fields (fontSize, lineHeight, sectionSpacing) superseded by ResumeStyle. Identified by UUID string. Version history is captured as ResumeVersion snapshots on every update.

**User** — A registered account. Has username, email, BCrypt-hashed password, and an optional OpenAI API key for AI features. Identified by auto-increment Long ID.

**Theme** — A visual styling preset applied to a Resume. Built-in themes are seeded via Flyway migration and are immutable except through admin channels. Custom themes are user-owned. Contains CSS content, layout type, and an optional variables schema (JSON list of VariableDeclarations) describing customizable CSS properties. The `cssContent` field is sanitized on input through CssSanitizer.

**ResumeVersion** — A point-in-time snapshot of a Resume's content and styling. Created automatically before each update (max 50 per resume). Enables version diff (LCS-based) and restore. Has a monotonically increasing versionNumber per resume.

**ResumeStyle** — Per-resume-per-theme custom CSS variable overrides. Persisted as a JSON map of variable name → value in the `customVariables` column. Supersedes the legacy fontSize/lineHeight/sectionSpacing fields on Resume (now deprecated).

**ShareLink** — A shareable, optionally time-limited link to a Resume. Can be toggled on/off, set to expire, and optionally desensitized (phone/email redacted). Public access via `/s/{token}` bypasses authentication.

**SectionTemplate** — A reusable Markdown snippet (prompt) for resume sections (e.g. "Experience", "Education"). User-owned with sort order for display.

**DesensitizeRule** — A regex-based rule for masking sensitive content in shared resumes. Has a pattern, replacement string, enabled toggle, and sort order. System defaults are merged with user-defined rules.

---

## Rendering & export pipeline

**Export** — The pipeline that converts a Resume into a rendered output format: HTML preview, HTML download, or PDF. Orchestrated by ExportService.

**Preview** — A live, A4-sized rendering of the current Resume shown in the editor's right panel. Generated exclusively server-side via `POST /api/resumes/{id}/preview`. The frontend shows a loading skeleton while the server response is pending — no client-side Markdown rendering.

**Smart One-Page** — An iterative algorithm (SmartOnePageService) that measures a preview's scroll height (via the PDF service) and progressively reduces fontSize/lineHeight/spacing until the content fits within one A4 page. Used in both preview and export flows.

**JSON Resume** — Bidirectional conversion between the internal Markdown format and the [JSON Resume Schema](https://jsonresume.org/). Handled by JsonResumeConverter which uses regex-based section parsing for the markdown→JSON direction and structured build-up for JSON→markdown.

**PDF Service** — A separate Spring Boot microservice (port 8090) that wraps Playwright's headless Chromium. Exposes three endpoints: `/health`, `/pdf` (HTML → PDF bytes), `/measure` (HTML → scrollHeight). The backend communicates with it via PdfServiceClient over HTTP.

---

## Theme layout & rendering

**Layout** — The structural arrangement of a Theme's output. One of: `single` (one column), `sidebar-left` (two columns, sidebar on left), `sidebar-right` (two columns, sidebar on right), `header-bar` (header section with full-width body). Validated against a whitelist in ThemeService.

**Layout Splitter** — Classifies Markdown sections (delimited by H2 headings) as sidebar or main content based on keyword matching of section titles (e.g. "Contact", "Skills", "Languages" → sidebar).

**CssSanitizer** — Strips dangerous CSS constructs from user-submitted theme CSS: `@import`, `expression()`, `javascript:` URIs, and escape sequences.

**VariableDeclaration** — A descriptor for a customizable CSS property within a Theme. Has name, type (color|select|slider|text), defaultValue, label, group, and optional options array for select-type variables. Stored as JSON in the Theme's `variablesSchema` column.

**base.css** — A shared structural CSS template (`backend/src/main/resources/templates/base.css`) used by all built-in themes. Contains the full set of resume layout selectors, each referencing CSS custom properties via `var()`. At render time, ExportService injects per-theme `variablesSchema` defaults as a `:root {}` block, then appends base.css. Custom (user-created) themes may still supply their own `css_content`.

**Store Architecture** — Frontend state is split into two Zustand stores: `resumeStore` (resume list, currentResume, CRUD, setContent/setTitle) and `themeStore` (themes, theme CSS, variables, custom style overrides, theme CRUD). `resumeStore.fetchResume` bridges into `themeStore` to load theme CSS and saved style on resume load; `themeStore.setTheme` bridges into `resumeStore` to update the resume's themeId.

---

## MCP (Model Context Protocol)

**MCP Server** — A Spring AI streamable HTTP transport exposing resume and theme management as AI-callable tools. Has its own bearer-token authentication (MCP API key) separate from JWT-based web auth. Tools are registered via MethodToolCallbackProvider.

**MCP Tool** — A Spring AI `@Tool`-annotated method in McpResumeTools or McpThemeTools. Exposes CRUD operations on Resumes and Themes through the MCP protocol. Currently calls `*Direct` service variants that bypass user authorization — exists as an admin-level bypass.

---

## Ancillary

**API Key** — Two separate concepts: (1) the user's OpenAI API key stored per-user for AI features, and (2) the MCP API key used as a bearer token for MCP server authentication.

**AI Service** — Makes raw OpenAI Chat Completions API calls (not Spring AI) to rewrite or suggest resume content. Uses `java.net.http.HttpClient` directly.

**Draft Recovery** — Client-side localStorage backup of editor content that survives browser crashes. Compares against the server version on load and offers restore if divergent.

**Current User ID** — The authenticated user's Long ID, extracted from the JWT subject (format: `userId:username`) via `SecurityContextHolder.getAuthentication().getName()` — a pattern duplicated across 9+ files.
