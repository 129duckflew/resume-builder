import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ThemeCustomizer from '@/components/editor/ThemeCustomizer'

const mockUpdateCustomVariable = vi.fn()
const mockResetCustomVariables = vi.fn()

vi.mock('@/stores/resumeStore', () => ({
  useResumeStore: vi.fn((selector?: any) => {
    const state = {
      currentThemeVariables: [
        { name: '--primary-color', type: 'color', defaultValue: '#2563eb', label: 'Primary Color', group: 'Colors' },
        { name: '--text-color', type: 'color', defaultValue: '#1a1a1a', label: 'Text Color', group: 'Colors' },
        { name: '--font-family', type: 'font', defaultValue: "'Inter', sans-serif", label: 'Font Family', group: 'Typography' },
        { name: '--font-size', type: 'size', defaultValue: '10.5pt', label: 'Base Font Size', group: 'Typography' },
        { name: '--page-margin', type: 'size', defaultValue: '18mm 22mm', label: 'Page Margin', group: 'Layout' },
      ],
      customVariables: {
        '--primary-color': '#ff0000',
      },
      updateCustomVariable: mockUpdateCustomVariable,
      resetCustomVariables: mockResetCustomVariables,
    }
    return selector ? selector(state) : state
  }),
}))

describe('ThemeCustomizer', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders group sections', () => {
    render(<ThemeCustomizer />)
    expect(screen.getByText('Colors')).toBeTruthy()
    expect(screen.getByText('Typography')).toBeTruthy()
    expect(screen.getByText('Layout')).toBeTruthy()
  })

  it('renders controls for each variable', () => {
    render(<ThemeCustomizer />)
    expect(screen.getByText('Primary Color')).toBeTruthy()
    expect(screen.getByText('Text Color')).toBeTruthy()
    expect(screen.getByText('Font Family')).toBeTruthy()
    expect(screen.getByText('Base Font Size')).toBeTruthy()
  })

  it('uses customVariables value when available', () => {
    render(<ThemeCustomizer />)
    const colorInputs = screen.getAllByDisplayValue('#ff0000')
    expect(colorInputs.length).toBeGreaterThanOrEqual(1)
  })

  it('uses default value when no custom override', () => {
    render(<ThemeCustomizer />)
    const defaultInputs = screen.getAllByDisplayValue('#1a1a1a')
    expect(defaultInputs.length).toBeGreaterThanOrEqual(1)
  })

  it('calls updateCustomVariable when typing a new value', async () => {
    render(<ThemeCustomizer />)
    // Find the color text input for text-color (default, no override)
    const sizeInputs = screen.getAllByDisplayValue('10.5pt')
    expect(sizeInputs.length).toBeGreaterThanOrEqual(1)
    const input = sizeInputs[0]
    await userEvent.clear(input)
    await userEvent.type(input, '12pt')
    expect(mockUpdateCustomVariable).toHaveBeenCalled()
  })

  it('renders reset button', () => {
    render(<ThemeCustomizer />)
    expect(screen.getByText('Reset')).toBeTruthy()
  })

  it('calls resetCustomVariables on reset click', async () => {
    render(<ThemeCustomizer />)
    await userEvent.click(screen.getByText('Reset'))
    expect(mockResetCustomVariables).toHaveBeenCalledOnce()
  })

})
