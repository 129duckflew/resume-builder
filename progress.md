# Resume Builder — 开发进度

## 架构总览

```
resume-builder/
├── backend/                          # Spring Boot 3.2 + Java 17
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/resume/
│       ├── ResumeApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   ├── JwtAuthFilter.java
│       │   ├── JwtUtil.java
│       │   ├── CorsConfig.java
│       │   └── PlaywrightConfig.java
│       ├── controller/
│       │   ├── AuthController.java               # /api/auth/login, /api/auth/register
│       │   ├── ResumeController.java             # CRUD + preview + export + JSON import + styles
│       │   ├── ThemeController.java
│       │   ├── SectionTemplateController.java
│       │   ├── ResumeVersionController.java      # GET/POST version snapshots
│       │   ├── ShareLinkController.java           # GET/POST share links
│       │   ├── AiController.java                 # POST /api/ai/suggest
│       │   ├── DesensitizeController.java        # GET/PUT desensitization rules
│       │   └── UserSettingsController.java       # AI API key management
│       ├── entity/
│       │   ├── Resume.java, Theme.java, User.java
│       │   ├── SectionTemplate.java, ResumeStyle.java
│       │   ├── ResumeVersion.java, ShareLink.java
│       │   └── DesensitizeRule.java
│       ├── repository/                           # 8 JpaRepository interfaces
│       ├── service/
│       │   ├── ResumeService.java, ThemeService.java
│       │   ├── MarkdownService.java, ExportService.java
│       │   ├── SmartOnePageService.java, PdfGenerationService.java
│       │   ├── SectionTemplateService.java, ResumeStyleService.java
│       │   ├── JsonResumeConverter.java
│       │   ├── ResumeVersionService.java, ShareLinkService.java
│       │   ├── AiService.java, DesensitizeService.java
│       │   └── UserService.java
│       └── dto/
│           ├── ResumeDTO.java, JsonResumeDTO.java
│           ├── ResumeStyleDTO.java, VariableDeclaration.java
├── frontend/                         # React 18 + Vite + TypeScript
│   ├── package.json, vite.config.ts, vitest.config.ts
│   ├── tailwind.config.ts, nginx.conf, Dockerfile
│   └── src/
│       ├── pages/
│       │   ├── HomePage.tsx, EditorPage.tsx, PreviewPage.tsx
│       │   ├── LoginPage.tsx, RegisterPage.tsx
│       ├── components/
│       │   ├── Layout.tsx
│       │   ├── ui/                                # shadcn/ui: button, dialog, toast, dropdown-menu, confirm-dialog
│       │   └── editor/
│       │       ├── SectionDragList.tsx, SortableSection.tsx
│       │       ├── SectionTemplatePicker.tsx, ThemeSelector.tsx
│       │       ├── ThemeCustomizer.tsx
│       │       ├── ExportPanel.tsx, VersionPanel.tsx
│       │       ├── SharePanel.tsx, AiAssistant.tsx
│       │       ├── DesensitizeSettings.tsx
│       ├── stores/                                # authStore, resumeStore, historyStore
│       ├── types/                                 # resume.ts, sectionTemplate.ts, desensitize.ts
│       ├── hooks/                                 # useKeyboardShortcuts, useDraftBackup, use-toast
│       └── lib/                                   # api.ts, markdown.ts, utils.ts
│   └── e2e/                                       # Playwright E2E tests (Docker)
│       ├── Dockerfile, playwright.config.ts
│       └── specs/core-flow.spec.ts
├── themes/                             # 7 内置 CSS 主题
├── .opencode/                          # AI agent definition files
├── docker-compose.yml                  # PostgreSQL 16 + Backend(:8081) + Frontend(:3000)
├── AGENTS.md, ROADMAP.md, progress.md
```

## 测试基线

| 模块 | 通过/总计 |
|------|----------|
| Backend | 144/144 |
| Frontend | 84/84 |
| **合计** | **228/228** ✅ |
| E2E (Docker) | core-flow 3 specs ✅ |

## 已完成目标

| # | 目标 | 提交 | 核心文件 |
|---|------|------|---------|
| 1 | 自定义章节模板 | `7bc7736` | SectionTemplate entity/service/controller, SectionTemplatePicker |
| 2 | JSON Resume 导入导出 | `017aec1` | JsonResumeConverter, JsonResumeDTO, import/export API + UI |
| 3 | 切换主题保留样式微调 | `5e888df` | ResumeStyle entity/repo/service, ThemeSelector, resumeStore |
| 4 | 服务端版本历史 | `5aae6d6` | ResumeVersion entity/repo/service/controller, VersionPanel |
| 5 | 可分享简历链接（含脱敏） | `fdeadc0` | ShareLink entity/repo/service/controller, SharePanel |
| 6 | E2E 测试 (Playwright) | `0eacbb5` | frontend/e2e/, core-flow.spec.ts |
| 7 | AI 助手（用户管理 API Key） | `c0ed1c7` | AiController/Service, AiAssistant, UserSettingsController |
| 8 | 高级样式删除确认弹窗替代原生 confirm() | `bea7e30` | confirm-dialog.tsx, HomePage.tsx, tailwind.config.ts |
| 9 | CI GitHub Actions (G15) | `0313262` | .github/workflows/ci.yml (backend + frontend + Docker E2E) |
| 10 | README 文档同步 (G16) | `0238e5c` | README.md (16 features, 28 endpoints, 189 tests) |
| 11 | 主题变量化与样式配置扩展 (G19-A) | `c66c9d5` | Theme/ResumeStyle 变量字段, ExportService :root 注入, ThemeCustomizer, 7 主题 var() 化 |

## 进行中

- G19-B 布局模板与渲染重构 — 修复 sidebar 主题失效 + 支持多布局（下一候选）
- G19-C 主题分类预览与自定义主题
- G9 ATS 关键词评分 — 求职闭环第一层，高价值
- 详见 ROADMAP.md 三层规划

## 启动方式

```bash
# 开发模式
docker compose up -d postgres
cd backend && mvn spring-boot:run                    # :8081
cd frontend && npm install && npm run dev            # :3000

# 生产模式
docker compose up --build

# 测试
cd backend && mvn test                               # 144 用例
cd frontend && npm test                              # 84 用例
```
