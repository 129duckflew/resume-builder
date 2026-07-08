# Resume Builder — AI 开发指南

## 角色

全栈工程师，维护前后端分离的简历生成系统（Spring Boot 3.2 + React 18 + PostgreSQL 16 + Playwright）。

```
resume-builder/
├── AGENTS.md, opencode.json, docker-compose.yml, themes/
├── backend/   (controller/ → service/ → repository/ → entity/)
└── frontend/  (pages/ → components/{ui,editor}/ → stores/ → lib/api.ts)
```

## 核心原则

| 原则 | 规则 |
|---|---|
| **TDD** | 先写测试 → 实现 → 重构 → 同一提交 |
| **前后端** | 前端 `/api/` → nginx → 后端；各层单向依赖 |
| **测试** | 每个功能必须有测试；文件 `*.test.ts` / `*Test.java`；与源文件同目录 `__tests__/` |
| **API** | RESTful：GET(列表) / GET(id) / POST(创建) / PUT(部分更新) / DELETE(204)；null 字段不覆盖 |
| **安全** | 注册/登录 + 主题列表 = 公开；其余需 JWT；用户只能操作自己的数据；BCrypt 加密 |

---

## 开发流水线

收到需求后，按以下相位自动推进。**每完成一个相位，自动判断下一个相位**，形成自驱动循环。

```
                   ┌─────────────────────────┐
                   │   P1  规 划              │
                   │  输出: 方案(影响面/文件/  │
                   │        测试计划)          │
                   └────────────┬────────────┘
                                │
                   ┌────────────▼────────────┐
                   │   P2  API 设 计          │ ← 仅限新端点/DB变更
                   │  派发: @api-designer     │
                   │  输出: JSON API 规范      │
                   └────────────┬────────────┘
                                │
                   ┌────────────▼────────────┐
                   │   P3  实 现              │
                   │  动作: TDD + skill(...)  │
                   └────────────┬────────────┘
                                │
              ┌──────────────────┼──────────────────┐
              ▼                  ▼                  ▼
     ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
     │  P4 测试验证   │  │  P5 代码审查  │  │  修复循环     │
     │ @test-runner  │  │ @code-reviewer│  │ P0/P1 项     │
     │ 输出: 通过/   │  │ 输出:         │  │ → 回到 P3    │
     │     失败 +    │  │   P0/P1/P2   │  │              │
     │     覆盖率    │  │   报告       │  │              │
     └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
            │                 │                  │
            │    失败          │ 有 P0/P1         │
            └──────→──────────┴──────→───────────┘
                                    │ 全部通过
                                    ▼
                          ┌─────────────────────┐
                          │  P6  提 交           │ ← 询问用户后执行
                          │  动作: git add +     │
                          │   commit             │
                          └──────────┬──────────┘
                                     │
                          ┌──────────▼──────────┐
                          │  P7  部 署           │
                          │  @docker-ops        │
                          │  docker compose up   │
                          └──────────┬──────────┘
                                     │
                          ┌──────────▼──────────┐
                          │  P8  归 档           │
                          │  @doc-recorder       │
                          │  → progress.md      │
                          └─────────────────────┘
```

### 自驱动循环规则

| 当前相位完成 | → 自动派发 | 条件 |
|---|---|---|
| P1 规划 | P2 或 P3 | 涉及新端点/DB → P2；纯 UI 改动 → P3 |
| P2 API 设计 | P3 实现 | — |
| P3 实现 | P4 测试验证 | `task` → `@test-runner` |
| P4 测试通过 | P5 代码审查 | `task` → `@code-reviewer` |
| P4 测试失败 | P3 实现 | 返回修复 |
| P5 有 P0/P1 | P3 实现 | 记录问题后返回，修复后重走 P4 → P5 |
| P5 无 P0/P1 | P6 提交 | 询问用户确认后执行 |
| P6 提交完成 | P7 部署 | `task` → `@docker-ops` |
| P7 部署成功 | P8 归档 | `task` → `@doc-recorder` |

### 与用户交互

| 用户输入 | 行为 |
|---|---|
| `继续` / `go` / `yes` | 进入下一相位，无需重复解释 |
| `修复 p1+p2` / `fix all` | 批量修复审查报告中的所有 P0/P1/P2，然后自动重走 P4→P5 |
| 具体指令（如"修改 X 文件中的 Y"） | 直接执行，跳过当前相位的汇报 |
| 拒绝/否决 | 回退到对应相位重做 |

### Agent 输出规范

| Agent | 输出格式 | 要求 |
|---|---|---|
| 主 agent（汇报） | `✅ 相位 N 完成：<名称>` + 一句话结果 + 下一步 + `请确认是否继续` | 不贴原始日志 |
| `@test-runner` | 总览（通过/失败）+ 覆盖率缺口 | 失败时含根因分析 |
| `@code-reviewer` | P0/P1/P2 列表（每项含文件:行号 + 建议） | P0/P1 必须修复 |
| `@docker-ops` | 结论表格（服务名 / 端口 / 状态） | 不贴原始 docker 日志 |
| `@doc-recorder` | 闭环摘要（提交 hash / 测试基线 / 文件清单） | 只写 progress.md，不回贴全文 |

---

## Agent 清单

在对话中用 `@` 提及（或由主 agent 按自驱动规则自动派发）：

| Agent | 触发时机 | 权限 | 职责 |
|---|---|---|---|
| `@api-designer` | P2：涉及新端点/DB 变更 | read-only | 查 Schema，返回 JSON API 规范 |
| `@test-runner` | P4：实现完成 | test-only | 跑 `mvn test` + `npm test`，返回通过/失败 |
| `@code-reviewer` | P5：测试通过 | read-only | 审 diff，返回 P0/P1/P2 报告 |
| `@docker-ops` | P7：提交完成 | bash | 构建/部署/排障，返回结论表格 |
| `@doc-recorder` | P8：部署成功 | edit+bash | 更新 progress.md，定义下一 Goal |

## Skill 清单

实现阶段按需加载（`skill("xxx")`）：

- `spring-data-jpa` — JPA 实体设计
- `rest-api` — REST API 设计
- `auth-security` — Spring Security + JWT
- `react-component` — React 组件编写
- `docker-deploy` — Docker 部署
- `testing` — 测试规范
- `tdd-protocol` — TDD 完整协议
- `doc-recording` — 进度归档规范
