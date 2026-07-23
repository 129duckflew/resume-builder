# Resume Builder — 开发进度

## 架构总览

```
resume-builder/
├── backend/                          # Spring Boot 4.1 + Java 25
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
│   │   ├── ThemeController.java            # CRUD + GET (自定义主题 owner 隔离)
│       │   ├── SectionTemplateController.java
│       │   ├── ResumeVersionController.java      # GET/POST version snapshots + GET diff
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
│   │   ├── ResumeService.java, ThemeService.java
│   │   ├── MarkdownService.java, ExportService.java, LayoutSplitter.java
│   │   ├── SmartOnePageService.java, PdfGenerationService.java
│   │   ├── SectionTemplateService.java, ResumeStyleService.java
│   │   ├── JsonResumeConverter.java
│   │   ├── ResumeVersionService.java, ShareLinkService.java
│   │   ├── AiService.java, DesensitizeService.java, CssSanitizer.java
│   │   └── UserService.java
│       └── dto/
│           ├── ResumeDTO.java, JsonResumeDTO.java
│           ├── ResumeStyleDTO.java, VariableDeclaration.java, ThemeDTO.java
│           ├── DiffLine.java, Hunk.java, LineType.java, VersionDiffResponse.java, VersionMeta.java
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
│   │       ├── SectionDragList.tsx, SortableSection.tsx
│       │       ├── SectionTemplatePicker.tsx, ThemeSelector.tsx  # 按 layout 分组+自定义主题
│       │       ├── ThemeCustomizer.tsx
│   │       ├── ExportPanel.tsx, VersionPanel.tsx, VersionDiff.tsx
│       │       ├── SharePanel.tsx, AiAssistant.tsx
│       │       ├── DesensitizeSettings.tsx
│       ├── stores/                                # authStore, resumeStore, historyStore
│       ├── types/                                 # resume.ts, sectionTemplate.ts, desensitize.ts
│       ├── hooks/                                 # useKeyboardShortcuts, useDraftBackup, use-toast
│       └── lib/                                   # api.ts, markdown.ts, utils.ts
│   └── e2e/                                       # Playwright E2E tests (Docker)
│       ├── Dockerfile, playwright.config.ts
│       └── specs/core-flow.spec.ts
├── db/migration/                        # Flyway 迁移: V1 建表 + V2 播种 13 内置主题到 DB
├── .opencode/                          # AI agent definition files
├── docker-compose.yml                  # PostgreSQL 16 + Backend(:8081) + Frontend(:3000)
├── AGENTS.md, ROADMAP.md, progress.md
```

## 测试基线

| 模块 | 通过/总计 |
|------|----------|
| Backend | 222/222 |
| Frontend | 117/117 |
| **合计** | **339/339** ✅ |
| E2E (Docker) | core-flow 2/3 ✅（register 用例因 RegisterPage input 缺 name 属性失败，遗留待修） |

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
| 11 | 主题变量化与样式配置扩展 (G19-A) | `c66c9d5` | Theme/ResumeStyle 变量字段, ExportService :root 注入, ThemeCustomizer, 13 主题 var() 化 |
| 12 | 布局模板与自定义主题 (G19-B+C) | `206c1d5` + `4141d85` | LayoutSplitter, ExportService 重构, 2 新主题, Theme CRUD, CssSanitizer, ThemeSelector 分组 |
| 13 | 集成 4 新内置简历主题 (Jake's, Academic, Swiss, Harvard) | `4b08e61` | themes/{jake,academic,swiss,harvard}/{theme.json,style.css}, ThemeService.java, ThemeCssCompletenessTest, ThemeServiceTest, ThemeServiceVariablesTest |
| 14 | G17 版本 diff 对比 | `cd3c867` | DiffLine.java, Hunk.java, LineType.java, VersionDiffResponse.java, VersionMeta.java, ResumeVersionController.java, ResumeVersionService.java, VersionDiff.tsx, VersionPanel.tsx, api.ts, resume.ts |
| 15 | bugfix: sidebar/sidebar-right 无章节时样式丢失 | `daba10e` | ExportService.java, sidebar/style.css, sidebar-right/style.css |
| 16 | KEDA scale-from-zero 按需启动 | - | k8s/08-scaling/, k8s/06-ingress/ingress.yaml, scripts/k8s-{install-keda,apply,smoke-test}.sh, README.md |

## 进行中

- G9 ATS 关键词评分 — 求职闭环第一层，高价值
- 🔴 遗留：RegisterPage.tsx `<input>` 缺 `name` 属性 → e2e register 用例失败，需独立修复
- 详见 ROADMAP.md

## 启动方式

```bash
# 开发模式
brew services start postgresql@16
cd backend && mvn spring-boot:run                    # :8081
cd frontend && npm install && npm run dev            # :3000

# 生产模式
docker compose up --build

# 测试
cd backend && mvn test                               # 222 用例
cd frontend && npm test                              # 117 用例
```

## 迭代记录

### 16 — 2026-07-10：401 超时提示与自动跳转修复
| 目标 | 提交 | 核心文件 |
|------|------|---------|
| 401 响应拦截器增强：toast 提示 + 清 token + 跳转 /login | `2a5e594` | `api.ts`, `App.tsx`, `Layout.tsx`, `api.test.ts` |

**测试基线**：前端 105/105（+4），TypeScript 零错误
**E2E**：Docker 内 2 passed
**待跟进**：RegisterPage `<input>` 缺 `name` 属性仍导致 e2e register 失败，G9 ATS 关键词评分待启动

### 17 — 2026-07-10：清理 backend IDE 项目文件（.gitignore 修复）
| 目标 | 提交 | 核心文件 |
|------|------|---------|
| 将误提交的 backend Eclipse/IDE 项目文件（`.classpath`, `.factorypath`, `.project`, `.settings/`）从 git 索引移除，并新增 `.gitignore` 规则防止再次误提交 | `fbfc11c` | `.gitignore` |

**背景**：上一个提交 `2a5e594` 误将 backend IDE 项目文件纳入版本控制，本次修复将其移除并保留本地 IDE 文件。
**测试基线**：无变动（纯配置修复）
**E2E**：无影响

### 18 — 2026-07-15：KEDA scale-from-zero 按需启动
| 目标 | 提交 | 核心文件 |
|------|------|---------|
| 拆分 IngressRoute 到对应命名空间，修复 Traefik 跨命名空间服务引用 | - | `k8s/06-ingress/ingress.yaml`, `scripts/k8s-apply.sh` |
| Deployment replicas 改为 0，删除手动 HPA | - | `k8s/04-backend/backend-deployment.yaml`, `k8s/05-frontend/frontend-deployment.yaml`, `k8s/04-backend/backend-hpa.yaml`, `k8s/05-frontend/frontend-hpa.yaml` |
| 新增 KEDA 安装脚本，修复 ScaledJob CRD | - | `scripts/k8s-install-keda.sh` |
| smoke-test 适配 scale-from-zero | - | `scripts/k8s-smoke-test.sh` |
| README 与 design doc 同步 | - | `README.md`, `docs/superpowers/specs/2026-07-15-keda-scale-from-zero-fix-design.md` |

**测试基线**：Backend 222/222，Frontend 117/117，K8s smoke-test 通过
**冷启动实测**：前端 ~7.6s，后端 ~17.8s；热请求 < 100ms
**待跟进**：G9 ATS 关键词评分，RegisterPage `<input>` 缺 `name` 属性
