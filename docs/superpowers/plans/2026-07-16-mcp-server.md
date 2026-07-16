# MCP Server Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an MCP server as a Spring Boot module enabling AI assistants to manage resumes and themes via STDIO / SSE.

**Architecture:** Spring AI `spring-ai-starter-mcp-server-webmvc` (1.0.9) embedded in the existing backend. Tools are `@McpTool` annotated Spring components with injected services. A configurable API key authenticates super admin access which bypasses per-user ownership checks.

**Tech Stack:** Spring Boot 3.2.5, Spring AI 1.0.9, Java 17, Maven

## Global Constraints

- Spring Boot parent version: 3.2.5
- Java version: 17
- MCP server uses `spring-ai-starter-mcp-server-webmvc` version managed via `spring-ai-bom:1.0.9`
- Super admin API key configured in `application.yml` as `app.mcp.api-key` (env: `MCP_API_KEY`)
- `@McpTool` annotation-based tools; existing services reused via DI
- Tools: `list_resumes`, `get_resume`, `create_resume`, `update_resume`, `delete_resume`, `list_versions`, `get_version`, `restore_version`, `diff_versions`, `list_themes`, `get_theme`, `update_theme`

---

### Task 1: Add MCP dependency and configuration

**Files:**
- Modify: `backend/pom.xml`
- Modify: `backend/src/main/resources/application.yml`

**Interfaces:**
- Consumes: nothing from other tasks
- Produces: Maven build compiles with MCP dependencies; Spring Boot recognizes MCP auto-configuration

- [ ] **Step 1: Add Spring AI BOM and MCP starter to pom.xml**

Add BOM import to `<dependencyManagement>` and the starter to `<dependencies>`:

```xml
<!-- Inside pom.xml, after <properties> and before <dependencies> -->
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.ai</groupId>
      <artifactId>spring-ai-bom</artifactId>
      <version>1.0.9</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

```xml
<!-- Inside <dependencies> -->
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

- [ ] **Step 2: Add MCP config to application.yml**

Read the existing `application.yml` first, then append:

```yaml
spring:
  ai:
    mcp:
      server:
        name: resume-mcp-server
        version: 1.0.0
        type: SYNC
        stdio: true
        sse-message-endpoint: /mcp/message
        sse-endpoint: /mcp/sse

app:
  mcp:
    api-key: ${MCP_API_KEY:}
```

- [ ] **Step 3: Verify build compiles**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/pom.xml backend/src/main/resources/application.yml
git commit -m "feat: add Spring AI MCP server dependency and config"
```

---

### Task 2: Add direct-access methods to services

**Files:**
- Modify: `backend/src/main/java/com/resume/service/ResumeService.java`
- Modify: `backend/src/main/java/com/resume/service/ThemeService.java`

**Interfaces:**
- Consumes: existing `ResumeRepository`, `ThemeRepository`, `ResumeVersionService`, `ResumeDTO`, `ThemeDTO`
- Produces: `ResumeService.findAll()`, `ResumeService.updateDirect(String, ResumeDTO)`, `ResumeService.deleteDirect(String)`, `ThemeService.updateDirect(String, ThemeDTO)`

- [ ] **Step 1: Add findAll, updateDirect, deleteDirect to ResumeService**

```java
// Add after existing findById method (line ~36)
public List<Resume> findAll() {
    return resumeRepository.findAll();
}
```

```java
// Add after existing update method (line ~115)
@Transactional
public Resume updateDirect(String id, ResumeDTO dto) {
    Resume resume = resumeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Resume not found: " + id));
    versionService.saveSnapshot(resume);
    if (dto.getTitle() != null) resume.setTitle(dto.getTitle());
    if (dto.getContent() != null) resume.setContent(dto.getContent());
    if (dto.getThemeId() != null) resume.setThemeId(dto.getThemeId());
    if (dto.getFontSize() != null) resume.setFontSize(dto.getFontSize());
    if (dto.getLineHeight() != null) resume.setLineHeight(dto.getLineHeight());
    if (dto.getSectionSpacing() != null) resume.setSectionSpacing(dto.getSectionSpacing());
    return resumeRepository.save(resume);
}
```

```java
// Add after existing delete method (line ~135)
public void deleteDirect(String id) {
    Resume resume = resumeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Resume not found: " + id));
    resumeRepository.delete(resume);
}
```

- [ ] **Step 2: Add updateDirect to ThemeService**

```java
// Add after existing updateCustom method (line ~193)
public Theme updateDirect(String id, ThemeDTO dto) {
    Theme theme = themeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Theme not found: " + id));
    if (dto.getName() != null) theme.setName(dto.getName().trim());
    if (dto.getDescription() != null) theme.setDescription(dto.getDescription());
    if (dto.getLayout() != null) {
        theme.setLayout(dto.getLayout());
    }
    if (dto.getCssContent() != null) {
        theme.setCssContent(CssSanitizer.sanitize(dto.getCssContent()));
    }
    if (dto.getVariablesSchema() != null) {
        theme.setVariablesSchema(dto.getVariablesSchema());
    }
    return themeRepository.save(theme);
}
```

- [ ] **Step 3: Verify build compiles**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/resume/service/ResumeService.java backend/src/main/java/com/resume/service/ThemeService.java
git commit -m "feat: add direct-access service methods for MCP super admin"
```

---

### Task 3: Implement MCP auth config

**Files:**
- Create: `backend/src/main/java/com/resume/mcp/McpAuthConfig.java`

**Interfaces:**
- Consumes: `environment.getProperty("app.mcp.api-key")`
- Produces: `McpAuthConfig.isAuthorized(McpTransportContext)` and `McpAuthConfig.isAuthorized()` for STDIO

- [ ] **Step 1: Create McpAuthConfig class**

```java
package com.resume.mcp;

import org.springframework.ai.mcp.server.transport.StreamableHttpServerTransport;
import org.springframework.ai.mcp.spec.McpSchema;
import org.springframework.ai.mcp.transport.McpTransportContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class McpAuthConfig {

    @Value("${app.mcp.api-key:}")
    private String mcpApiKey;

    @Bean
    public StreamableHttpServerTransport.TransportContextExtractor transportContextExtractor() {
        return serverRequest -> {
            String auth = serverRequest.headers().firstHeader("Authorization");
            return McpTransportContext.create(java.util.Map.of(
                "authorization", auth
            ));
        };
    }

    public boolean isAuthorized(McpTransportContext context) {
        if (mcpApiKey == null || mcpApiKey.isEmpty()) {
            return true; // No key configured = allow all
        }
        String auth = (String) context.get("authorization");
        if (auth == null) return false;
        String token = auth.startsWith("Bearer ") ? auth.substring(7) : auth;
        return mcpApiKey.equals(token);
    }

    public boolean isAuthorized() {
        if (mcpApiKey == null || mcpApiKey.isEmpty()) {
            return true;
        }
        // STDIO mode: check env var as fallback
        String envKey = System.getenv("MCP_API_KEY");
        return mcpApiKey.equals(envKey);
    }
}
```

- [ ] **Step 2: Verify build compiles**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/resume/mcp/McpAuthConfig.java
git commit -m "feat: add MCP auth config with transport context extractor"
```

---

### Task 4: Implement resume MCP tools

**Files:**
- Create: `backend/src/main/java/com/resume/mcp/McpResumeTools.java`

**Interfaces:**
- Consumes: `ResumeService.findAll()`, `ResumeService.findById(id)`, `ResumeService.create(dto, userId)`, `ResumeService.updateDirect(id, dto)`, `ResumeService.deleteDirect(id)`, `ResumeVersionService.getVersions(resumeId)`, `ResumeVersionService.getVersion(resumeId, version)`, `ResumeVersionService.restoreVersion(resumeId, version)`, `ResumeVersionService.getDiff(resumeId, a, b)`, `VersionDiffResponse`
- Produces: MCP tool callable functions

- [ ] **Step 1: Write McpResumeTools class**

```java
package com.resume.mcp;

import com.resume.dto.ResumeDTO;
import com.resume.dto.VersionDiffResponse;
import com.resume.entity.Resume;
import com.resume.entity.ResumeVersion;
import com.resume.service.ResumeService;
import com.resume.service.ResumeVersionService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class McpResumeTools {

    private final ResumeService resumeService;
    private final ResumeVersionService versionService;
    private final McpAuthConfig authConfig;

    public McpResumeTools(ResumeService resumeService,
                          ResumeVersionService versionService,
                          McpAuthConfig authConfig) {
        this.resumeService = resumeService;
        this.versionService = versionService;
        this.authConfig = authConfig;
    }

    @Tool(description = "List all resumes, optionally filtered by user ID")
    public List<Resume> listResumes(
            @ToolParam(description = "Optional user ID to filter resumes by") Long userId) {
        if (userId != null) {
            return resumeService.findByUserId(userId);
        }
        return resumeService.findAll();
    }

    @Tool(description = "Get a resume by its ID")
    public Resume getResume(
            @ToolParam(description = "Resume ID") String id) {
        return resumeService.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found: " + id));
    }

    @Tool(description = "Create a new resume for a user")
    public Resume createResume(
            @ToolParam(description = "User ID who will own this resume") Long userId,
            @ToolParam(description = "Resume title", required = false) String title,
            @ToolParam(description = "Markdown content", required = false) String content,
            @ToolParam(description = "Theme ID (e.g. classic, modern)", required = false) String themeId) {
        ResumeDTO dto = new ResumeDTO();
        dto.setTitle(title);
        dto.setContent(content);
        dto.setThemeId(themeId);
        return resumeService.create(dto, userId);
    }

    @Tool(description = "Update a resume partially. Only provided fields are changed.")
    public Resume updateResume(
            @ToolParam(description = "Resume ID") String id,
            @ToolParam(description = "New title", required = false) String title,
            @ToolParam(description = "New markdown content", required = false) String content,
            @ToolParam(description = "New theme ID", required = false) String themeId,
            @ToolParam(description = "Font size", required = false) Float fontSize,
            @ToolParam(description = "Line height", required = false) Float lineHeight,
            @ToolParam(description = "Section spacing (e.g. normal, compact, relaxed)", required = false) String sectionSpacing) {
        ResumeDTO dto = new ResumeDTO();
        dto.setTitle(title);
        dto.setContent(content);
        dto.setThemeId(themeId);
        dto.setFontSize(fontSize);
        dto.setLineHeight(lineHeight);
        dto.setSectionSpacing(sectionSpacing);
        return resumeService.updateDirect(id, dto);
    }

    @Tool(description = "Delete a resume permanently")
    public void deleteResume(
            @ToolParam(description = "Resume ID") String id) {
        resumeService.deleteDirect(id);
    }

    @Tool(description = "List all version snapshots for a resume")
    public List<ResumeVersion> listVersions(
            @ToolParam(description = "Resume ID") String resumeId) {
        return versionService.getVersions(resumeId);
    }

    @Tool(description = "Get a specific version snapshot")
    public ResumeVersion getVersion(
            @ToolParam(description = "Resume ID") String resumeId,
            @ToolParam(description = "Version number (1, 2, 3...)") int version) {
        return versionService.getVersion(resumeId, version);
    }

    @Tool(description = "Restore a previous version as the current resume content")
    public Resume restoreVersion(
            @ToolParam(description = "Resume ID") String resumeId,
            @ToolParam(description = "Version number to restore") int version) {
        Resume restored = versionService.restoreVersion(resumeId, version);
        return resumeService.restoreFromVersionDirect(restored);
    }

    @Tool(description = "Compare two versions and return a structured diff")
    public VersionDiffResponse diffVersions(
            @ToolParam(description = "Resume ID") String resumeId,
            @ToolParam(description = "First version number") int versionA,
            @ToolParam(description = "Second version number") int versionB) {
        return versionService.getDiff(resumeId, versionA, versionB);
    }
}
```

- [ ] **Step 2: Add restoreFromVersionDirect to ResumeService**

The existing `ResumeService.restoreFromVersion` requires a non-null userId and checks ownership. Add an overload that skips ownership check:

```java
// Add to ResumeService
@Transactional
public Resume restoreFromVersionDirect(Resume restored) {
    Resume resume = resumeRepository.findById(restored.getId())
            .orElseThrow(() -> new RuntimeException("Resume not found"));
    resume.setTitle(restored.getTitle());
    resume.setContent(restored.getContent());
    resume.setThemeId(restored.getThemeId());
    resume.setFontSize(restored.getFontSize());
    resume.setLineHeight(restored.getLineHeight());
    resume.setSectionSpacing(restored.getSectionSpacing());
    return resumeRepository.save(resume);
}
```

- [ ] **Step 3: Verify build compiles**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/resume/mcp/McpResumeTools.java backend/src/main/java/com/resume/service/ResumeService.java
git commit -m "feat: implement resume MCP tools"
```

---

### Task 5: Implement theme MCP tools

**Files:**
- Create: `backend/src/main/java/com/resume/mcp/McpThemeTools.java`

**Interfaces:**
- Consumes: `ThemeService.findAll()`, `ThemeService.findById(id)`, `ThemeService.updateDirect(id, dto)`
- Produces: MCP tool callable functions

- [ ] **Step 1: Write McpThemeTools class**

```java
package com.resume.mcp;

import com.resume.dto.ThemeDTO;
import com.resume.entity.Theme;
import com.resume.service.ThemeService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class McpThemeTools {

    private final ThemeService themeService;

    public McpThemeTools(ThemeService themeService) {
        this.themeService = themeService;
    }

    @Tool(description = "List all themes (built-in and custom)")
    public List<Theme> listThemes() {
        return themeService.findAll();
    }

    @Tool(description = "Get a theme by its ID with full details including CSS")
    public Theme getTheme(
            @ToolParam(description = "Theme ID (e.g. classic, modern, sidebar)") String id) {
        return themeService.findById(id)
                .orElseThrow(() -> new RuntimeException("Theme not found: " + id));
    }

    @Tool(description = "Update a theme (including built-in themes). Only provided fields are changed.")
    public Theme updateTheme(
            @ToolParam(description = "Theme ID") String id,
            @ToolParam(description = "New theme name", required = false) String name,
            @ToolParam(description = "New description", required = false) String description,
            @ToolParam(description = "CSS content", required = false) String cssContent,
            @ToolParam(description = "Layout type (single, sidebar-left, sidebar-right, header-bar)", required = false) String layout,
            @ToolParam(description = "Variables schema JSON", required = false) String variablesSchema) {
        ThemeDTO dto = new ThemeDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setCssContent(cssContent);
        dto.setLayout(layout);
        dto.setVariablesSchema(variablesSchema);
        return themeService.updateDirect(id, dto);
    }
}
```

- [ ] **Step 2: Verify build compiles**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/resume/mcp/McpThemeTools.java
git commit -m "feat: implement theme MCP tools"
```

---

### Task 6: Write tests

**Files:**
- Create: `backend/src/test/java/com/resume/mcp/McpResumeToolsTest.java`
- Create: `backend/src/test/java/com/resume/mcp/McpThemeToolsTest.java`

**Interfaces:**
- Consumes: `McpResumeTools`, `McpThemeTools`, `ResumeService`, `ThemeService`
- Produces: Verified tool behavior

- [ ] **Step 1: Write McpResumeToolsTest**

```java
package com.resume.mcp;

import com.resume.dto.ResumeDTO;
import com.resume.dto.VersionDiffResponse;
import com.resume.entity.Resume;
import com.resume.entity.ResumeVersion;
import com.resume.service.ResumeService;
import com.resume.service.ResumeVersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class McpResumeToolsTest {

    @Mock
    private ResumeService resumeService;
    @Mock
    private ResumeVersionService versionService;
    @Mock
    private McpAuthConfig authConfig;

    private McpResumeTools tools;

    @BeforeEach
    void setUp() {
        tools = new McpResumeTools(resumeService, versionService, authConfig);
    }

    @Test
    void listResumes_withUserId_shouldDelegateToFindByUserId() {
        when(resumeService.findByUserId(1L)).thenReturn(List.of(new Resume()));

        List<Resume> result = tools.listResumes(1L);

        assertThat(result).hasSize(1);
        verify(resumeService).findByUserId(1L);
        verifyNoMoreInteractions(resumeService);
    }

    @Test
    void listResumes_withoutUserId_shouldDelegateToFindAll() {
        when(resumeService.findAll()).thenReturn(List.of(new Resume(), new Resume()));

        List<Resume> result = tools.listResumes(null);

        assertThat(result).hasSize(2);
        verify(resumeService).findAll();
    }

    @Test
    void getResume_shouldReturnResume() {
        Resume resume = new Resume();
        resume.setId("abc-123");
        when(resumeService.findById("abc-123")).thenReturn(Optional.of(resume));

        Resume result = tools.getResume("abc-123");

        assertThat(result.getId()).isEqualTo("abc-123");
    }

    @Test
    void getResume_notFound_shouldThrow() {
        when(resumeService.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tools.getResume("missing"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void createResume_shouldDelegateToService() {
        Resume expected = new Resume();
        expected.setId("new-id");
        when(resumeService.create(any(ResumeDTO.class), eq(1L))).thenReturn(expected);

        Resume result = tools.createResume(1L, "My Resume", "# Content", "classic");

        assertThat(result.getId()).isEqualTo("new-id");
        verify(resumeService).create(any(ResumeDTO.class), eq(1L));
    }

    @Test
    void updateResume_shouldDelegateToUpdateDirect() {
        Resume expected = new Resume();
        expected.setId("abc");
        when(resumeService.updateDirect(eq("abc"), any(ResumeDTO.class))).thenReturn(expected);

        Resume result = tools.updateResume("abc", "New Title", null, null, null, null, null);

        assertThat(result.getId()).isEqualTo("abc");
        verify(resumeService).updateDirect(eq("abc"), any(ResumeDTO.class));
    }

    @Test
    void deleteResume_shouldDelegateToDeleteDirect() {
        tools.deleteResume("abc");

        verify(resumeService).deleteDirect("abc");
    }

    @Test
    void listVersions_shouldDelegateToService() {
        when(versionService.getVersions("r1")).thenReturn(List.of(new ResumeVersion()));

        List<ResumeVersion> result = tools.listVersions("r1");

        assertThat(result).hasSize(1);
    }

    @Test
    void getVersion_shouldDelegateToService() {
        ResumeVersion v = new ResumeVersion();
        v.setVersionNumber(3);
        when(versionService.getVersion("r1", 3)).thenReturn(v);

        ResumeVersion result = tools.getVersion("r1", 3);

        assertThat(result.getVersionNumber()).isEqualTo(3);
    }

    @Test
    void restoreVersion_shouldCallRestoreFromVersionDirect() {
        Resume restored = new Resume();
        restored.setId("r1");
        when(versionService.restoreVersion("r1", 2)).thenReturn(restored);
        when(resumeService.restoreFromVersionDirect(restored)).thenReturn(restored);

        Resume result = tools.restoreVersion("r1", 2);

        assertThat(result.getId()).isEqualTo("r1");
    }

    @Test
    void diffVersions_shouldDelegateToService() {
        VersionDiffResponse expected = new VersionDiffResponse(null, null, List.of());
        when(versionService.getDiff("r1", 1, 2)).thenReturn(expected);

        VersionDiffResponse result = tools.diffVersions("r1", 1, 2);

        assertThat(result).isSameAs(expected);
    }
}
```

- [ ] **Step 2: Write McpThemeToolsTest**

```java
package com.resume.mcp;

import com.resume.dto.ThemeDTO;
import com.resume.entity.Theme;
import com.resume.service.ThemeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class McpThemeToolsTest {

    @Mock
    private ThemeService themeService;

    private McpThemeTools tools;

    @BeforeEach
    void setUp() {
        tools = new McpThemeTools(themeService);
    }

    @Test
    void listThemes_shouldReturnAllThemes() {
        when(themeService.findAll()).thenReturn(List.of(new Theme(), new Theme()));

        List<Theme> result = tools.listThemes();

        assertThat(result).hasSize(2);
    }

    @Test
    void getTheme_shouldReturnTheme() {
        Theme theme = new Theme();
        theme.setId("modern");
        when(themeService.findById("modern")).thenReturn(Optional.of(theme));

        Theme result = tools.getTheme("modern");

        assertThat(result.getId()).isEqualTo("modern");
    }

    @Test
    void getTheme_notFound_shouldThrow() {
        when(themeService.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tools.getTheme("missing"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updateTheme_shouldDelegateToUpdateDirect() {
        Theme expected = new Theme();
        expected.setId("classic");
        when(themeService.updateDirect(eq("classic"), any(ThemeDTO.class))).thenReturn(expected);

        Theme result = tools.updateTheme("classic", "New Classic", null, "body {}", null, null);

        assertThat(result.getId()).isEqualTo("classic");
        verify(themeService).updateDirect(eq("classic"), any(ThemeDTO.class));
    }
}
```

- [ ] **Step 3: Run tests**

Run: `cd backend && mvn test -pl . -q`
Expected: All tests pass (the new tests + existing ones)

- [ ] **Step 4: Commit**

```bash
git add backend/src/test/java/com/resume/mcp/McpResumeToolsTest.java backend/src/test/java/com/resume/mcp/McpThemeToolsTest.java
git commit -m "test: add MCP tools unit tests"
```
