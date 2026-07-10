import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import { GradientText } from '@/components/effects/GradientText'

describe('GradientText', () => {
  it('renders text with gradient styling', () => {
    render(<GradientText>My Resumes</GradientText>)

    const text = screen.getByText('My Resumes')
    expect(text).toHaveClass('bg-clip-text')
    expect(text).toHaveClass('text-transparent')
  })
})
