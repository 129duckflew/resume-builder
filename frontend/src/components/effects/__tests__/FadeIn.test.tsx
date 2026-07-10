import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import { FadeIn } from '@/components/effects/FadeIn'

describe('FadeIn', () => {
  it('renders children with fade-in-up animation', () => {
    render(<FadeIn><span>Hello</span></FadeIn>)

    const el = screen.getByText('Hello').parentElement!
    expect(el.className).toContain('animate-fade-in-up')
  })

  it('disables animation when prefers-reduced-motion', () => {
    render(<FadeIn><span>World</span></FadeIn>)

    const el = screen.getByText('World').parentElement!
    expect(el.className).toContain('motion-reduce:animate-none')
  })
})
