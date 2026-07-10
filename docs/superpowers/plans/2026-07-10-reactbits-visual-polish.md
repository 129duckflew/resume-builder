# React Bits Visual Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add targeted react-bits-style visual polish to auth pages, HomePage, and ThemeSelector with zero new npm dependencies.

**Architecture:** Add focused presentational components in `frontend/src/components/effects/`, then integrate them surgically into existing pages. Preserve shadcn/ui for functional primitives and keep ThemeSelector's store interactions unchanged while replacing the dropdown list with a card-grid popover.

**Tech Stack:** React 18, TypeScript, Vite, Tailwind CSS v3, Vitest, Testing Library.

## Global Constraints

- No new npm dependencies.
- Effects must respect `prefers-reduced-motion`.
- Decorative effects must not intercept pointer events or keyboard focus.
- `ThemeSelector` must preserve `setTheme`, `createTheme`, `updateTheme`, and `deleteTheme` flows.
- Verify with `cd frontend && npm test` and `npm run build`.

---

## File Structure

- Create `frontend/src/components/effects/AuroraBackground.tsx`: decorative animated auth-page background.
- Create `frontend/src/components/effects/GradientText.tsx`: text wrapper with gradient animation.
- Create `frontend/src/components/effects/SpotlightCard.tsx`: card wrapper with pointer-follow spotlight.
- Create `frontend/src/components/effects/__tests__/GradientText.test.tsx`: rendering tests for gradient text.
- Create `frontend/src/components/effects/__tests__/SpotlightCard.test.tsx`: pointer variable tests for spotlight card.
- Modify `frontend/src/pages/LoginPage.tsx`: render background behind existing form.
- Modify `frontend/src/pages/RegisterPage.tsx`: render background behind existing form.
- Modify `frontend/src/pages/HomePage.tsx`: apply gradient heading and spotlight empty-state cards.
- Modify `frontend/src/components/editor/ThemeSelector.tsx`: replace dropdown menu list with card-grid popover.
- Modify `frontend/src/components/editor/__tests__/ThemeSelector.test.tsx`: align expectations with card-grid popover while preserving behavior assertions.

## Tasks

### Task 1: Effects Components

- [ ] Write failing tests for `GradientText` and `SpotlightCard`.
- [ ] Run targeted tests and confirm missing components fail.
- [ ] Implement `GradientText` and `SpotlightCard` minimally.
- [ ] Add `AuroraBackground` as a presentational component.
- [ ] Run targeted effects tests and confirm pass.

### Task 2: Page Integrations

- [ ] Write or update tests for HomePage empty-state step cards using spotlight wrappers.
- [ ] Run targeted HomePage test and confirm failure.
- [ ] Integrate `GradientText`, `SpotlightCard`, and `AuroraBackground` into pages.
- [ ] Run targeted HomePage tests and confirm pass.

### Task 3: ThemeSelector Card Grid

- [ ] Update ThemeSelector tests to assert card-grid popover behavior.
- [ ] Run targeted ThemeSelector tests and confirm failure.
- [ ] Replace dropdown list with grouped card-grid popover while preserving dialogs and callbacks.
- [ ] Run targeted ThemeSelector tests and confirm pass.

### Task 4: Full Verification

- [ ] Run `cd frontend && npm test`.
- [ ] Run `cd frontend && npm run build`.
- [ ] Inspect `git diff --stat` and summarize changes.
