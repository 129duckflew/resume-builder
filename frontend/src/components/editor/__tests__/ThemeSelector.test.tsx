import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ThemeSelector from '@/components/editor/ThemeSelector'

const mockSetTheme = vi.fn()
const mockFetchThemes = vi.fn()
const mockCreateTheme = vi.fn()
const mockUpdateTheme = vi.fn()
const mockDeleteTheme = vi.fn()

vi.mock('@/stores/resumeStore', () => ({
  useResumeStore: vi.fn((selector?: any) => {
    const state = {
      themes: [
        { id: 'classic', name: 'Classic', description: '', builtIn: true, layout: 'single' },
        { id: 'modern', name: 'Modern', description: '', builtIn: true, layout: 'single' },
        { id: 'minimal', name: 'Minimal', description: '', builtIn: true, layout: 'single' },
        { id: 'sidebar', name: 'Sidebar', description: '', builtIn: true, layout: 'sidebar-left' },
        { id: 'stackoverflow', name: 'Stack Overflow', description: '', builtIn: true, layout: 'single' },
        { id: 'elegant', name: 'Elegant', description: '', builtIn: true, layout: 'single' },
        { id: 'compact', name: 'Compact', description: '', builtIn: true, layout: 'single' },
        { id: 'user-1-custom', name: 'My Custom', description: '', builtIn: false, layout: 'sidebar-right', userId: 1 },
      ],
      currentResume: { id: '1', themeId: 'classic' },
      fetchThemes: mockFetchThemes,
      setTheme: mockSetTheme,
      createTheme: mockCreateTheme,
      updateTheme: mockUpdateTheme,
      deleteTheme: mockDeleteTheme,
    }
    return selector ? selector(state) : state
  }),
}))

describe('ThemeSelector card grid', () => {
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

  it('shows themes grouped by layout when card grid is opened', async () => {
    render(<ThemeSelector />)
    await userEvent.click(screen.getByRole('button'))
    expect(screen.getByRole('dialog', { name: 'Choose a theme' })).toBeTruthy()
    // Should show layout group labels
    expect(screen.getByText('Single')).toBeTruthy()
    expect(screen.getByText('Two-Column')).toBeTruthy()
    // Should show theme names
    expect(screen.getByText('Modern')).toBeTruthy()
    expect(screen.getByText('Sidebar')).toBeTruthy()
    expect(screen.getByText('Stack Overflow')).toBeTruthy()
    expect(screen.getByText('Elegant')).toBeTruthy()
    expect(screen.getByText('Compact')).toBeTruthy()
    expect(screen.getByText('Minimal')).toBeTruthy()
  })

  it('shows custom theme with (Custom) marker', async () => {
    render(<ThemeSelector />)
    await userEvent.click(screen.getByRole('button'))
    expect(screen.getByText('My Custom')).toBeTruthy()
    expect(screen.getByText('(Custom)')).toBeTruthy()
  })

  it('shows Create Theme button', async () => {
    render(<ThemeSelector />)
    await userEvent.click(screen.getByRole('button'))
    expect(screen.getByText('Create Theme')).toBeTruthy()
  })

  it('calls setTheme when a different theme is clicked', async () => {
    render(<ThemeSelector />)
    await userEvent.click(screen.getByRole('button'))
    await userEvent.click(screen.getByText('Modern'))
    expect(mockSetTheme).toHaveBeenCalledWith('modern')
  })

  it('shows check mark on current theme', async () => {
    render(<ThemeSelector />)
    await userEvent.click(screen.getByRole('button'))
    expect(screen.getByRole('button', { name: /Classic selected/i })).toBeTruthy()
  })

  it('opens create dialog when Create Theme is clicked', async () => {
    render(<ThemeSelector />)
    await userEvent.click(screen.getByRole('button'))
    await userEvent.click(screen.getByText('Create Theme'))
    // Dialog should open with title
    expect(screen.getByText('Create Custom Theme')).toBeTruthy()
  })
})
