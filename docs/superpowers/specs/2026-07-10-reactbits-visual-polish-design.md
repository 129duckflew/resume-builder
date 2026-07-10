# React Bits Visual Polish Design

## Goal

Introduce selected react-bits-style visual polish into the frontend without adding npm dependencies or changing core resume-building workflows.

## Scope

- Add zero-dependency visual effect components under `frontend/src/components/effects/`.
- Add an animated decorative background to login and register pages.
- Add gradient text to the HomePage header.
- Add spotlight cards to the HomePage empty-state steps.
- Rebuild `ThemeSelector` from a dropdown list into a card-grid popover while preserving theme selection, custom theme editing, deletion, and creation.

## Constraints

- No new npm dependencies.
- Keep shadcn/ui components for functional primitives such as buttons, dialogs, and confirmation dialogs.
- Effects must respect `prefers-reduced-motion`.
- Decorative effects must not intercept pointer events or keyboard focus.
- Existing theme store data flow remains unchanged.

## Architecture

Create a small `components/effects` layer for presentational, copy-paste-style components inspired by react-bits. These components are independent of app state and use Tailwind classes plus CSS custom properties. Functional UI remains in `components/ui`.

## Components

- `AuroraBackground`: decorative pure-CSS animated gradient background.
- `GradientText`: semantic text wrapper with animated gradient styling.
- `SpotlightCard`: container component that tracks pointer position on the card and exposes `--spotlight-x` and `--spotlight-y` CSS variables for a radial highlight.

## Integrations

- `LoginPage` and `RegisterPage` render `AuroraBackground` behind their existing forms.
- `HomePage` renders `GradientText` for the `My Resumes` heading and wraps empty-state step cards in `SpotlightCard`.
- `ThemeSelector` keeps the existing trigger button and dialogs, but the opened selector becomes a grouped card grid. Cards preserve current theme check marks, layout grouping, custom markers, edit/delete controls, and `setTheme(theme.id)` behavior.

## Testing

- Unit tests for `GradientText` and `SpotlightCard` verify rendering and pointer-position behavior.
- `ThemeSelector` tests continue to assert current theme label, grouped themes, custom marker, create dialog, and selection callbacks against the new card grid.
- Existing HomePage tests remain valid; add an empty-state assertion for spotlight-enhanced step cards if needed.
- Verify with `cd frontend && npm test` and `npm run build`.

## Risks

- Theme selector card grid can be cramped in narrow editor layouts. Mitigation: use a fixed popover width with responsive single/two-column grid classes and compact cards.
- Copy-paste effects require manual updates in the future. Mitigation: keep them small and dependency-free.
