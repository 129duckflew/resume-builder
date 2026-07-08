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

## Subagent 委派

在对话中用 `@` 提及以下子代理：

| Agent | 职责 | 隔离内容 |
|---|---|---|
| `@api-designer` | 查询 DB Schema、设计 API、返回结构化规范 | Schema 查询结果不污染主线程 |
| `@docker-ops` | Docker 构建/部署/日志 | 长日志不进主线程，只返结论 |
| `@code-reviewer` | 安全审查、并发分析、资源泄漏检查 | 检查报告不中断主线开发 |
| `@test-runner` | 批量测试、失败分析、覆盖率缺口 | 测试日志蒸馏为通过/失败清单 |

---

## Skills 按需加载

相关任务触发时，加载对应技能模块：

- `@.opencode/skills/spring-data-jpa.md` — JPA 实体设计 Checklist
- `@.opencode/skills/rest-api.md` — REST API 设计 Checklist
- `@.opencode/skills/auth-security.md` — Spring Security + JWT Checklist
- `@.opencode/skills/react-component.md` — React 组件编写 Checklist
- `@.opencode/skills/docker-deploy.md` — Docker 部署 Checklist
- `@.opencode/skills/testing.md` — 测试规范 Checklist

---

## 禁止事项

- 禁止修改 `progress.md`
- 禁止直接操作数据库（使用 JPA Repository）
- 禁止跳过测试直接提交
- 禁止硬编码密钥（使用 `application.yml` + 环境变量）
- 禁止在 Controller 中写业务逻辑
