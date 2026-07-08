# Resume Builder — 项目蓝图

## 短期高价值目标

---

### 目标一：自定义章节模板

降低用户输入门槛，提供预置章节模板并支持自定义。

| 步骤 | 内容 | 涉及文件 |
|---|---|---|
| 1.1 | 设计 `SectionTemplate` 实体：`id`, `userId`(null=系统内置), `name`, `icon`, `prompt`(markdown 模板), `sortOrder` | `entity/SectionTemplate.java` |
| 1.2 | 初始化系统内置模板（个人信息、工作经历、教育背景、技能、项目经历、证书） | `service/SectionTemplateService.java` + `@PostConstruct` |
| 1.3 | API: `GET /api/section-templates`（系统+用户）、`POST/PUT/DELETE /api/section-templates`（用户自定义） | `controller/SectionTemplateController.java` |
| 1.4 | 前端 Section 侧栏增加「+ 添加章节」按钮，弹出模板选择列表 | `components/editor/SectionDragList.tsx` → 新增 `SectionTemplatePicker` 组件 |
| 1.5 | 选中模板后，在编辑器光标处插入模板 Markdown 内容 | `pages/EditorPage.tsx` 集成 |
| 1.6 | 为新增端点添加测试 | `SectionTemplateServiceTest.java`, `SectionTemplateControllerTest.java` |

---

### 目标二：JSON Resume 导入导出

支持 [jsonresume.org](https://jsonresume.org) 标准格式，实现跨平台简历迁移。

| 步骤 | 内容 | 涉及文件 |
|---|---|---|
| 2.1 | 设计 `jsonresume-schema` 到内部 Markdown 的双向转换器 | `service/JsonResumeConverter.java` |
| 2.2 | 实现 `import(content)`：将 JSON Resume → Markdown（按 `basics`、`work`、`education`、`skills` 等节生成对应 Markdown 标题+列表） | `service/JsonResumeConverter.java` |
| 2.3 | 实现 `export(resume)`：解析现有 Markdown → JSON Resume 结构（按 Markdown 标题映射回 JSON 字段） | `service/JsonResumeConverter.java` |
| 2.4 | API: `POST /api/resumes/import/json`（创建新简历）、`GET /api/resumes/{id}/export/json`（下载 `.json`） | `controller/ResumeController.java` |
| 2.5 | 前端 HomePage 增加「导入 JSON」按钮；Editor/Preview 增加「导出 JSON」按钮 | `pages/HomePage.tsx`, `components/editor/ExportPanel.tsx` |
| 2.6 | 为转换器添加测试（round-trip 验证） | `JsonResumeConverterTest.java` |

---

### 目标三：切换主题保留用户样式微调

当前切换主题会丢失用户对 `fontSize`/`lineHeight` 的微调。目标是将用户自定义样式持久化并按主题独立存储。

| 步骤 | 内容 | 涉及文件 |
|---|---|---|
| 3.1 | 扩展 Resume 实体或新建 `ResumeStyle` 实体，按 `(resumeId, themeId)` 存储样式覆盖值 | `entity/ResumeStyle.java` |
| 3.2 | 新增 `GET /api/resumes/{id}/styles?themeId=` 和 `PUT /api/resumes/{id}/styles` 端点 | `controller/ResumeController.java`, `service/ResumeStyleService.java` |
| 3.3 | 前端 `ThemeSelector` 切换主题时，先保存当前主题的样式，再加载新主题的已存样式 | `components/editor/ThemeSelector.tsx` |
| 3.4 | 导出时应用当前主题 + 对应样式覆盖 | `service/ExportService.java` |
| 3.5 | 为服务和端点添加测试 | `ResumeStyleServiceTest.java` |

---

## 验收标准

每个目标完成后：`mvn test` + `npm test` 全部通过，docker 部署后手动验证核心流程。每个目标的实现 + 测试在同一 commit 内。
