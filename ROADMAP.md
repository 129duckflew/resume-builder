# Resume Builder — 项目蓝图

## 进度

| 目标 | 状态 | 提交 |
|---|---|---|
| ✅ 1. 自定义章节模板 | 已完成 | `7bc7736` |
| ✅ 2. JSON Resume 导入导出 | 已完成 | `017aec1` + `48b8243` |
| ✅ 3. 切换主题保留样式微调 | 已完成 | `5e888df` |

---

## 中期核心能力

### 目标四：服务端版本历史

每次 `PUT /api/resumes/{id}` 自动存档，支持版本浏览、diff 对比、一键回滚。

| # | 内容 | 涉及 |
|---|---|---|
| 4.1 | 新建 `ResumeVersion` 实体：`id, resumeId, versionNumber, title, content, themeId, fontSize, lineHeight, sectionSpacing, createdAt` | `entity/ResumeVersion.java` |
| 4.2 | 实现 `ResumeVersionService`：每次 update 前自动 `saveSnapshot()`，版本号递增 | `service/ResumeVersionService.java` |
| 4.3 | API: `GET /api/resumes/{id}/versions`（列表）、`GET /api/resumes/{id}/versions/{version}`（详情）、`POST /api/resumes/{id}/versions/{version}/restore`（回滚） | `controller/ResumeVersionController.java` |
| 4.4 | 前端 EditorPage 增加版本侧栏/下拉：版本列表、显示时间戳、点击恢复 | `components/editor/VersionPanel.tsx` |
| 4.5 | 版本间 diff 对比展示（修改/新增/删除行高亮） | `components/editor/VersionDiff.tsx` |
| 4.6 | 测试 + 版本数上限策略（保留最近 N 版或自动清理旧版） | —— |

**影响评估**：数据库新表，业务核心变更，需注意每次 update 多一次 insert 写入。建议保留最近 50 版。

---

### 目标五：可分享链接

生成公开只读链接，可选择是否套用脱敏规则。

| # | 内容 | 涉及 |
|---|---|---|
| 5.1 | `ShareLink` 实体：`id(UUID), resumeId, enabled, desensitize, expiresAt, createdAt` | `entity/ShareLink.java` |
| 5.2 | `POST /api/resumes/{id}/share` 创建分享链接，`GET /s/{token}` 公开访问（无 JWT） | `controller/ShareController.java` |
| 5.3 | 公开端点返回纯 HTML（同 preview），受 `enabled` + `expiresAt` 控制 | `config/SecurityConfig.java` 放行 `/s/**` |
| 5.4 | 前端 Editor/Preview 增加"分享"按钮 → 创建链接 → 显示可复制 URL + 开关 | —— |
| 5.5 | 测试 | —— |

---

### 目标六：E2E 测试（Playwright）

用 Playwright 覆盖核心用户流程，避免回归。

| # | 内容 | 涉及 |
|---|---|---|
| 6.1 | 配置 Playwright Test Runner | `frontend/e2e/playwright.config.ts` |
| 6.2 | 注册 → 登录 → 创建简历 → 编辑内容 → 切换主题 → 保存 | `e2e/auth-resume.spec.ts` |
| 6.3 | 拖拽排序章节 → 导出 PDF → 导出 HTML → 导出 JSON | `e2e/export.spec.ts` |
| 6.4 | 导入 JSON → 验证内容渲染正确 | `e2e/import.spec.ts` |
| 6.5 | CI 集成（GitHub Actions / docker compose 启动 → 跑 e2e） | `.github/workflows/e2e.yml` |

**注意**：需要 docker 内 Playwright 浏览器镜像，或 host 模式共享主机 Chromium。

---

### 目标七：AI 辅助写作

接入 LLM API，提供语法润色、内容建议、根据岗位描述定制简历。

| # | 内容 | 涉及 |
|---|---|---|
| 7.1 | 后端 `AiService` 封装 LLM API 调用（OpenAI 兼容接口） | `service/AiService.java` |
| 7.2 | `POST /api/resumes/{id}/ai/rewrite` — 润色当前内容 | `controller/AiController.java` |
| 7.3 | `POST /api/resumes/{id}/ai/suggest` — 基于岗位描述生成定制建议 | —— |
| 7.4 | 前端 EditorPage 增加 Magic Wand 按钮 → 弹出 AI 助手面板 | `components/editor/AiAssistant.tsx` |
| 7.5 | 流式输出（SSE）逐 token 渲染到预览 | —— |
| 7.6 | API Key 管理（用户自行配置或系统全局配置） | —— |

---

## 验收标准

每个目标完成后：`mvn test` + `npm test` 全部通过，docker 部署后手动验证核心流程。每个目标的实现 + 测试在同一 commit 内。
