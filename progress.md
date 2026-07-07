# Resume Builder — 开发进度

## 架构总览

```
resume-builder/
├── backend/          # Spring Boot 3.2 + Java 17
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/resume/
│       ├── ResumeApplication.java
│       ├── config/CorsConfig.java
│       ├── controller/
│       │   ├── ResumeController.java    # CRUD + preview + export
│       │   └── ThemeController.java     # theme list + CSS
│       ├── entity/
│       │   ├── Resume.java              # id, title, content(md), themeId, fontSize, lineHeight...
│       │   └── Theme.java
│       ├── repository/
│       ├── service/
│       │   ├── ResumeService.java       # CRUD + default markdown template
│       │   ├── MarkdownService.java     # commonmark: md → HTML
│       │   ├── ExportService.java       # HTML generation
│       │   ├── SmartOnePageService.java # 智能一页算法（Playwright 注入式）
│       │   └── ThemeService.java        # 启动时自动加载 3 个内置主题
│       └── dto/
├── frontend/         # React 18 + Vite + TypeScript
│   ├── package.json
│   ├── vite.config.ts + vitest config
│   ├── tailwind.config.ts
│   ├── nginx.conf
│   ├── Dockerfile
│   └── src/
│       ├── pages/
│       │   ├── HomePage.tsx         # 简历列表（grid 卡片）
│       │   ├── EditorPage.tsx       # 核心编辑器（三段式布局）
│       │   └── PreviewPage.tsx      # 全屏 A4 iframe 预览
│       ├── components/
│       │   ├── Layout.tsx
│       │   ├── ui/button.tsx        # shadcn/ui 按钮
│       │   └── editor/
│       │       ├── SectionDragList.tsx   # @dnd-kit 章节拖拽
│       │       ├── SortableSection.tsx
│       │       ├── ThemeSelector.tsx
│       │       └── ExportPanel.tsx
│       ├── stores/resumeStore.ts    # zustand
│       └── lib/
│           ├── api.ts               # axios 封装
│           ├── markdown.ts          # 章节解析/重组
│           └── utils.ts             # cn() helper
├── docker-compose.yml   # PostgreSQL 16 + Backend + Frontend
├── .env.example
└── .gitignore
```

## 技术栈

| 层 | 技术 | 用途 |
|---|---|---|
| **后端** | Spring Boot 3.2 + Java 17 | REST API |
| **ORM** | Spring Data JPA + Hibernate | PostgreSQL 持久化 |
| **DB** | PostgreSQL 16 | 存储简历和主题 |
| **Markdown** | commonmark (org.commonmark) | md → HTML |
| **PDF 导出** | Playwright for Java (待集成) | 无头浏览器渲染 PDF |
| **前端框架** | React 18 + TypeScript | UI |
| **构建** | Vite | 开发/构建 |
| **组件库** | shadcn/ui + Tailwind CSS | 界面组件 |
| **编辑器** | @uiw/react-md-editor | Markdown 编辑 |
| **拖拽** | @dnd-kit/core + @dnd-kit/sortable | 章节拖拽排序 |
| **状态管理** | zustand | 全局状态 |
| **路由** | react-router-dom v6 | 前端路由 |
| **HTTP** | axios | API 请求 |
| **部署** | Docker Compose | 一键启动 |

## 内置主题

| 主题 | ID | 风格 | 状态 |
|---|---|---|---|
| **Classic** (默认) | `classic` | Times New Roman 衬线 / 纯黑白 / 大写强调 / 两端对齐 | ✅ |
| **Modern** | `modern` | Inter 无衬线 / 蓝色 #2563eb 强调 / 圆点列表 | ✅ |
| **Minimal** | `minimal` | system-ui 无衬线 / 灰色 #999 / 超大留白 | ✅ |

## 测试覆盖

| 模块 | 文件 | 用例 | 覆盖内容 |
|---|---|---|---|
| **Backend** — MarkdownServiceTest | `service/` | 8 | heading/bold/list/link/paragraph/null/blank 转换 |
| **Backend** — SmartOnePageServiceTest | `service/` | 6 | 短内容默认 / 长内容压缩 / 超长警告 / 已有配置继承 / 下限保护 |
| **Backend** — ResumeServiceTest | `service/` | 8 | findAll/findById/create/update/delete CRUD 全路径 |
| **Backend** — ThemeServiceTest | `service/` | 5 | 列表 / 查找 / 启动加载 / 跳过已存在 / 资源回退 |
| **Backend** — ResumeControllerTest | `controller/` | 9 | 所有 HTTP 端点 + 400/404 + export header |
| **Backend** — ThemeControllerTest | `controller/` | 5 | 列表 / 查找 / CSS 内容 / 404 |
| **Frontend** — markdown.test.ts | `lib/` | 7 | 章节解析 / 行范围 / 拖拽重排 |
| **Frontend** — resumeStore.test.ts | `stores/` | 4 | 初始状态 / setContent / setTitle / null safety |
| **Frontend** — Layout.test.tsx | `components/` | 2 | 标题渲染 / 按钮存在 |
| **合计** | **9 文件** | **54** | **全部通过** |

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
- [x] 智能一页算法服务（占位，待 Playwright 注入）
- [x] 验证注解 + 异常处理

### ✅ Phase 3 — 前端基础页面（已完成）
- [x] HomePage：简历列表（grid 卡片 + 新建/编辑/删除）
- [x] EditorPage：三段式布局（章节侧栏 + Markdown 编辑器 + A4 预览）
- [x] PreviewPage：全屏 A4 iframe 渲染预览
- [x] 自动保存（debounce 500ms）
- [x] 章节拖拽排序（@dnd-kit）

### ✅ Phase 4 — 主题系统（已完成）
- [x] 3 个内置 CSS 主题（Classic / Modern / Minimal）
- [x] 前端 ThemeSelector 切换
- [x] 预览区 iframe 动态注入主题 CSS
- [x] 主题 API（GET /api/themes, GET /api/themes/{id}/css）

### 🔲 Phase 5 — 导出与智能一页（部分完成）
- [x] ExportService HTML 生成
- [x] 前端 ExportPanel（PDF / HTML 按钮）
- [x] SmartOnePageService 算法骨架
- [ ] **Playwright for Java 集成** — 精确渲染检测 + PDF 生成
- [ ] PDF 导出端点实际生成文件
- [ ] 前端导出错误提示（内容过长时引导用户精简）

### 🔲 Phase 6 — 打磨 UX
- [ ] 键盘快捷键（Cmd+S 保存 / Tab 缩进）
- [ ] localStorage 草稿备份
- [ ] 版本历史 / 撤销重做
- [ ] 更友好的空状态引导
- [ ] EditorPage 远程预览（调用后端 API 获得精确渲染）

---

## 启动方式

```bash
# 开发模式
docker compose up -d postgres                          # 启动数据库
cd backend && mvn spring-boot:run                      # 后端 :8080
cd frontend && npm run dev                             # 前端 :3000

# 生产模式
docker compose up --build                              # 全部容器

# 测试
cd backend && mvn test                                 # 后端 41 用例
cd frontend && npm test                                # 前端 13 用例
```
