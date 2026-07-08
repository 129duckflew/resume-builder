---
name: react-component
description: React 18 + TypeScript component checklist. Use when creating or editing frontend pages, components, zustand stores, hooks, or api.ts (axios) in frontend/src/.
---

## React 组件编写 Checklist

### 架构规范
- [ ] 通用 UI 放 `components/ui/`，业务组件放 `components/editor/`
- [ ] 页面组件放 `pages/`，路由在 `App.tsx` 定义
- [ ] 全局状态用 zustand，局部状态用 `useState`
- [ ] shadcn 组件从 `@/components/ui/` 导入
- [ ] 图标用 `lucide-react`
- [ ] API 调用通过 `@/lib/api.ts`（resumeApi / themeApi / jsonResumeApi）
- [ ] JWT token 通过 axios interceptor 自动注入

### 代码规范
- [ ] 使用 TypeScript 函数组件 + Props interface
- [ ] 文件名：`PascalCase.tsx`
- [ ] 样式用 Tailwind CSS，不写独立 CSS 文件
- [ ] 避免内联 style，使用 cn() + tailwind-merge 组合类名
- [ ] 动画用 tailwindcss-animate + tailwind.config.ts 的 keyframes
- [ ] 避免将原生 `confirm()` / `alert()` 直接用于用户交互，使用 Dialog/ConfirmDialog

### 测试规范
- [ ] 测试放同级 `__tests__/` 目录
- [ ] 组件测试：`render` + `screen` + `userEvent` 模拟交互
- [ ] Mock Radix dialog：不需要专门 mock，Radix Portal 在 JSDOM 中正常工作
- [ ] Mock toast：`vi.mock('@/hooks/use-toast', () => ({ toast: mockToast }))`
- [ ] 与 JSDOM 不兼容的 API（如 ResizeObserver）需要 polyfill

### 数据流
- [ ] API 调用 → zustand action（store）→ 组件读取
- [ ] 避免在组件中直接调用 axios
- [ ] async action 使用 try/catch，错误通过 toast 展示
- [ ] loading 状态使用独立的 boolean state，不与 data 混用
