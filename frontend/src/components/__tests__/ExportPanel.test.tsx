import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ExportPanel from '@/components/editor/ExportPanel'

const { mockExportPdf, mockExportHtml, mockToast, mockJsonExport, mockSaveBlob } = vi.hoisted(() => ({
  mockExportPdf: vi.fn().mockResolvedValue(true),
  mockExportHtml: vi.fn().mockResolvedValue(true),
  mockToast: vi.fn(),
  mockJsonExport: vi.fn().mockResolvedValue({ basics: { name: 'Test' } }),
  mockSaveBlob: vi.fn().mockResolvedValue(true),
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
  jsonResumeApi: {
    exportJson: mockJsonExport,
  },
  saveBlobToDisk: mockSaveBlob,
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

const defaultProps = {
  smartOnePage: true,
  onSmartOnePageChange: () => {},
  desensitize: false,
  onDesensitizeChange: () => {},
}

describe('ExportPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders PDF and HTML buttons', () => {
    render(<ExportPanel {...defaultProps} />)
    expect(screen.getByText('PDF')).toBeTruthy()
    expect(screen.getByText('HTML')).toBeTruthy()
  })

  it('renders desensitize toggle and settings button', () => {
    render(<ExportPanel {...defaultProps} />)
    expect(screen.getByText('Desensitize')).toBeTruthy()
  })

  it('smart one-page toggle defaults to checked', () => {
    render(<ExportPanel {...defaultProps} />)
    const checkboxes = screen.getAllByRole('checkbox') as HTMLInputElement[]
    const smartCheckbox = checkboxes.find(c => c.checked === true)
    expect(smartCheckbox).toBeTruthy()
  })

  it('allows toggling smart one-page off', async () => {
    const onChange = vi.fn()
    render(<ExportPanel {...defaultProps} onSmartOnePageChange={onChange} />)
    const checkboxes = screen.getAllByRole('checkbox') as HTMLInputElement[]
    const smartCheckbox = checkboxes.find(c => c.checked === true)
    await userEvent.click(smartCheckbox!)
    expect(onChange).toHaveBeenCalledWith(false)
  })

  it('passes smartOnePage=true and desensitize=false when exporting PDF with both toggles default', async () => {
    render(<ExportPanel {...defaultProps} />)
    await userEvent.click(screen.getByText('PDF'))
    expect(mockExportPdf).toHaveBeenCalledWith('test-id', true, false)
  })

  it('passes smartOnePage=false when exporting PDF with toggle off', async () => {
    render(<ExportPanel {...defaultProps} smartOnePage={false} />)
    await userEvent.click(screen.getByText('PDF'))
    expect(mockExportPdf).toHaveBeenCalledWith('test-id', false, false)
  })

  it('calls exportHtml for HTML button', async () => {
    render(<ExportPanel {...defaultProps} />)
    await userEvent.click(screen.getByText('HTML'))
    expect(mockExportHtml).toHaveBeenCalledWith('test-id', true, false)
  })

  it('toggles desensitize on and passes it to export', async () => {
    const onDesensitizeChange = vi.fn()
    render(<ExportPanel {...defaultProps} desensitize={false} onDesensitizeChange={onDesensitizeChange} />)
    const desensitizeCheckbox = screen.getByLabelText('Desensitize') as HTMLInputElement
    await userEvent.click(desensitizeCheckbox)
    expect(onDesensitizeChange).toHaveBeenCalledWith(true)
  })

  it('does not toast success when the save dialog is cancelled', async () => {
    mockExportPdf.mockResolvedValueOnce(false)
    render(<ExportPanel {...defaultProps} />)
    await userEvent.click(screen.getByText('PDF'))
    expect(mockExportPdf).toHaveBeenCalledWith('test-id', true, false)
    expect(mockToast).not.toHaveBeenCalled()
  })

  it('exports JSON via save dialog', async () => {
    render(<ExportPanel {...defaultProps} />)
    await userEvent.click(screen.getByText('JSON'))
    expect(mockJsonExport).toHaveBeenCalledWith('test-id')
    expect(mockSaveBlob).toHaveBeenCalledWith(expect.any(Blob), 'resume.json', 'application/json')
    expect(mockToast).toHaveBeenCalledWith({ title: 'JSON exported', variant: 'success' })
  })
})
