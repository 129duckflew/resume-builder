---
name: testing
description: Test conventions checklist (backend mvn test / frontend npm test). Use when writing @WebMvcTest/@ExtendWith/MockMvc or vitest render/renderHook tests, or before running the full suite.
---

## 测试规范 Checklist

- [ ] 后端：`cd backend && mvn test`（62 用例）
- [ ] 前端：`cd frontend && npm test`（44 用例）
- [ ] Controller 测试：`@WebMvcTest` + `@AutoConfigureMockMvc(addFilters=false)` + `MockMvc`
- [ ] Service 测试：`@ExtendWith(MockitoExtension.class)` + `@Mock` Repository
- [ ] 前端组件测试：`render` + `screen.getByText` / `getByRole` / `getByDisplayValue`
- [ ] 前端 Store 测试：`getState()` / `setState()` 直接操作
- [ ] 前端 Hooks 测试：`renderHook` + `userEvent`
- [ ] 纯函数测试：直接 import 测试输入/输出
- [ ] Mock axios：`vi.mock('@/lib/api')` + `vi.mocked().mockResolvedValue()`
- [ ] Mock zustand：`vi.mock('@/stores/xxxStore')` + selector 模拟
- [ ] 避免 mock 纯逻辑（markdown.ts 等）
- [ ] 每个功能至少一个测试覆盖正常路径 + 一个边界条件
