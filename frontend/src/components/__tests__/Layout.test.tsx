import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import Layout from '@/components/Layout'

vi.mock('@/stores/resumeStore', () => ({
  useResumeStore: vi.fn(() => ({
    createResume: vi.fn().mockResolvedValue({ id: '1' }),
  })),
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
