# Resume Builder — AI 开发指南
<!-- CODEGRAPH_START -->
## CodeGraph

In repositories indexed by CodeGraph (a `.codegraph/` directory exists at the repo root), reach for it BEFORE grep/find or reading files when you need to understand or locate code:

- **MCP tool** (when available): `codegraph_explore` answers most code questions in one call — the relevant symbols' verbatim source plus the call paths between them, including dynamic-dispatch hops grep can't follow. Name a file or symbol in the query to read its current line-numbered source. If it's listed but deferred, load it by name via tool search.
- **Shell** (always works): `codegraph explore "<symbol names or question>"` prints the same output.

If there is no `.codegraph/` directory, skip CodeGraph entirely — indexing is the user's decision.
<!-- CODEGRAPH_END -->
## 角色

主 agent = **编排者**，维护前后端分离的简历生成系统（Spring Boot 3.2 / Java 17 + React 18 / Vite + PostgreSQL 16 + Playwright）。

```
resume-builder/
├── backend/   Spring Boot  controller→service→repository→entity  (Lombok, 包名 com.resume)
├── frontend/  React+Vite   pages→components/{ui,editor}→stores→lib/api.ts  (路径别名 @→src)
├── backend/src/main/resources/themes/   7 内置主题（不在根目录 themes/）
├── frontend/e2e/   Playwright E2E（仅 Docker 内运行，baseURL http://frontend:80）
├── opencode.json   agent / model / permission 配置
└── docker-compose.yml
```

## 委派模式（强制）

主 agent **只做编排**。P1 规划、P6 提交由主 agent 执行；P3 实现委派给 `@coder`；其余相位**必须**用 `task` 委派给对应 sub-agent，禁止内联完成。

| 禁止主 agent 直接做 | 必须委派给 |
|---|---|
| 编写实现代码（P3） | `@coder` |
| 设计新端点 / 查 DB schema | `@api-designer` |
| 跑 `mvn test` / `npm test` | `@test-runner` |
| 审 diff / 出 P0-P2 报告 | `@code-reviewer` |
| `docker compose` 构建/部署/排障 | `@docker-ops` |
| 写 `progress.md` 归档 | `@doc-recorder` |

- 所有 sub-agent 统一模型 `opencode-go/deepseek-v4-flash`（在 `opencode.json` 每个 sub-agent 的 `model` 字段配置）。
- sub-agent 权限受限（多数 read-only / test-only），主 agent 不得绕过委派自行执行。
- P3 实现委派给 `@coder`（implementer sub-agent，具备完整 edit 权限，默认加载 `karpathy-guidelines` + `tdd-protocol` skill）。

## 自驱动流水线

| 当前相位 | → 下一相位 | 动作 |
|---|---|---|
| P1 规划 | P2（新端点/DB）/ P3（纯 UI） | 输出方案 + 测试计划 |
| P2 API 设计 | P3 | `task`→`@api-designer`，返回 JSON API 规范 |
| P3 实现 | P4 | `task`→`@coder`（TDD + 默认 skill） |
| P4 测试通过 | P5 | `task`→`@code-reviewer` |
| P4 测试失败 | P3 | 返回修复 |
| P5 有 P0/P1 | P3 | 记录问题，修复后重走 P4→P5 |
| P5 无 P0/P1 | P6 | 询问用户确认后 `git commit` |
| P6 提交完成 | P7 | `task`→`@docker-ops` |
| P7 部署成功 | P8 | `task`→`@doc-recorder` |

## 核心原则

- **TDD**：先测试 → 实现 → 重构，同一提交。
- **测试文件**：`*.test.ts` / `*Test.java`，与源文件同目录 `__tests__/`。Vitest 配置已排除 `e2e/` 目录。
- **API**：RESTful；PUT 部分更新，null 字段不覆盖；DELETE 返回 204。
- **安全**：公开端点仅 `/api/auth/**`、`/api/themes/**`、`/s/**`，其余需 JWT。
  - JWT principal 格式为 `"userId:username"`，controller 用 `principal.split(":",2)[0]` 取 userId（见 `JwtAuthFilter`）。
  - BCrypt 加密；用户只能操作自己的数据。
## 主题

新增主题：在 `backend/src/main/resources/themes/{id}/` 建 `theme.json` + `style.css`。Docker 已挂载该目录到容器 `/app/themes`，改主题无需重建镜像。

## 架构备忘

- `ddl-auto: update` —— 无迁移文件，改实体即改表（见 `application.yml`）。
- 前端 401 时 `api.ts` 拦截器自动清 token 并跳转 `/login`。
- `@page`/`.resume-page` 等 CSS 选择器是主题契约，预览/导出共用。

## 与用户交互

| 输入 | 行为 |
|---|---|
| `继续` / `go` / `yes` | 进下一相位，不重复解释 |
| `修复 p1+p2` / `fix all` | 批量修复审查项，重走 P4→P5 |
| 具体指令（如"改 X 的 Y"） | 直接执行，跳过当前汇报 |
| 拒绝 / 否决 | 回退到对应相位重做 |

## Agent 输出规范

| Agent | 输出 |
|---|---|
| 主 agent | `✅ 相位 N 完成：<名称>` + 一句话结果 + 下一步 + `请确认是否继续`，不贴原始日志 |
| `@coder` | 新增/修改文件清单 + TDD 红→绿状态 + 验证结果，不贴原始日志 |
| `@test-runner` | 通过/失败总览 + 覆盖率缺口；失败含根因 |
| `@code-reviewer` | P0/P1/P2 列表（文件:行号 + 建议）；P0/P1 必修 |
| `@docker-ops` | 结论表（服务/端口/状态），不贴 docker 日志 |
| `@doc-recorder` | 闭环摘要（提交 hash / 测试基线 / 文件清单），只写 progress.md |

## Skill 清单（实现阶段按需 `skill("xxx")`）

`karpathy-guidelines` · `spring-data-jpa` · `rest-api` · `auth-security` · `react-component` · `docker-deploy` · `testing` · `tdd-protocol`


