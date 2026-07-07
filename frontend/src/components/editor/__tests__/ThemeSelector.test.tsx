import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import ThemeSelector from '@/components/editor/ThemeSelector'

const mockSetTheme = vi.fn()
const mockFetchThemes = vi.fn()

vi.mock('@/stores/resumeStore', () => ({
  useResumeStore: vi.fn((selector?: any) => {
    const state = {
      themes: [
        { id: 'classic', name: 'Classic', description: '', builtIn: true },
        { id: 'modern', name: 'Modern', description: '', builtIn: true },
        { id: 'minimal', name: 'Minimal', description: '', builtIn: true },
        { id: 'sidebar', name: 'Sidebar', description: '', builtIn: true },
        { id: 'stackoverflow', name: 'Stack Overflow', description: '', builtIn: true },
        { id: 'elegant', name: 'Elegant', description: '', builtIn: true },
        { id: 'compact', name: 'Compact', description: '', builtIn: true },
      ],
      currentResume: { id: '1', themeId: 'classic' },
      fetchThemes: mockFetchThemes,
      setTheme: mockSetTheme,
    }
    return selector ? selector(state) : state
  }),
}))

describe('ThemeSelector', () => {
  beforeEach(() => vi.clearAllMocks())

  it('renders all 7 theme buttons', () => {
    render(<ThemeSelector />)
    expect(screen.getByText('Classic')).toBeTruthy()
    expect(screen.getByText('Modern')).toBeTruthy()
    expect(screen.getByText('Minimal')).toBeTruthy()
    expect(screen.getByText('Sidebar')).toBeTruthy()
    expect(screen.getByText('Stack Overflow')).toBeTruthy()
    expect(screen.getByText('Elegant')).toBeTruthy()
    expect(screen.getByText('Compact')).toBeTruthy()
  })
})
