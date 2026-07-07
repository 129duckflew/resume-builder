# TDD 开发规范

## 原则

- **Red-Green-Refactor**：先写失败测试 → 实现通过 → 重构优化
- 每个功能点必须至少有一个对应测试
- 测试即为文档：测试名描述行为意图（`方法_场景_预期结果`）
- 每次提交必须包含实现 + 对应的测试

## 分层测试策略

| 层 | 框架 | 测什么 |
|---|---|---|
| Controller | `@WebMvcTest` + MockMvc | HTTP 状态码、JSON body、header、校验 |
| Service | `@ExtendWith(MockitoExtension)` | 纯逻辑、边界条件、mock 依赖 |
| Frontend lib | vitest | 纯函数、工具函数 |
| Frontend store | vitest + zustand | state 变更、action |
| Frontend component | vitest + @testing-library/react | 渲染输出、用户交互 |

## 约定

- 测试文件命名：`{ClassName}Test.java` / `{module}.test.ts`（放被测文件同级 `__tests__/` 目录）
- 测试方法名：`方法_场景_预期结果`（Java） / `it('does x when y')`（TS）
- `describe` 按功能分组，`it` 描述具体行为
- 避免测试实现细节，只测公开 API 的行为
- Mock 外部依赖（数据库、文件系统、第三方服务），不 mock 纯逻辑

## 项目当前覆盖

| 模块 | 文件 | 用例数 |
|---|---|---|
| Backend Service | MarkdownService / SmartOnePageService / ResumeService / ThemeService | 27 |
| Backend Controller | ResumeController / ThemeController | 14 |
| Frontend lib | markdown.ts | 7 |
| Frontend store | resumeStore.ts | 4 |
| Frontend component | Layout.tsx | 2 |
| **合计** | **9 文件** | **54** |
