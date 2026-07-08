import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, act } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
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

const initialStore = {
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
  themes: [
    { id: 'classic', name: 'Classic', description: '', builtIn: true },
    { id: 'modern', name: 'Modern', description: '', builtIn: true },
  ],
  loading: false,
  fetchResume: mockFetchResume,
  fetchThemes: vi.fn(),
  updateResume: mockUpdateResume,
  setContent: mockSetContent,
}

let storeState = { ...initialStore }

vi.mock('@/stores/resumeStore', () => ({
  useResumeStore: vi.fn((selector?: any) => {
    return selector ? selector(storeState) : storeState
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

const { mockPreview } = vi.hoisted(() => ({
  mockPreview: vi.fn().mockResolvedValue('<h1>Initial preview</h1>'),
}))

vi.mock('@/lib/api', () => {
  return {
    resumeApi: { preview: mockPreview },
    themeApi: { getCss: vi.fn().mockResolvedValue('body { color: black; }') },
  }
})

vi.mock('@/hooks/use-toast', () => ({
  useToast: () => ({ toasts: [], toast: vi.fn(), dismiss: vi.fn() }),
  toast: vi.fn(),
}))

import EditorPage from '@/pages/EditorPage'

function EditorPageApp() {
  return (
    <MemoryRouter initialEntries={['/editor/test-id']}>
      <Routes>
        <Route path="/editor/:id" element={<EditorPage />} />
      </Routes>
    </MemoryRouter>
  )
}

describe('EditorPage resizable panels', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    storeState = { ...initialStore, currentResume: { ...initialStore.currentResume } }
  })

  it('renders PanelGroup with three panels', () => {
    render(<EditorPageApp />)
    expect(screen.getByText('Sections')).toBeTruthy()
    expect(screen.getByDisplayValue('Test Resume')).toBeTruthy()
  })

  it('renders resize handles between panels', () => {
    const { container } = render(<EditorPageApp />)
    const handles = container.querySelectorAll('[data-resize-handle]')
    expect(handles.length).toBe(2)
  })

  it('renders sections sidebar, editor, and preview area', () => {
    const { container } = render(<EditorPageApp />)
    const panels = container.querySelectorAll('[data-panel]')
    expect(panels.length).toBe(3)
  })
})

describe('EditorPage preview refresh on theme change', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    storeState = { ...initialStore, currentResume: { ...initialStore.currentResume } }
  })

  it('calls preview API on initial render', async () => {
    render(<EditorPageApp />)
    await vi.waitFor(() => {
      expect(mockPreview).toHaveBeenCalledWith('test-id', true, false)
    })
  })

  it('re-fetches preview when themeId changes', async () => {
    const { rerender } = render(<EditorPageApp />)

    // Wait for initial preview call
    await vi.waitFor(() => {
      expect(mockPreview).toHaveBeenCalledTimes(1)
    })

    // Change themeId via store (triggers effect re-run with new themeId dep)
    storeState = {
      ...storeState,
      currentResume: { ...storeState.currentResume!, themeId: 'modern' },
      currentThemeCss: 'body { color: blue; }',
    }

    // Rerender causes EditorPage to read new store values
    act(() => { rerender(<EditorPageApp />) })

    // The effect should re-run because themeId changed → preview cleared + re-fetched
    await vi.waitFor(() => {
      expect(mockPreview).toHaveBeenCalledTimes(2)
    })
  })
})

describe('EditorPage section click locates editor', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    storeState = { ...initialStore, currentResume: { ...initialStore.currentResume } }
  })

  it('focuses textarea and places caret at the clicked section line', async () => {
    const user = userEvent.setup()
    render(<EditorPageApp />)

    // content = "# Hello\n## Section\ncontent"
    // section "Section" has startLine=1 → char offset = "# Hello\n".length = 8
    await user.click(screen.getByRole('button', { name: 'Section' }))

    const textarea = document.querySelector(
      'textarea.w-md-editor-text-input',
    ) as HTMLTextAreaElement
    expect(textarea).toBeTruthy()
    expect(document.activeElement).toBe(textarea)
    expect(textarea.selectionStart).toBe(8)
    expect(textarea.selectionEnd).toBe(8)
  })

  it('sets scrollTop on the editor scroll container for the clicked line', async () => {
    const user = userEvent.setup()
    render(<EditorPageApp />)

    const area = document.querySelector('.w-md-editor-area') as HTMLElement
    expect(area).toBeTruthy()
    const spy = vi.spyOn(area, 'scrollTop', 'set')

    // section "Hello" has startLine=0 → scrollTop = 0 * 18 = 0 (set still called)
    await user.click(screen.getByRole('button', { name: 'Hello' }))
    expect(spy).toHaveBeenCalled()

    // section "Section" has startLine=1 → scrollTop = 1 * 18 = 18
    spy.mockClear()
    await user.click(screen.getByRole('button', { name: 'Section' }))
    expect(spy).toHaveBeenCalledWith(18)
  })
})
