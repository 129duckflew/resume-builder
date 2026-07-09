# Resume Builder — 项目蓝图

## 进度总览

| 目标 | 状态 | 提交 |
|---|---|---|
| ✅ 1. 自定义章节模板 | 已完成 | `7bc7736` |
| ✅ 2. JSON Resume 导入导出 | 已完成 | `017aec1` + `48b8243` |
| ✅ 3. 切换主题保留样式微调 | 已完成 | `5e888df` |
| ✅ 4. 服务端版本历史 | 已完成 | `5aae6d6` |
| ✅ 5. 可分享简历链接（含脱敏） | 已完成 | `fdeadc0` |
| ✅ 6. E2E 测试 (Playwright) | 已完成 | `0eacbb5` |
| ✅ 7. AI 助手（用户管理 API Key） | 已完成 | `c0ed1c7` |
| ✅ 8. 高级样式删除确认弹窗 | 已完成 | `bea7e30` |
| ✅ 9. CI GitHub Actions (G15) | 已完成 | `0313262` |
| ✅ 10. README 文档同步 (G16) | 已完成 | `0238e5c` |

> 已完成目标详情见 `progress.md`。测试基线 189/189 ✅，E2E core-flow 3 specs ✅。

---

## 战略方向

产品定位：**本地优先、隐私优先的求职工具**。已完成"做简历"全链路，未来延伸到"用简历求职"。

三个结构性短板：
1. 产品价值停在"做简历"，未覆盖投递命中率痛点。
2. 主题仅 CSS 换肤，结构/布局单一。
3. README/文档滞后于代码（列 2 controller/98 测试，实际 9 controller/189 测试）。

---

## 第一层 · 求职闭环（最高产品价值，建议优先）

把工具从"简历生成器"升级为"求职辅助器"。复用已有 AiService，低改造成本。

### G9 · ATS 关键词评分

粘贴岗位 JD → 分析简历关键词匹配度 + 缺失关键词 + 改写建议。

| # | 内容 | 涉及 |
|---|---|---|
| 9.1 | `AtsScoreService`：调用 LLM 对比简历内容与 JD，输出匹配度/缺失词/建议 | `service/AtsScoreService.java` |
| 9.2 | `POST /api/resumes/{id}/ats/score`（body: jd 文本）→ 返回评分 JSON | `controller/AtsScoreController.java` |
| 9.3 | 前端 EditorPage 增加 ATS 面板：JD 输入框 + 评分卡（匹配度环形图 + 关键词列表） | `components/editor/AtsPanel.tsx` |
| 9.4 | UI 明确"仅供参考"提示，依赖用户自带 LLM Key（沿用 G7） | —— |
| 9.5 | 测试 | —— |

**依赖**：复用 AiService / UserSettings 的 key 管理。**风险**：评分质量受模型影响，需免责声明。

### G10 · 岗位定制版简历

一键基于 JD 生成定向简历副本，保留原版。解决"海投 vs 定制"矛盾。

| # | 内容 | 涉及 |
|---|---|---|
| 10.1 | `POST /api/resumes/{id}/duplicate` 复制简历（含 content/style） | `controller/ResumeController.java` |
| 10.2 | `POST /api/resumes/{id}/ai/tailor`（body: jd）→ LLM 生成定制版内容草稿 | `controller/AiController.java` |
| 10.3 | 前端 ATS 面板增加"生成定制版"按钮 → 复制 + AI 改写 → 跳转新简历编辑 | `components/editor/AtsPanel.tsx` |
| 10.4 | 测试 | —— |

**依赖**：G9。**风险**：AI 改写可能丢信息，需保留原版可回退。

### G11 · AI 流式输出 (SSE)

完成 ROADMAP 7.5 遗留。润色/建议逐 token 渲染，体验质变。

| # | 内容 | 涉及 |
|---|---|---|
| 11.1 | `AiController` 改 `SseEmitter`，LLM 流式响应透传 | `controller/AiController.java` |
| 11.2 | 前端 `EventSource` 订阅，逐 token 渲染到面板/预览 | `components/editor/AiAssistant.tsx` |
| 11.3 | 错误处理 + 超时 + 取消机制 | —— |
| 11.4 | 测试 | —— |

**依赖**：无。**风险**：SSE 与现有 JWT 鉴权交互需验证（token 走 query 还是 header）。

---

## 第二层 · 模板与生态（差异化，中期）

### G12 · 多布局模板

突破 CSS 换肤：双栏/三栏/顶栏/侧栏结构化模板。布局是 ATS 与审美的矛盾解。

| # | 内容 | 涉及 |
|---|---|---|
| 12.1 | 新模板 schema：`layout`（slots 定义）+ 现有 `style.css` | `themes/{id}/theme.json` |
| 12.2 | 渲染层按 layout 组装 section 到 slots，而非单一 `.resume-page` | `service/ExportService.java` / 前端预览 |
| 12.3 | 至少 2 个新布局模板（双栏 / 顶栏） | `themes/` |
| 12.4 | 测试 | —— |

**依赖**：无。**风险**：⭐ 高 —— 冲击"`@page`/`.resume-page` 是主题契约"假设，需 P2 先评估渲染契约改动面，否则可能拖长。

### G13 · DOCX 导出

很多招聘系统只收 Word。补齐格式互操作性。

| # | 内容 | 涉及 |
|---|---|---|
| 13.1 | 选型：Apache POI 或 docx4j | `backend/pom.xml` |
| 13.2 | `DocxExportService`：Markdown → DOCX（标题/列表/加粗映射） | `service/DocxExportService.java` |
| 13.3 | `POST /api/resumes/{id}/export/docx` | `controller/ResumeController.java` |
| 13.4 | 前端 ExportPanel 增加 DOCX 按钮 | `components/editor/ExportPanel.tsx` |
| 13.5 | 测试 | —— |

**依赖**：无。**风险**：DOCX 无 CSS 概念，样式还原度有限，需明确"结构优先"预期。

### G14 · 模板示例库

内置行业范例简历（应届/资深/转行），一键 fork。降低冷启动门槛。

| # | 内容 | 涉及 |
|---|---|---|
| 14.1 | 示例数据打包进 themes 或独立 `samples/` 目录 | `backend/src/main/resources/` |
| 14.2 | `GET /api/samples` 列表 + `POST /api/samples/{id}/fork` 复制为用户简历 | `controller/SampleController.java` |
| 14.3 | 前端 HomePage 增加"从模板开始"入口 | `pages/HomePage.tsx` |
| 14.4 | 测试 | —— |

**依赖**：G12（布局丰富后示例更有价值）。**风险**：示例内容需人工审核，避免误导。

---

## 第三层 · 工程基建（降风险，穿插进行）

### G15 · CI GitHub Actions

完成 ROADMAP 6.5 遗留。PR 自动跑 mvn/npm test + Docker e2e，防止回归。

| # | 内容 | 涉及 |
|---|---|---|
| 15.1 | Backend job: mvn test | `.github/workflows/ci.yml` |
| 15.2 | Frontend job: npm test + build | —— |
| 15.3 | E2E job: docker compose up → Playwright | —— |
| 15.4 | PR 状态检查 + 必过门 | branch protection |

**依赖**：无。**风险**：低。

### G16 · README / 文档同步

README 补齐 9 controller、189 测试、新功能；加截图。纯文档。

| # | 内容 | 涉及 |
|---|---|---|
| 16.1 | README API 表补齐至 9 controller | `README.md` |
| 16.2 | 测试数更新为 189，新增功能行（版本/分享/AI/脱敏/模板） | `README.md` |
| 16.3 | progress.md 同步测试基线与已完成目标表 | `progress.md` |
| 16.4 | 加截图（编辑器/主题/预览） | `docs/` |

**依赖**：无。**风险**：无。

### G17 · 版本 diff 对比

完成 ROADMAP 4.5 遗留。版本列表中选两版做行级 diff 高亮（新增/删除/修改）。

| # | 内容 | 涉及 |
|---|---|---|
| 17.1 | 后端 `GET /api/resumes/{id}/versions/diff?a=&b=` 返回行级 diff | `controller/ResumeVersionController.java` |
| 17.2 | 前端 VersionPanel 增加两版选择 + diff 视图 | `components/editor/VersionDiff.tsx` |
| 17.3 | 测试 | —— |

**依赖**：无。**风险**：低，闭环已有版本功能。

### G18 · i18n 中英双语

前端接入 i18next，中/英双语。覆盖面广但改动琐碎。

| # | 内容 | 涉及 |
|---|---|---|
| 18.1 | 接入 i18next + 语言切换器 | `frontend/src/i18n.ts` |
| 18.2 | 抽取所有硬编码中文到 locale 文件 | `locales/zh.json`, `locales/en.json` |
| 18.3 | 主题/导出文案本地化（后端文案通过 API 返回） | —— |
| 18.4 | 测试 | —— |

**依赖**：无。**风险**：改动面广，易遗漏；无新后端端点。

---

## 建议执行顺序

```
G16(文档) → G15(CI) → G17(diff)          // 先清技术债 + 防回归，低风险快速闭环
   → G9(ATS) → G11(SSE) → G10(定制版)    // 第一层求职闭环，核心价值
      → G12(多布局) → G13(DOCX) → G14(示例库)  // 第二层差异化
```

穿插：G18(i18n) 可在任意稳定节点后接入，建议 G10 完成后做（届时文案基本定型）。

理由：先 1-2 天清掉遗留债务（文档/CI/diff 都是小改），再投入有用户价值的求职闭环；模板生态放最后，因为它会动渲染契约，风险最高、且 ATS 价值更直接。

---

## 全局风险

- **G12 多布局**会冲击现有"`@page`/`.resume-page` 是主题契约"假设，需 P2 先做 API 设计评估渲染层改动面。
- **G9/G10/G11 都复用 AiService**，若 G7 的 key 管理有未发现的边界，会连锁暴露——建议 G9 前先让 `@code-reviewer` 复审 AiController/UserSettings。
- **SSE 鉴权**（G11）：JWT 在 EventSource 下无法走 header，需改 query token 或 cookie，触及 SecurityConfig，需加载 `auth-security` skill。

---

## 验收标准

每个目标完成后：`mvn test` + `npm test` 全部通过，docker 部署后手动验证核心流程。每个目标的实现 + 测试在同一 commit 内。
