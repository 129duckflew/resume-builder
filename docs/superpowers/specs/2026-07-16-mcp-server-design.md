# MCP Server for Resume Builder

## Overview

Build a Model Context Protocol (MCP) server as a Spring Boot module within the existing backend, enabling AI assistants to view, modify, and manage resumes and themes through standardized MCP tools. The server supports dual transport (SSE for remote clients, STDIO for local clients) and authenticates via a configurable super admin API key that bypasses per-user ownership checks.

## Transport

- **SSE (Streamable-HTTP)**: Exposed at `/mcp/sse` with message endpoint `/mcp/message` for remote AI clients (e.g., OpenCode connecting over HTTP).
- **STDIO**: Simultaneously enabled via `spring.ai.mcp.server.stdio=true` for local AI clients (e.g., OpenCode launching the server as a child process).
- **Library**: `spring-ai-starter-mcp-server-webmvc` (Spring AI 1.1.x, compatible with Spring Boot 3.2.5).

## Authentication

- **SSE mode**: A `TransportContextExtractor` extracts the `Authorization` header from incoming HTTP requests. The token is validated against `app.mcp.api-key` configured in `application.yml` / env var `MCP_API_KEY`.
- **STDIO mode**: The key is read from the `MCP_API_KEY` environment variable.
- **Super admin bypass**: When authenticated, all tools skip `userId` ownership checks. Resume tools expose an optional `userId` parameter to scope the query.

## Tools

| Tool | Parameters | Description |
|------|-----------|-------------|
| `list_resumes` | `userId?` (optional filter) | List all resumes in the system |
| `get_resume` | `id` | Get full resume content and metadata |
| `create_resume` | `userId, title?, content?, themeId?` | Create a new resume for a user |
| `update_resume` | `id, title?, content?, themeId?, fontSize?, lineHeight?, sectionSpacing?` | Update resume fields (partial) |
| `delete_resume` | `id` | Delete a resume |
| `list_versions` | `resumeId` | List version history for a resume |
| `get_version` | `resumeId, version` | Get a specific snapshot version |
| `restore_version` | `resumeId, version` | Restore a previous version |
| `diff_versions` | `resumeId, versionA, versionB` | Compute structured diff between two versions |
| `list_themes` | — | List all themes (built-in + custom) |
| `get_theme` | `id` | Get theme metadata, CSS, variables, and layout |
| `update_theme` | `id, name?, description?, cssContent?, layout?, variablesSchema?` | Update any theme including built-in (super admin only) |

## Architecture

```
backend/src/main/java/com/resume/mcp/
├── McpResumeTools.java      # Resume CRUD + version tools (@McpTool)
├── McpThemeTools.java       # Theme list/get/update tools (@McpTool)
├── McpAuthConfig.java       # API key config, TransportContextExtractor, auth helper
```

- Tools are Spring `@Component` beans with `@McpTool` annotated methods.
- Existing `ResumeService`, `ResumeVersionService`, `ThemeService` are injected via constructor DI.
- `McpAuthConfig` provides a `TransportContextExtractor` for SSE auth and a utility method to validate the API key from `McpTransportContext` (SSE) or environment (STDIO).
- Tool methods receive `McpSyncServerExchange` as a special parameter for access to transport context and logging.

## Dependencies

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.ai</groupId>
      <artifactId>spring-ai-bom</artifactId>
      <version>1.1.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

## Configuration

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

## User-Facing MCP Client Configuration

For OpenCode or any MCP client, the configuration would be:

**SSE (remote):**
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

**STDIO (local):**
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

## Testing

- Unit tests for each tool method using mocked services.
- Integration test verifying SSE endpoint connectivity and tool registration.
- Auth test for valid/invalid API key scenarios.
