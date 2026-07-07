import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
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

describe('ThemeSelector dropdown', () => {
  beforeEach(() => vi.clearAllMocks())

  it('shows current theme name in trigger button', () => {
    render(<ThemeSelector />)
    expect(screen.getByText('Classic')).toBeTruthy()
  })

  it('shows palette icon in trigger', () => {
    render(<ThemeSelector />)
    const trigger = screen.getByRole('button')
    expect(trigger.querySelector('svg')).toBeTruthy()
  })

  it('shows all 7 themes when dropdown is opened', async () => {
    render(<ThemeSelector />)
    await userEvent.click(screen.getByRole('button'))
    expect(screen.getByText('Modern')).toBeTruthy()
    expect(screen.getByText('Sidebar')).toBeTruthy()
    expect(screen.getByText('Stack Overflow')).toBeTruthy()
    expect(screen.getByText('Elegant')).toBeTruthy()
    expect(screen.getByText('Compact')).toBeTruthy()
    expect(screen.getByText('Minimal')).toBeTruthy()
  })

  it('calls setTheme when a different theme is clicked', async () => {
    render(<ThemeSelector />)
    await userEvent.click(screen.getByRole('button'))
    await userEvent.click(screen.getByText('Modern'))
    expect(mockSetTheme).toHaveBeenCalledWith('modern')
  })
})
