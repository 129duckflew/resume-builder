import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return { ...actual as any, useParams: () => ({ id: 'test-id' }) }
})

const mockUpdateResume = vi.fn()
const mockFetchResume = vi.fn().mockResolvedValue(undefined)
const mockSetContent = vi.fn()
const mockPushState = vi.fn()
const mockReset = vi.fn()

vi.mock('@/stores/resumeStore', () => ({
  useResumeStore: vi.fn((selector?: any) => {
    const state = {
      currentResume: {
        id: 'test-id',
        title: 'Test Resume',
        content: '# Hello\n## Section\ncontent',
        themeId: 'classic',
        fontSize: null,
        lineHeight: null,
        sectionSpacing: 'normal',
        createdAt: '',
        updatedAt: '',
      },
      currentThemeCss: 'body { color: black; }',
      themes: [{ id: 'classic', name: 'Classic', description: '', builtIn: true }],
      loading: false,
      fetchResume: mockFetchResume,
      fetchThemes: vi.fn(),
      updateResume: mockUpdateResume,
      setContent: mockSetContent,
    }
    return selector ? selector(state) : state
  }),
}))

vi.mock('@/stores/historyStore', () => ({
  useHistoryStore: vi.fn((selector?: any) => {
    const state = {
      pushState: mockPushState,
      reset: mockReset,
      undo: vi.fn(),
      redo: vi.fn(),
    }
    return selector ? selector(state) : state
  }),
}))

vi.mock('@/hooks/useDraftBackup', () => ({
  useDraftBackup: () => ({
    getDraft: vi.fn(() => null),
    clearDraft: vi.fn(),
  }),
}))

vi.mock('@/hooks/useKeyboardShortcuts', () => ({
  useKeyboardShortcuts: vi.fn(),
}))

vi.mock('@/lib/api', () => ({
  resumeApi: {
    preview: vi.fn().mockResolvedValue('<h1>Preview</h1>'),
  },
  themeApi: {
    getCss: vi.fn().mockResolvedValue('body { color: black; }'),
  },
}))

vi.mock('@/hooks/use-toast', () => ({
  useToast: () => ({ toasts: [], toast: vi.fn(), dismiss: vi.fn() }),
  toast: vi.fn(),
}))

import EditorPage from '@/pages/EditorPage'

describe('EditorPage resizable panels', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders PanelGroup with three panels', () => {
    render(
      <MemoryRouter initialEntries={['/editor/test-id']}>
        <Routes>
          <Route path="/editor/:id" element={<EditorPage />} />
        </Routes>
      </MemoryRouter>,
    )

    expect(screen.getByText('Sections')).toBeTruthy()
    expect(screen.getByDisplayValue('Test Resume')).toBeTruthy()
  })

  it('renders resize handles between panels', () => {
    const { container } = render(
      <MemoryRouter initialEntries={['/editor/test-id']}>
        <Routes>
          <Route path="/editor/:id" element={<EditorPage />} />
        </Routes>
      </MemoryRouter>,
    )

    const handles = container.querySelectorAll('[data-resize-handle]')
    expect(handles.length).toBe(2)
  })

  it('renders sections sidebar, editor, and preview area', () => {
    const { container } = render(
      <MemoryRouter initialEntries={['/editor/test-id']}>
        <Routes>
          <Route path="/editor/:id" element={<EditorPage />} />
        </Routes>
      </MemoryRouter>,
    )

    const panels = container.querySelectorAll('[data-panel]')
    expect(panels.length).toBe(3)
  })
})
