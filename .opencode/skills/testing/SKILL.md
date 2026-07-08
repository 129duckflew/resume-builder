---
name: testing
description: Test conventions checklist (backend mvn test / frontend npm test). Use when writing @WebMvcTest/@ExtendWith/MockMvc or vitest render/renderHook tests, or before running the full suite.
---

## 测试规范 Checklist

### 运行命令
- [ ] 后端：`cd backend && mvn test`（117 用例 / 17 测试类）
- [ ] 前端：`cd frontend && npm test`（72 用例 / 12 测试文件）

### 后端测试
- [ ] Controller：`@WebMvcTest` + `@AutoConfigureMockMvc(addFilters=false)` + `MockMvc`
- [ ] Service：`@ExtendWith(MockitoExtension.class)` + `@Mock` Repository
- [ ] Setup：`@BeforeEach` 中 `MockitoAnnotations.openMocks(this)`
- [ ] 测试方法命名：`方法_场景_预期结果`

### 前端测试
- [ ] 组件渲染：`render` + `screen.getByText` / `getByRole` / `getByDisplayValue`
- [ ] Store：`useXxxStore.getState()` / `setState()` 直接操作（不 mock）
- [ ] Hooks：`renderHook` + `userEvent`
- [ ] 纯函数：直接 import 测试输入/输出
- [ ] Mock api：`vi.mock('@/lib/api')` + `vi.mocked().mockResolvedValue()`
- [ ] Mock store selector：`vi.mock('@/stores/xxxStore', () => ({ useXxxStore: () => ({ ... }) }))`
- [ ] Mock hoisted：`const { mockFn } = vi.hoisted(() => ({ mockFn: vi.fn() }))`（解决 vi.mock 提升问题）
- [ ] 避免 mock 纯逻辑（markdown.ts 等）
- [ ] 测试文件命名：`{component}.test.ts` / `{component}.test.tsx`
- [ ] 测试文件位置：与源文件同级 `__tests__/` 目录
- [ ] 每个功能至少一个正常路径 + 一个边界条件
