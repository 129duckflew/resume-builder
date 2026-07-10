import { describe, expect, it } from 'vitest'
import { render } from '@testing-library/react'
import { Skeleton } from '@/components/ui/skeleton'

describe('Skeleton', () => {
  it('renders with pulse animation class', () => {
    const { container } = render(<Skeleton data-testid="skeleton" />)

    const el = container.firstChild as HTMLElement
    expect(el.className).toContain('animate-pulse')
    expect(el.className).toContain('rounded-md')
    expect(el.className).toContain('bg-muted/50')
  })

  it('accepts custom className', () => {
    const { container } = render(<Skeleton className="h-10 w-48" />)

    const el = container.firstChild as HTMLElement
    expect(el.className).toContain('h-10')
    expect(el.className).toContain('w-48')
  })
})
