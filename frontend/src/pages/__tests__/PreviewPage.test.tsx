import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, act } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return { ...actual as any, useParams: () => ({ id: 'test-id' }) }
})

const { mockPreview, mockExportPdf, mockExportHtml } = vi.hoisted(() => ({
  mockPreview: vi.fn(),
  mockExportPdf: vi.fn().mockResolvedValue(undefined),
  mockExportHtml: vi.fn().mockResolvedValue(undefined),
}))

vi.mock('@/lib/api', () => ({
  resumeApi: {
    preview: mockPreview,
    exportPdf: mockExportPdf,
    exportHtml: mockExportHtml,
  },
}))

import PreviewPage from '@/pages/PreviewPage'

function PreviewPageApp() {
  return (
    <MemoryRouter initialEntries={['/preview/test-id']}>
      <Routes>
        <Route path="/preview/:id" element={<PreviewPage />} />
      </Routes>
    </MemoryRouter>
  )
}

describe('PreviewPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('shows loading state on mount', () => {
    mockPreview.mockResolvedValue('<h1>Hello</h1>')
    render(<PreviewPageApp />)
    expect(screen.getByText('Loading preview...')).toBeTruthy()
  })

  it('calls preview API with smartOnePage=true on mount', async () => {
    mockPreview.mockResolvedValue('<h1>Hello</h1>')
    render(<PreviewPageApp />)
    await vi.waitFor(() => {
      expect(mockPreview).toHaveBeenCalledWith('test-id', true)
    })
  })

  it('renders iframe with HTML content when preview succeeds', async () => {
    mockPreview.mockResolvedValue('<h1>Hello</h1>')
    render(<PreviewPageApp />)

    await vi.waitFor(() => {
      expect(screen.queryByText('Loading preview...')).toBeNull()
    })

    const iframe = document.querySelector('iframe')
    expect(iframe).toBeTruthy()
    expect(iframe?.title).toBe('Resume Preview')
  })

  it('shows error banner when preview API fails', async () => {
    mockPreview.mockRejectedValue({
      response: { data: { error: 'Preview generation failed' } },
    })
    render(<PreviewPageApp />)

    await vi.waitFor(() => {
      expect(screen.getByText('Preview generation failed')).toBeTruthy()
    })
  })

  it('shows fallback error message when no error detail', async () => {
    mockPreview.mockRejectedValue(new Error('Network Error'))
    render(<PreviewPageApp />)

    await vi.waitFor(() => {
      expect(screen.getByText('Preview generation failed')).toBeTruthy()
    })
  })

  it('toggling smart one-page re-fetches preview', async () => {
    mockPreview.mockResolvedValue('<h1>Hello</h1>')
    render(<PreviewPageApp />)

    await vi.waitFor(() => {
      expect(screen.queryByText('Loading preview...')).toBeNull()
    })

    mockPreview.mockResolvedValue('<h1>World</h1>')
    const checkbox = screen.getByRole('checkbox') as HTMLInputElement
    await userEvent.click(checkbox)

    await vi.waitFor(() => {
      expect(mockPreview).toHaveBeenCalledTimes(2)
    })
    expect(mockPreview).toHaveBeenCalledWith('test-id', false)
  })

  it('Download PDF button calls exportPdf with smartOnePage=true', async () => {
    mockPreview.mockResolvedValue('<h1>Hello</h1>')
    render(<PreviewPageApp />)

    await vi.waitFor(() => {
      expect(screen.queryByText('Loading preview...')).toBeNull()
    })

    await userEvent.click(screen.getByText('Download PDF'))
    expect(mockExportPdf).toHaveBeenCalledWith('test-id', true)
  })

  it('Download HTML button calls exportHtml with smartOnePage=true', async () => {
    mockPreview.mockResolvedValue('<h1>Hello</h1>')
    render(<PreviewPageApp />)

    await vi.waitFor(() => {
      expect(screen.queryByText('Loading preview...')).toBeNull()
    })

    await userEvent.click(screen.getByText('Download HTML'))
    expect(mockExportHtml).toHaveBeenCalledWith('test-id', true)
  })

  it('renders Back to Editor button', async () => {
    mockPreview.mockResolvedValue('<h1>Hello</h1>')
    render(<PreviewPageApp />)

    await vi.waitFor(() => {
      expect(screen.queryByText('Loading preview...')).toBeNull()
    })

    expect(screen.getByText('Back to Editor')).toBeTruthy()
  })
})
