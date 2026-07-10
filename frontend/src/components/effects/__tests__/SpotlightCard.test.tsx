import { describe, expect, it } from 'vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import { SpotlightCard } from '@/components/effects/SpotlightCard'

describe('SpotlightCard', () => {
  it('renders children inside a spotlight card', () => {
    render(<SpotlightCard>Write</SpotlightCard>)

    expect(screen.getByText('Write').closest('[data-slot="spotlight-card"]')).toBeTruthy()
  })

  it('updates spotlight coordinates on pointer movement', () => {
    render(<SpotlightCard>Style</SpotlightCard>)

    const card = screen.getByText('Style').closest('[data-slot="spotlight-card"]') as HTMLElement
    fireEvent.mouseMove(card, { clientX: 24, clientY: 36 })

    expect(card.style.getPropertyValue('--spotlight-x')).toBe('24px')
    expect(card.style.getPropertyValue('--spotlight-y')).toBe('36px')
  })
})
