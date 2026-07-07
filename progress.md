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
│       │   ├── CorsConfig.java
│       │   └── PlaywrightConfig.java
│       ├── controller/
│       │   ├── ResumeController.java    # CRUD + preview + export
│       │   └── ThemeController.java     # theme list + CSS
│       ├── entity/
│       │   ├── Resume.java
│       │   └── Theme.java
│       ├── repository/
│       ├── service/
│       │   ├── ResumeService.java
│       │   ├── MarkdownService.java     # commonmark: md → HTML
│       │   ├── ExportService.java       # HTML generation
│       │   ├── SmartOnePageService.java # Playwright A4 auto-fit
│       │   ├── PdfGenerationService.java# Playwright PDF generation
│       │   └── ThemeService.java        # 启动时加载 7 个内置主题
│       └── dto/
├── frontend/                         # React 18 + Vite + TypeScript
│   ├── package.json
│   ├── vite.config.ts
│   ├── vitest.config.ts
│   ├── tailwind.config.ts
│   ├── nginx.conf
│   ├── Dockerfile
│   └── src/
│       ├── pages/
│       │   ├── HomePage.tsx
│       │   ├── EditorPage.tsx         # 三段式可拖拽布局
│       │   └── PreviewPage.tsx
│       ├── components/
│       │   ├── Layout.tsx
│       │   ├── ui/
│       │   │   ├── button.tsx
│       │   │   ├── dialog.tsx
│       │   │   ├── dropdown-menu.tsx
│       │   │   ├── toast.tsx
│       │   │   └── toaster.tsx
│       │   └── editor/
│       │       ├── SectionDragList.tsx
│       │       ├── SortableSection.tsx
│       │       ├── ThemeSelector.tsx    # 下拉菜单选择
│       │       └── ExportPanel.tsx      # PDF/HTML + Smart One-Page 开关
│       ├── stores/
│       │   ├── resumeStore.ts          # zustand
│       │   └── historyStore.ts         # undo/redo
│       ├── hooks/
│       │   ├── useKeyboardShortcuts.ts
│       │   ├── useDraftBackup.ts
│       │   └── use-toast.ts
│       └── lib/
│           ├── api.ts
│           ├── markdown.ts
│           └── utils.ts
├── docker-compose.yml
├── .env.example
├── .gitignore
├── AGENTS.md                          # TDD 规范
├── progress.md                        # 本文件
└── README.md
```

## 技术栈

| 层 | 技术 | 用途 |
|---|---|---|
| **后端** | Spring Boot 3.2 + Java 17 | REST API |
| **ORM** | Spring Data JPA + Hibernate | PostgreSQL 持久化 |
| **DB** | PostgreSQL 16 | 存储简历和主题 |
| **Markdown** | commonmark (org.commonmark) | md → HTML |
| **PDF 导出** | Playwright for Java | 无头 Chromium 渲染 PDF |
| **前端框架** | React 18 + TypeScript | UI |
| **构建** | Vite | 开发/构建 |
| **组件库** | shadcn/ui + Tailwind CSS | 界面组件 |
| **编辑器** | @uiw/react-md-editor | CodeMirror 6 Markdown 编辑 |
| **拖拽** | @dnd-kit/core + @dnd-kit/sortable | 章节拖拽排序 |
| **下拉菜单** | @radix-ui/react-dropdown-menu | 主题选择器 |
| **状态管理** | zustand | 全局状态 |
| **路由** | react-router-dom v6 | 前端路由 |
| **HTTP** | axios | API 请求 |
| **部署** | Docker Compose | 一键启动 |

## 内置主题（7 个）

| 主题 | ID | 风格 | 配色 |
|---|---|---|---|
| **Classic** (默认) | `classic` | 传统商务 / Times New Roman 衬线 / 大写强调 | 纯黑白 |
| **Modern** | `modern` | 科技公司 / Inter 无衬线 / 圆点列表 | 蓝 `#2563eb` |
| **Minimal** | `minimal` | 极简学术 / system-ui / 超大留白 | 灰 `#999` |
| **Sidebar** | `sidebar` | 双栏侧边栏 / 彩色侧栏 + 主内容 | 深蓝 `#1a365d` |
| **Stack Overflow** | `stackoverflow` | 开发者社区 / 标签式技能 / 卡片背景 | 橙 `#f48024` |
| **Elegant** | `elegant` | 轻奢商务 / Georgia 衬线 / 暖白底 | 墨绿 `#1b4332` |
| **Compact** | `compact` | 紧凑密集 / 9.5pt 最小间距 | 纯黑 `#555` |

## TDD 规范

参见项目根目录的 `AGENTS.md` — 规定了 Red-Green-Refactor 流程、分层测试策略、命名约定等。

## 测试覆盖（98 测试）

| 模块 | 文件 | 用例 | 覆盖内容 |
|---|---|---|---|
| **Backend** — MarkdownServiceTest | `service/` | 8 | heading/bold/list/link/paragraph/null/blank 转换 |
| **Backend** — SmartOnePageServiceTest | `service/` | 8 | 短内容 / 长内容压缩 / 超长警告 / 继承 / 下限保护 |
| **Backend** — ResumeServiceTest | `service/` | 9 | findAll/findById/create/update/delete + 部分更新 |
| **Backend** — ThemeServiceTest | `service/` | 5 | 列表 / 查找 / 启动时覆盖 / 资源回退 |
| **Backend** — ThemeCssCompletenessTest | `service/` | 1 | 验证所有 7 个主题包含必需的选择器 |
| **Backend** — PdfGenerationServiceTest | `service/` | 4 | 不可用时 fallback / Mock 浏览器 |
| **Backend** — ResumeControllerTest | `controller/` | 14 | HTTP 端点 / 400/404/503 / export header / 部分更新 |
| **Backend** — ThemeControllerTest | `controller/` | 5 | 列表 / 查找 / CSS 内容 / 404 |
| **Frontend** — markdown.test.ts | `lib/` | 7 | 章节解析 / 行范围 / 拖拽重排 |
| **Frontend** — resumeStore.test.ts | `stores/` | 4 | 初始状态 / setContent / setTitle / null safety |
| **Frontend** — historyStore.test.ts | `stores/` | 9 | push/undo/redo/canUndo/canRedo/reset |
| **Frontend** — useKeyboardShortcuts.test.ts | `hooks/` | 3 | Cmd+S / Cmd+Z / Cmd+Shift+Z |
| **Frontend** — Layout.test.tsx | `components/` | 2 | 标题渲染 / 按钮存在 |
| **Frontend** — ExportPanel.test.tsx | `components/` | 6 | 按钮渲染 / Smart One-Page 开关 / API 参数传递 |
| **Frontend** — SortableSection.test.tsx | `components/` | 3 | 渲染 / 点击调用 onClick / 拖拽手柄不触发 |
| **Frontend** — ThemeSelector.test.tsx | `components/` | 4 | trigger 文本 / palette 图标 / 展开后 7 项 / 点击切换 |
| **Frontend** — EditorPage.test.tsx | `pages/` | 5 | PanelGroup / resize handles / 预览 API 调用 / 主题切换刷新 |
| **合计** | **17 文件** | **98** | **全部通过** |

---

## 开发路线

### ✅ Phase 1 — 项目骨架（已完成）
- [x] Spring Boot 3.2 + Maven 项目初始化
- [x] React 18 + Vite + TypeScript 脚手架
- [x] shadcn/ui + Tailwind CSS 主题系统
- [x] Docker Compose：PostgreSQL + Backend + Frontend
- [x] 三端联调验证通过

### ✅ Phase 2 — 后端核心（已完成）
- [x] Resume 实体 (JPA) + CRUD API
- [x] Theme 实体 + 启动时加载内置主题
- [x] Markdown → HTML 转换（commonmark）
- [x] HTML 导出（ExportService）
- [x] Playwright 集成 + PDF 生成
- [x] 验证注解 + 部分更新支持

### ✅ Phase 3 — 前端基础页面（已完成）
- [x] HomePage：简历列表（grid 卡片 + 新建/编辑/删除）
- [x] EditorPage：三段式可拖拽布局（PanelGroup）
- [x] PreviewPage：全屏 A4 iframe 渲染预览
- [x] 自动保存（debounce 800ms）
- [x] 章节拖拽排序（@dnd-kit）
- [x] 章节点击定位到编辑器

### ✅ Phase 4 — 主题系统（已完成）
- [x] 7 个内置 CSS 主题（Classic / Modern / Minimal / Sidebar / Stack Overflow / Elegant / Compact）
- [x] 前端 DropdownMenu 主题选择器
- [x] 预览区动态注入主题 CSS + 主题切换即时刷新
- [x] 主题 API（GET /api/themes, GET /api/themes/{id}/css）
- [x] ThemeCssCompletenessTest 验证所有主题覆盖必需选择器

### ✅ Phase 5 — 导出与智能一页（已完成）
- [x] ExportService HTML 生成
- [x] 前端 ExportPanel（PDF / HTML / Smart One-Page 开关 / loading / 错误 Dialog）
- [x] SmartOnePageService Playwright 集成（scrollHeight 测量 + 逐步压缩）
- [x] PlaywrightConfig 单例管理（含 Chromium 不可用 fallback）
- [x] PdfGenerationService Playwright PDF 生成
- [x] PDF 导出端点 + 503/400/500 错误处理
- [x] injectCssVariables CSS 变量注入

### ✅ Phase 6 — UX 打磨（已完成）
- [x] 键盘快捷键（Cmd+S 保存 / Cmd+Z 撤销 / Cmd+Shift+Z 重做）
- [x] localStorage 草稿备份 + 自动恢复 Dialog
- [x] Undo/Redo 历史栈（50 步上限）
- [x] Toast 通知组件（保存成功 / 导出成功/失败）
- [x] 空状态引导（新建引导 + 三步指引卡片）
- [x] EditorPage 后端精确预览（debounce 800ms，fallback 客户端渲染）

### ✅ 文档（已完成）
- [x] README.md 完整项目说明
- [x] AGENTS.md TDD 开发规范
- [x] progress.md 开发进度跟踪

---

## 启动方式

```bash
# 开发模式
docker compose up -d postgres                          # 启动数据库
cd backend && mvn spring-boot:run                      # 后端 :8080
cd frontend && npm install && npm run dev              # 前端 :3000

# 生产模式
docker compose up --build                              # 全部容器

# 测试
cd backend && mvn test                                 # 后端 54 用例
cd frontend && npm test                                # 前端 44 用例
```
