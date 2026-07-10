import { describe, expect, it, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { Button } from '@/components/ui/button'

describe('Button', () => {
  it('renders shimmer variant with overflow-hidden class', () => {
    render(<Button variant="shimmer">Shine</Button>)

    const btn = screen.getByRole('button', { name: 'Shine' })
    expect(btn.className).toContain('overflow-hidden')
  })

  it('injects a ripple span on click', () => {
    render(<Button>Click Me</Button>)

    const btn = screen.getByRole('button', { name: 'Click Me' })
    fireEvent.click(btn)

    const ripples = btn.querySelectorAll('[data-ripple]')
    expect(ripples.length).toBe(1)
  })

  it('removes ripple span after timeout', async () => {
    vi.useFakeTimers()
    render(<Button>Click Me</Button>)

    const btn = screen.getByRole('button', { name: 'Click Me' })
    fireEvent.click(btn)
    expect(btn.querySelectorAll('[data-ripple]').length).toBe(1)

    vi.advanceTimersByTime(700)

    await vi.waitFor(() => {
      expect(btn.querySelectorAll('[data-ripple]').length).toBe(0)
    })

    vi.useRealTimers()
  })

  it('respects prefers-reduced-motion and does not create ripple', () => {
    const matchMedia = window.matchMedia
    window.matchMedia = vi.fn().mockImplementation((query: string) => ({
      matches: query === '(prefers-reduced-motion: reduce)',
      media: query,
      onchange: null,
      addListener: vi.fn(),
      removeListener: vi.fn(),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn(),
    }))

    render(<Button>Click Me</Button>)
    const btn = screen.getByRole('button', { name: 'Click Me' })
    fireEvent.click(btn)

    expect(btn.querySelectorAll('[data-ripple]').length).toBe(0)

    window.matchMedia = matchMedia
  })
})
