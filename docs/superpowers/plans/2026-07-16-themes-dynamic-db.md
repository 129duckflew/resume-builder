# Themes as Dynamic DB Data — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move all 13 built-in themes from static classpath files into Flyway-managed DB seed data. DB becomes the single source of truth; users can modify any theme via UI/MCP with changes persisting across restarts.

**Architecture:** Flyway V1 creates tables, V2 seeds 13 themes with ON CONFLICT DO NOTHING. ddl-auto switched to validate. ThemeService loses @PostConstruct init. Built-in guard removed from updateCustom(). Classpath theme files deleted.

**Tech Stack:** Java 17, Spring Boot 3.2.5, Flyway, PostgreSQL, React + TypeScript

## Global Constraints

- DB is the only source of truth for themes — no classpath file fallback
- Built-in themes CAN be modified but CANNOT be deleted
- Flyway migrations run on startup; baselines existing DBs at V1
- MCP tools unchanged — already correct behavior via updateDirect()
- `ddl-auto: validate` — Hibernate validates schema against DB, no auto-DDL

---

### Task 1: Add Flyway Dependencies

**Files:**
- Modify: `backend/pom.xml`

**Interfaces:**
- Consumes: None
- Produces: Flyway available on classpath for Spring Boot auto-config

- [ ] **Step 1: Add flyway-core and flyway-database-postgresql to pom.xml**

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

Insert after the postgresql dependency block (after line 54).

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -f backend/pom.xml -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/pom.xml
git commit -m "feat: add Flyway dependencies for DB migration management"
```

---

### Task 2: Create V1 Schema Migration

**Files:**
- Create: `backend/src/main/resources/db/migration/V1__create_tables.sql`

**Interfaces:**
- Consumes: None
- Produces: All 8 entity tables in the DB (users, themes, resumes, resume_versions, resume_styles, share_links, section_templates, desensitize_rules)

- [ ] **Step 1: Create migration file**

Write the following SQL:

```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    api_key VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS themes (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    css_content TEXT,
    is_built_in BOOLEAN NOT NULL DEFAULT false,
    sort_order INTEGER,
    variables_schema TEXT,
    layout VARCHAR(20) NOT NULL DEFAULT 'single',
    user_id BIGINT
);

CREATE TABLE IF NOT EXISTS resumes (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    theme_id VARCHAR(50) NOT NULL DEFAULT 'classic',
    font_size FLOAT,
    line_height FLOAT,
    section_spacing VARCHAR(20) DEFAULT 'normal',
    user_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS resume_versions (
    id BIGSERIAL PRIMARY KEY,
    resume_id VARCHAR(36) NOT NULL,
    version_number INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    theme_id VARCHAR(50) NOT NULL,
    font_size FLOAT,
    line_height FLOAT,
    section_spacing VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_version_resume_id ON resume_versions(resume_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_version_resume_version ON resume_versions(resume_id, version_number);

CREATE TABLE IF NOT EXISTS resume_styles (
    id BIGSERIAL PRIMARY KEY,
    resume_id VARCHAR(36) NOT NULL,
    theme_id VARCHAR(50) NOT NULL,
    font_size FLOAT,
    line_height FLOAT,
    section_spacing VARCHAR(20),
    custom_variables TEXT,
    UNIQUE(resume_id, theme_id)
);

CREATE TABLE IF NOT EXISTS share_links (
    id VARCHAR(36) PRIMARY KEY,
    resume_id VARCHAR(36) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    desensitize BOOLEAN NOT NULL DEFAULT false,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS section_templates (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(50),
    prompt TEXT NOT NULL,
    sort_order INTEGER
);

CREATE TABLE IF NOT EXISTS desensitize_rules (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    pattern VARCHAR(500) NOT NULL,
    replacement VARCHAR(200) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER
);
```

- [ ] **Step 2: Verify file location**

Confirm: `backend/src/main/resources/db/migration/V1__create_tables.sql` exists

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/resources/db/migration/V1__create_tables.sql
git commit -m "feat: add Flyway V1 migration — create all entity tables"
```

---

### Task 3: Create V2 Theme Seed Migration

**Files:**
- Create: `backend/src/main/resources/db/migration/V2__seed_themes.sql`

**Interfaces:**
- Consumes: V1 tables (themes table must exist)
- Produces: 13 built-in themes seeded in DB with ON CONFLICT DO NOTHING

- [ ] **Step 1: Create migration file with 13 INSERT statements**

The file must contain one INSERT per theme. Each INSERT includes:
- id (e.g. 'classic')
- name (e.g. 'Classic')
- description
- css_content (the full CSS from each theme's style.css)
- is_built_in = true
- sort_order (1-13)
- variables_schema (JSON array of variable declarations from theme.json)
- layout (single, sidebar-left, sidebar-right, or header-bar)
- user_id = NULL

All CSS content must be proper SQL string literals — single quotes escaped as `''`.

Use `ON CONFLICT (id) DO NOTHING` for idempotency.

The 13 themes and their data come from the classpath files at `backend/src/main/resources/themes/<id>/theme.json` and `style.css`. The SQL file is large (~2500 lines) — write all 13 INSERTS.

- [ ] **Step 2: Commit**

```bash
git add backend/src/main/resources/db/migration/V2__seed_themes.sql
git commit -m "feat: add Flyway V2 migration — seed 13 built-in themes"
```

---

### Task 4: Update application.yml for Flyway

**Files:**
- Modify: `backend/src/main/resources/application.yml`

**Interfaces:**
- Consumes: Flyway dependencies (Task 1)
- Produces: Flyway auto-config with baseline, ddl-auto set to validate

- [ ] **Step 1: Change ddl-auto and add Flyway config**

Change line 23:
```yaml
# before:
      ddl-auto: update
# after:
      ddl-auto: validate
```

Add after line 28 (after the hibernate properties block):
```yaml
  flyway:
    baseline-on-migrate: true
    baseline-version: 1
```

Remove lines 33-35:
```yaml
# REMOVE:
app:
  themes:
    path: classpath:themes/
```

- [ ] **Step 2: Commit**

```bash
git add backend/src/main/resources/application.yml
git commit -m "feat: configure Flyway with baseline-on-migrate, switch to ddl-auto validate"
```

---

### Task 5: Refactor ThemeService — Remove Classpath Init

**Files:**
- Modify: `backend/src/main/java/com/resume/service/ThemeService.java`

**Interfaces:**
- Consumes: V2 seed migration (Task 3, themes now in DB)
- Produces: ThemeService no longer reads classpath files on startup

- [ ] **Step 1: Remove @PostConstruct init and classpath loading**

Remove lines 9-10 (imports):
```java
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
```

Remove lines 11-13 (imports):
```java
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
```

Remove line 34 (field):
```java
private final ResourceLoader resourceLoader;
```

Remove lines 36-37 (field):
```java
@Value("${app.themes.path:classpath:themes/}")
private String themesPath;
```

Change constructor to remove resourceLoader (lines 39-42):
```java
// BEFORE:
public ThemeService(ThemeRepository themeRepository, ResourceLoader resourceLoader) {
    this.themeRepository = themeRepository;
    this.resourceLoader = resourceLoader;
}
// AFTER:
public ThemeService(ThemeRepository themeRepository) {
    this.themeRepository = themeRepository;
}
```

Remove lines 44-59 (initBuiltInThemes method):
```java
// REMOVE entire method:
@PostConstruct
public void initBuiltInThemes() {
    loadOrRefreshBuiltIn("classic", "Classic", ...);
    ...
}
```

Remove lines 61-104 (loadOrRefreshBuiltIn method and objectMapper field at line 61):
```java
// REMOVE loadOrRefreshBuiltIn method entirely (lines 63-104)
// BUT KEEP the objectMapper field — move it. Change line 61:
// BEFORE:
private final ObjectMapper objectMapper = new ObjectMapper();
// AFTER — move to after the constructor:
// Keep: private final ObjectMapper objectMapper = new ObjectMapper();
// This field stays — it's used by getVariables() and createCustomTheme()
```

Also remove unused imports (lines 16-18):
```java
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
```
And (lines 20-24, keep only what's needed — check what remains):
```java
// Keep: Collections, List, Optional, Set
// Remove: Map if only used in loadOrRefreshBuiltIn
import java.util.Map; // REMOVE
```
Also remove:
```java
import java.util.UUID; // check if still used — yes, in createCustomTheme, KEEP
import java.util.stream.Collectors; // REMOVE
```

- [ ] **Step 2: Remove builtIn guard from updateCustom**

In `updateCustom()` method, lines 169-171:
```java
// REMOVE:
if (theme.isBuiltIn()) {
    throw new IllegalStateException("Cannot modify a built-in theme");
}
```

Change the ownership check (lines 172-174) to only apply for non-built-in:
```java
// BEFORE:
if (!userId.equals(theme.getUserId())) {
    throw new SecurityException("Not authorized to modify this theme");
}
// AFTER:
if (!theme.isBuiltIn() && !userId.equals(theme.getUserId())) {
    throw new SecurityException("Not authorized to modify this theme");
}
```

- [ ] **Step 3: Verify compilation**

Run: `mvn compile -f backend/pom.xml -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/resume/service/ThemeService.java
git commit -m "refactor: remove classpath theme loading, allow modifying built-in themes"
```

---

### Task 6: Update ThemeController — Remove Dead Catch

**Files:**
- Modify: `backend/src/main/java/com/resume/controller/ThemeController.java`

**Interfaces:**
- Consumes: ThemeService.updateCustom() no longer throws IllegalStateException for built-in
- Produces: Clean controller without dead catch block

- [ ] **Step 1: Remove IllegalStateException catch from update()**

Remove lines 81-82:
```java
} catch (IllegalStateException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
```

The delete() method's IllegalStateException catch stays (still thrown for built-in themes).

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -f backend/pom.xml -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/resume/controller/ThemeController.java
git commit -m "refactor: remove dead IllegalStateException catch from theme update"
```

---

### Task 7: Delete Classpath Theme Files

**Files:**
- Delete: `backend/src/main/resources/themes/` (entire directory)

**Interfaces:**
- Consumes: V2 seed migration (Task 3, themes now in DB)
- Produces: Clean resources directory, no classpath theme fallback

- [ ] **Step 1: Delete the themes directory**

```bash
rm -rf backend/src/main/resources/themes/
```

- [ ] **Step 2: Verify deletion**

Run: `ls backend/src/main/resources/themes/ 2>&1`
Expected: No such file or directory

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/resources/themes/
git commit -m "refactor: remove classpath theme files — now seeded via Flyway"
```

---

### Task 8: Update Frontend — Allow Editing All Themes

**Files:**
- Modify: `frontend/src/components/editor/ThemeSelector.tsx`

**Interfaces:**
- Consumes: Backend now allows modifying built-in themes (Task 5)
- Produces: Edit/delete controls visible for all themes, not just custom

- [ ] **Step 1: Show edit/delete buttons for all themes**

Change line 202 from:
```tsx
{!theme.builtIn && (
```
to: remove the condition — buttons always visible:
```tsx
{(
```

And the closing brace at line 221 changes from:
```tsx
)}
```
to:
```tsx
)}
```

Wait — we should keep the delete button hidden for built-in themes (backend still blocks deletion). Change the conditional to only show edit for built-in, both for custom:
```tsx
<div className="absolute bottom-2 right-2 z-20 flex gap-1">
  <button
    type="button"
    className="rounded bg-white/90 p-1 text-muted-foreground shadow-sm hover:text-primary"
    onClick={(e) => { e.stopPropagation(); openEdit(theme) }}
    title="Edit"
  >
    <Pencil className="h-3 w-3" />
  </button>
  {!theme.builtIn && (
    <button
      type="button"
      className="rounded bg-white/90 p-1 text-muted-foreground shadow-sm hover:text-destructive"
      onClick={(e) => { e.stopPropagation(); setOpen(false); setDeleteTarget(theme) }}
      title="Delete"
    >
      <Trash2 className="h-3 w-3" />
    </button>
  )}
</div>
```

- [ ] **Step 2: Update dialog title/labels**

Change lines 244-247:
```tsx
// BEFORE:
<DialogTitle>{editTheme ? 'Edit Theme' : 'Create Custom Theme'}</DialogTitle>
<DialogDescription>
  {editTheme ? 'Modify your custom theme settings.' : 'Create a new custom theme with your own CSS.'}
</DialogDescription>
// AFTER:
<DialogTitle>{editTheme ? 'Edit Theme' : 'Create Theme'}</DialogTitle>
<DialogDescription>
  {editTheme ? 'Modify theme settings.' : 'Create a new theme with your own CSS.'}
</DialogDescription>
```

- [ ] **Step 3: Update (Custom) label**

Change line 199 from showing "(Custom)" for non-built-in to showing "(Built-in)" for built-in:
```tsx
// BEFORE:
{!theme.builtIn && <span className="ml-1">(Custom)</span>}
// AFTER:
{theme.builtIn && <span className="ml-1 text-muted-foreground">(Built-in)</span>}
{!theme.builtIn && <span className="ml-1">(Custom)</span>}
```

- [ ] **Step 4: Verify build**

Run: `npm run build --prefix frontend`
Expected: No errors

- [ ] **Step 5: Commit**

```bash
git add frontend/src/components/editor/ThemeSelector.tsx
git commit -m "feat: allow editing all themes, show edit button on built-in themes"
```

---

### Task 9: Update ThemeServiceTest — Remove Init Tests, Fix BuiltIn Guard

**Files:**
- Modify: `backend/src/test/java/com/resume/service/ThemeServiceTest.java`

**Interfaces:**
- Consumes: ThemeService no longer has `initBuiltInThemes()`, `resourceLoader` field, or `isBuiltIn()` guard in `updateCustom()`
- Produces: All existing tests compile and pass; removed init-related tests; updated built-in guard test

- [ ] **Step 1: Remove resourceLoader mock and update constructor call**

Lines 10-11: Remove imports:
```java
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;
```
Also remove unused import on line 15:
```java
import java.util.stream.Collectors; // REMOVE (was only used in init tests)
```

Lines 28-29: Remove mock field:
```java
// REMOVE:
@Mock
private ResourceLoader resourceLoader;
```

Lines 34-36: Change constructor:
```java
// BEFORE:
service = new ThemeService(repository, resourceLoader);
// AFTER:
service = new ThemeService(repository);
```

- [ ] **Step 2: Remove initBuiltInThemes test methods**

Remove both test methods (lines 72-97):
- `initBuiltInThemes_overwritesExistingThemes()` (lines 72-83)
- `initBuiltInThemes_createsMissingTheme_withResourceFallback()` (lines 85-97)

- [ ] **Step 3: Fix updateCustom_builtIn test — now should succeed**

Replace lines 203-213:
```java
// BEFORE:
@Test
void updateCustom_builtIn_throws() {
    Theme existing = new Theme();
    existing.setId("classic");
    existing.setBuiltIn(true);

    when(repository.findById("classic")).thenReturn(Optional.of(existing));

    com.resume.dto.ThemeDTO dto = new com.resume.dto.ThemeDTO();
    assertThrows(IllegalStateException.class, () -> service.updateCustom("classic", dto, 1L));
}

// AFTER:
@Test
void updateCustom_builtIn_succeeds() {
    Theme existing = new Theme();
    existing.setId("classic");
    existing.setBuiltIn(true);

    when(repository.findById("classic")).thenReturn(Optional.of(existing));
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    com.resume.dto.ThemeDTO dto = new com.resume.dto.ThemeDTO();
    dto.setName("Updated Classic");

    Theme result = service.updateCustom("classic", dto, 1L);

    assertEquals("Updated Classic", result.getName());
}
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/test/java/com/resume/service/ThemeServiceTest.java
git commit -m "test: update ThemeServiceTest for removed init and built-in guard"
```

---

### Task 10: Update ThemeServiceVariablesTest — Remove Init Test

**Files:**
- Modify: `backend/src/test/java/com/resume/service/ThemeServiceVariablesTest.java`

**Interfaces:**
- Consumes: ThemeService no longer has `initBuiltInThemes()` or `resourceLoader`
- Produces: All remaining tests compile and pass

- [ ] **Step 1: Remove resourceLoader mock and update constructor**

Lines 11-12: Remove imports:
```java
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;
```

Lines 28-29: Remove mock field:
```java
// REMOVE:
@Mock
private ResourceLoader resourceLoader;
```

Lines 38-41: Change setUp():
```java
// BEFORE:
@BeforeEach
void setUp() {
    service = new ThemeService(repository, resourceLoader);
}

// AFTER:
@BeforeEach
void setUp() {
    service = new ThemeService(repository);
}
```

Also remove ALL_THEMES constant (lines 33-36) since it's only used by the init test:
```java
// REMOVE:
private static final List<String> ALL_THEMES = List.of(
        "classic", "modern", "minimal", "sidebar", "stackoverflow", "elegant", "compact",
        "sidebar-right", "header-bar", "jake", "academic", "swiss", "harvard"
);
```
Also remove unused import:
```java
import java.util.List; // check if still used by other tests — getVariables returns List, so KEEP
```
Actually keep `java.util.List` — used by `getVariables` return type.

- [ ] **Step 2: Remove initBuiltInThemes test method**

Remove `initBuiltInThemes_loadsVariablesSchema()` test (lines 43-65).

Also remove unused import after removing the init test:
```java
import java.nio.charset.StandardCharsets; // REMOVE
```
And static mock imports — check if `anyString` and `times` are still used:
```java
// verify: anyString — not used anymore, REMOVE
// verify: times — not used anymore, REMOVE
// verify: when, verify, mockito.* — keep verify, still used
```
Change:
```java
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
```
To:
```java
import static org.mockito.Mockito.*;
```
(anyString not needed, but keep `*` import which covers what's needed)

Actually, the remaining tests use `when(repository.findById(...))` which still needs static imports. Let's keep:
```java
import static org.mockito.Mockito.*;
```
And remove only `anyString`:
```java
// REMOVE line:
import static org.mockito.ArgumentMatchers.anyString;
```

- [ ] **Step 3: Commit**

```bash
git add backend/src/test/java/com/resume/service/ThemeServiceVariablesTest.java
git commit -m "test: update ThemeServiceVariablesTest for removed init"
```

---

### Task 11: Run All Tests and Verify

**Files:**
- (no code changes — verification only)

**Interfaces:**
- Consumes: All previous tasks completed
- Produces: All tests pass, build succeeds

- [ ] **Step 1: Run backend tests**

```bash
mvn test -f backend/pom.xml -q
```
Expected: BUILD SUCCESS — all tests pass

- [ ] **Step 2: Run frontend build**

```bash
npm run build --prefix frontend
```
Expected: No build errors

- [ ] **Step 3: Commit if any remaining fixes needed**

If any test failures remain, fix them and commit.
```
