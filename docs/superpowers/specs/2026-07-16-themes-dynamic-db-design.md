# Design: Themes as Dynamic DB Data

## Problem

13 built-in themes are stored as static classpath files (`themes/<id>/style.css` + `theme.json`). On every app restart, `ThemeService.initBuiltInThemes()` (annotated `@PostConstruct`) reads these files and upserts into the DB, overwriting any prior theme edits. Theme modifications are ephemeral. The MCP `update_theme` tool can modify built-in themes but changes are lost on restart. The REST API blocks built-in theme updates entirely.

## Goal

Move all theme data into the DB as the single source of truth. Built-in themes are seeded once via Flyway migration. Users can modify any theme (built-in or custom) via UI or MCP, and changes persist across restarts. Delete protection remains on built-in themes.

## Migration Strategy

Two Flyway migrations replace `ddl-auto: update` + `@PostConstruct`:

| Migration | Content | Purpose |
|-----------|---------|---------|
| V1 | DDL for all 8 entity tables | Schema source of truth |
| V2 | INSERT 13 themes with CSS/variables/metadata | One-time theme seed with `ON CONFLICT DO NOTHING` |

### Flyway Config

```yaml
spring:
  jpa.hibernate.ddl-auto: validate
  flyway:
    baseline-on-migrate: true
    baseline-version: 1
```

- **Existing DB**: Flyway baselines at V1 (tables exist), runs V2 (no-op since themes already seeded by old `initBuiltInThemes`)
- **Fresh DB**: Flyway runs V1 (creates tables), V2 (seeds themes). Hibernate validates.

## Changes

### Backend — Files Modified

| File | Change |
|------|--------|
| `pom.xml` | Add `flyway-core` + `flyway-database-postgresql` dependencies |
| `application.yml` | Change `ddl-auto: validate`, add Flyway config, remove `app.themes.path` |
| `ThemeService.java` | Remove `@PostConstruct initBuiltInThemes()` + `loadOrRefreshBuiltIn()` (lines 44-104), remove `resourceLoader`/`themesPath` fields, remove `isBuiltIn()` guard from `updateCustom()`, keep delete guard |
| `ThemeController.java` | Remove dead `IllegalStateException` catch from `update()` |

### Backend — Files Created

| File | Content |
|------|---------|
| `db/migration/V1__create_tables.sql` | DDL for users, themes, resumes, resume_versions, resume_styles, share_links, section_templates, desensitize_rules |
| `db/migration/V2__seed_themes.sql` | 13 INSERT statements with `ON CONFLICT (id) DO NOTHING` — full CSS, variables JSON, layout, sort_order |

### Backend — Files Deleted

| Path | Count |
|------|-------|
| `resources/themes/` (entire directory) | 13 folders × 2 files (style.css + theme.json) |

### Frontend

| File | Change |
|------|--------|
| `ThemeSelector.tsx:202` | Remove `!theme.builtIn` condition — edit/delete controls visible for all themes |
| `ThemeSelector.tsx:244-247` | Change "Custom Theme" → "Theme" in dialog title/description |
| `ThemeSelector.tsx:199` | Change `!(Custom)` label to show `(Built-in)` for built-in themes |

### MCP

No changes. `McpThemeTools.updateTheme()` already calls `updateDirect()` which has no built-in guard.

## Entity Tables (V1 DDL)

8 tables derived from JPA entities: `users`, `themes`, `resumes`, `resume_versions`, `resume_styles`, `share_links`, `section_templates`, `desensitize_rules`.
