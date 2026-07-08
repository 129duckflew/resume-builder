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
│       │   ├── ResumeController.java           # CRUD + preview + export + JSON import + styles
│       │   ├── ThemeController.java
│       │   └── SectionTemplateController.java
│       ├── entity/
│       │   ├── Resume.java
│       │   ├── Theme.java
│       │   ├── SectionTemplate.java
│       │   └── ResumeStyle.java                # 按 (resumeId, themeId) 存储样式覆盖
│       ├── repository/
│       │   ├── ResumeRepository.java
│       │   ├── ThemeRepository.java
│       │   ├── SectionTemplateRepository.java
│       │   └── ResumeStyleRepository.java
│       ├── service/
│       │   ├── ResumeService.java
│       │   ├── MarkdownService.java
│       │   ├── ExportService.java
│       │   ├── SmartOnePageService.java
│       │   ├── PdfGenerationService.java
│       │   ├── ThemeService.java
│       │   ├── SectionTemplateService.java
│       │   ├── JsonResumeConverter.java
│       │   └── ResumeStyleService.java          # GET/PUT per-theme styles
│       └── dto/
│           ├── ResumeDTO.java
│           └── JsonResumeDTO.java
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
│       │   ├── EditorPage.tsx
│       │   └── PreviewPage.tsx
│       ├── components/
│       │   ├── Layout.tsx
│       │   ├── ui/
│       │   │   ├── button.tsx, dialog.tsx, dropdown-menu.tsx, toast.tsx, toaster.tsx
│       │   └── editor/
│       │       ├── SectionDragList.tsx
│       │       ├── SectionTemplatePicker.tsx
│       │       ├── SortableSection.tsx
│       │       ├── ThemeSelector.tsx             # 切换主题时保存/加载样式
│       │       └── ExportPanel.tsx
│       ├── stores/
│       │   ├── resumeStore.ts                   # + themeStyles 持久化
│       │   └── historyStore.ts
│       ├── types/
│       │   ├── resume.ts                        # + ThemeStyle 类型
│       │   ├── sectionTemplate.ts
│       │   └── desensitize.ts
│       ├── hooks/
│       │   ├── useKeyboardShortcuts.ts
│       │   ├── useDraftBackup.ts
│       │   └── use-toast.ts
│       └── lib/
│           ├── api.ts                           # + getStyles/updateStyles
│           ├── markdown.ts
│           └── utils.ts
├── themes/                             # 7 个内置 CSS 主题
├── .opencode/
│   └── agents/
│       ├── api-designer.md, code-reviewer.md, docker-ops.md, doc-recorder.md
├── docker-compose.yml
├── AGENTS.md
├── ROADMAP.md
└── progress.md
```

## 测试基线

| 模块 | 通过/总计 |
|------|----------|
| Backend | 110/110 |
| Frontend | 58/58 |
| **合计** | **168/168** ✅ |

## 已完成目标

| 目标 | 提交 | 核心文件 |
|------|------|---------|
| 1. 自定义章节模板 | `7bc7736` | SectionTemplate entity/service/controller, SectionTemplatePicker |
| 2. JSON Resume 导入导出 | `017aec1` + `48b8243` | JsonResumeConverter, JsonResumeDTO, import/export API + UI |
| 3. 切换主题保留样式微调 | `5e888df` | ResumeStyle entity/repo/service, ResumeController (styles), api.ts, resumeStore.ts, types/resume.ts |

## 进行中

- **维护与技术债务** — 三个核心目标已全部完成。下一步建议：补充集成测试、优化 CI/CD、重构前端 store、升级依赖、完善错误边界。请确认方向。

## 启动方式

```bash
# 开发模式
docker compose up -d postgres
cd backend && mvn spring-boot:run                    # :8081
cd frontend && npm install && npm run dev            # :3000

# 生产模式
docker compose up --build

# 测试
cd backend && mvn test                               # 110 用例
cd frontend && npm test                              # 58 用例
```
