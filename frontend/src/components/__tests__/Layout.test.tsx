import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import Layout from '@/components/Layout'

vi.mock('@/stores/resumeStore', () => ({
  useResumeStore: vi.fn((selector?: any) => {
    const state = { createResume: vi.fn().mockResolvedValue({ id: '1' }) }
    return selector ? selector(state) : state
  }),
}))

vi.mock('@/hooks/use-toast', () => ({
  useToast: () => ({ toasts: [], toast: vi.fn(), dismiss: vi.fn() }),
}))

describe('Layout', () => {
  it('renders app title', () => {
    render(
      <BrowserRouter>
        <Layout />
      </BrowserRouter>,
    )
    expect(screen.getByText('Resume Builder')).toBeInTheDocument()
  })

  it('renders new resume button', () => {
    render(
      <BrowserRouter>
        <Layout />
      </BrowserRouter>,
    )
    expect(screen.getByText('New Resume')).toBeInTheDocument()
  })
})
