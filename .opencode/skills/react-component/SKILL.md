---
name: react-component
description: React 18 + TypeScript component checklist. Use when creating or editing frontend pages, components, zustand stores, hooks, or api.ts (axios) in frontend/src/.
---

## React 组件编写 Checklist

- [ ] 使用 TypeScript 函数组件 + Props interface
- [ ] 通用 UI 放 `components/ui/`，业务组件放 `components/editor/`
- [ ] 页面组件放 `pages/`，路由在 `App.tsx` 定义
- [ ] 全局状态用 zustand，局部状态用 `useState`
- [ ] shadcn 组件从 `@/components/ui/` 导入
- [ ] 图标用 `lucide-react`
- [ ] API 调用通过 `@/lib/api.ts`（resumeApi / themeApi）
- [ ] JWT token 通过 axios interceptor 自动注入
- [ ] 样式用 Tailwind CSS，不写独立 CSS 文件
- [ ] 测试放同级 `__tests__/` 目录
- [ ] 文件名：`PascalCase.tsx`
