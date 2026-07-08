import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ExportPanel from '@/components/editor/ExportPanel'

const { mockExportPdf, mockExportHtml, mockToast } = vi.hoisted(() => ({
  mockExportPdf: vi.fn().mockResolvedValue(undefined),
  mockExportHtml: vi.fn().mockResolvedValue(undefined),
  mockToast: vi.fn(),
}))

const mockResume = {
  id: 'test-id',
  title: 'Test',
  content: '# Hello',
  themeId: 'classic',
  fontSize: null,
  lineHeight: null,
  sectionSpacing: 'normal',
  createdAt: '',
  updatedAt: '',
}

vi.mock('@/lib/api', () => ({
  resumeApi: {
    exportPdf: mockExportPdf,
    exportHtml: mockExportHtml,
  },
}))

vi.mock('@/hooks/use-toast', () => ({
  useToast: () => ({ toast: mockToast }),
  toast: mockToast,
}))

vi.mock('@/stores/resumeStore', () => ({
  useResumeStore: vi.fn((selector?: any) => {
    const state = { currentResume: mockResume }
    return selector ? selector(state) : state
  }),
}))

describe('ExportPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders PDF and HTML buttons', () => {
    render(<ExportPanel smartOnePage={true} onSmartOnePageChange={() => {}} />)
    expect(screen.getByText('PDF')).toBeTruthy()
    expect(screen.getByText('HTML')).toBeTruthy()
  })

  it('smart one-page toggle defaults to checked', () => {
    render(<ExportPanel smartOnePage={true} onSmartOnePageChange={() => {}} />)
    const checkbox = screen.getByRole('checkbox') as HTMLInputElement
    expect(checkbox.checked).toBe(true)
  })

  it('allows toggling smart one-page off', async () => {
    const onChange = vi.fn()
    render(<ExportPanel smartOnePage={true} onSmartOnePageChange={onChange} />)
    const checkbox = screen.getByRole('checkbox') as HTMLInputElement
    await userEvent.click(checkbox)
    expect(onChange).toHaveBeenCalledWith(false)
  })

  it('passes smartOnePage=true when exporting PDF with toggle on', async () => {
    render(<ExportPanel smartOnePage={true} onSmartOnePageChange={() => {}} />)
    await userEvent.click(screen.getByText('PDF'))
    expect(mockExportPdf).toHaveBeenCalledWith('test-id', true)
  })

  it('passes smartOnePage=false when exporting PDF with toggle off', async () => {
    render(<ExportPanel smartOnePage={false} onSmartOnePageChange={() => {}} />)
    await userEvent.click(screen.getByText('PDF'))
    expect(mockExportPdf).toHaveBeenCalledWith('test-id', false)
  })

  it('calls exportHtml for HTML button', async () => {
    render(<ExportPanel smartOnePage={true} onSmartOnePageChange={() => {}} />)
    await userEvent.click(screen.getByText('HTML'))
    expect(mockExportHtml).toHaveBeenCalledWith('test-id', true)
  })
})
