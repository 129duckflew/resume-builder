# Resume Builder — AI 开发指南

## 你的角色

你是一个全栈工程师，负责维护一个前后端分离的简历生成系统。技术栈：Spring Boot 3.2 + React 18 + PostgreSQL 16 + Playwright。

---

## 项目全景

```
resume-builder/
├── AGENTS.md              # 本文件 — AI 主指令
├── opencode.json          # Harness 配置
├── .opencode/
│   ├── agents/            # Subagent 定义
│   └── skills/            # Checklist 技能模块
├── backend/               # Spring Boot 3.2 (Java 17, Maven)
│   ├── controller/        # @RestController 层
│   ├── service/           # @Service 业务逻辑
│   ├── repository/        # JpaRepository 数据访问
│   ├── entity/            # JPA 实体
│   ├── config/            # Security + JWT + CORS + Playwright
│   └── dto/               # 请求/响应 DTO
├── frontend/              # React 18 (Vite, TypeScript)
│   ├── pages/             # LoginPage | RegisterPage | HomePage | EditorPage | PreviewPage
│   ├── components/
│   │   ├── ui/            # shadcn/ui 组件（button/dialog/toast/dropdown-menu）
│   │   └── editor/        # 编辑器专属组件
│   ├── stores/            # authStore (JWT) + resumeStore + historyStore
│   ├── hooks/             # useKeyboardShortcuts / useDraftBackup / use-toast
│   └── lib/               # api.ts (axios) + markdown.ts (章节解析)
├── docker-compose.yml     # PostgreSQL 16 + Backend(:8081) + Frontend(:3000)
└── themes/                # 7 个内置 CSS 主题
```

---

## 构建与测试

```bash
# 后端
cd backend && mvn spring-boot:run               # 启动 :8080
cd backend && mvn test                           # 62 用例

# 前端
cd frontend && npm install && npm run dev        # 启动 :3000
cd frontend && npm test                          # 44 用例

# Docker
docker compose up --build -d                     # 全部容器
docker compose logs backend                      # 后端日志
docker compose logs frontend                     # 前端日志

# 合计 106 测试，全部通过
```

---

## 编码规范

- **前后端分离**：前端通过 `/api/` → nginx 代理 → 后端
- **后端分层**：`@RestController` → `@Service` → `JpaRepository`
- **前端分层**：`Page` → `Component` → `Store(zustand)` → `API(axios)`
- **API 路径**：`/api/resumes`, `/api/auth`, `/api/themes`
- **REST 风格**：GET(列表) / GET(id) / POST(创建) / PUT(部分更新) / DELETE(204)
- **部分更新**：PUT 请求中 null 字段不覆盖已有值
- **每个新功能必须有对应测试**
- **测试文件命名**：`{ClassName}Test.java` / `{module}.test.ts`
- **测试方法命名**：`方法_场景_预期结果`（Java）/ `it('does x when y')`（TS）

---

## 安全规则

- `POST /api/auth/register` + `/api/auth/login` — 公开
- `GET /api/themes/**` — 公开
- `GET/POST/PUT/DELETE /api/resumes/**` — 需要 JWT（`Authorization: Bearer <token>`）
- 用户只能操作自己的简历（`Resume.userId` 过滤）
- 密码使用 `BCryptPasswordEncoder`
- Token 载荷：`userId:username` 复合字符串

---

## TDD 流程

1. **Red** — 先写失败测试
2. **Green** — 实现功能使测试通过
3. **Refactor** — 重构优化代码
4. **Commit** — 实现 + 测试在同一提交

---

## 开发工作流（主 agent 编排闭环）

收到功能需求后，按以下关卡顺序推进。**带 🚧 的关卡完成后必须停下汇报、等用户确认再进下一关**；未带 🚧 的可自动连续执行。

| # | 关卡 | 动作 | 派发 / 加载 | 类型 |
|---|---|---|---|---|---|
| 1 | 规划 | 出方案（影响面、文件清单、测试计划） | `plan` 或 build 内规划 | 🚧 |
| 2 | API 设计 | 涉及后端新端点 / DB 变更时先拿规范 | `task` → `@api-designer` | 自动 |
| 3 | 实现 | TDD：Red → Green → Refactor | 按需 `skill(...)` | 自动 |
| 4 | 测试 | 跑后端 `mvn test` + 前端 `npm test` | `task` → `@test-runner` | 🚧 |
| 5 | 审查 | 审 diff（安全 / 并发 / 资源 / 覆盖） | `task` → `@code-reviewer` | 🚧 |
| 6 | 提交 | 实现 + 测试同提交 | 询问用户后 commit | 🚧 |
| 7 | 部署 | `docker compose up --build -d` backend + frontend | `task` → `@docker-ops` | 自动 |

### 派发规则（满足即派，不要自己硬扛）

- 涉及 DB Schema / 新 REST 端点 → 先 `task` 派 `@api-designer`，拿到 JSON 规范再写后端
- 任何代码实现完成 → `task` 派 `@test-runner`；未达当前测试基线禁止进审查
- 测试通过、提交前 → `task` 派 `@code-reviewer`；P0/P1 必须先修复并复测
- **提交完成后 → 自动 `task` 派 `@docker-ops` 执行 `docker compose up --build -d` 部署 backend + frontend，取蒸馏结论汇报**
- 需要 docker / 端口 / 日志（非部署场景）→ `task` 派 `@docker-ops`；原始日志留在子线程，只取蒸馏结论
- 写 JPA / REST / Security / React / Docker / 测试 → 调用 `skill` 工具加载对应 checklist

### 🚧 关卡汇报格式

```
✅ 关卡 N 完成：<关卡名>
结果：<一句话结论>
下一步：<下一关卡将做的事>
请确认是否继续？
```

### 闭环纪律

- 子 agent 返回的是结论，不是原始日志，不要回贴主线程
- 一轮需求走完 1 → 7 才算闭环；任何 🚧 被否决则回对应关卡重做
- 禁止跳过测试或审查直接提交

---

## Subagent 委派

在对话中用 `@` 提及以下子代理（调度规则见上方「开发工作流」一节）：

| Agent | 职责 | 隔离内容 |
|---|---|---|
| `@api-designer` | 查询 DB Schema、设计 API、返回结构化规范 | Schema 查询结果不污染主线程 |
| `@docker-ops` | Docker 构建/部署/日志 | 长日志不进主线程，只返结论 |
| `@code-reviewer` | 安全审查、并发分析、资源泄漏检查 | 检查报告不中断主线开发 |
| `@test-runner` | 批量测试、失败分析、覆盖率缺口 | 测试日志蒸馏为通过/失败清单 |

---

## Skills 按需加载

相关任务触发时，加载对应技能模块（已注册为 opencode 原生 skill，由主 agent 的 `skill` 工具按 description 自动匹配；调度规则见上方「开发工作流」一节）：

- `@skill spring-data-jpa` — JPA 实体设计 Checklist
- `@skill rest-api` — REST API 设计 Checklist
- `@skill auth-security` — Spring Security + JWT Checklist
- `@skill react-component` — React 组件编写 Checklist
- `@skill docker-deploy` — Docker 部署 Checklist
- `@skill testing` — 测试规范 Checklist

---

## 禁止事项

- 禁止修改 `progress.md`
- 禁止直接操作数据库（使用 JPA Repository）
- 禁止跳过测试直接提交
- 禁止硬编码密钥（使用 `application.yml` + 环境变量）
- 禁止在 Controller 中写业务逻辑
