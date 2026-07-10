import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import type { Resume } from '@/types/resume'

const { mockToast, mockDeleteResume, mockCreateResume, mockFetchResumes, mockResumes } = vi.hoisted(() => ({
  mockToast: vi.fn(),
  mockDeleteResume: vi.fn().mockResolvedValue(undefined),
  mockCreateResume: vi.fn(),
  mockFetchResumes: vi.fn(),
  mockResumes: [] as Resume[],
}))

vi.mock('@/hooks/use-toast', () => ({
  toast: mockToast,
}))

vi.mock('@/stores/resumeStore', () => ({
  useResumeStore: () => ({
    resumes: mockResumes,
    loading: false,
    fetchResumes: mockFetchResumes,
    deleteResume: mockDeleteResume,
    createResume: mockCreateResume,
  }),
}))

vi.mock('@/lib/api', () => ({
  jsonResumeApi: { importJson: vi.fn() },
}))

import HomePage from '@/pages/HomePage'

function renderHomePage() {
  return render(
    <MemoryRouter>
      <HomePage />
    </MemoryRouter>,
  )
}

describe('HomePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockResumes.splice(0, mockResumes.length,
      {
        id: '1',
        title: 'My Resume',
        content: '# Hello',
        themeId: 'classic',
        fontSize: null,
        lineHeight: null,
        sectionSpacing: 'normal',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-02',
      },
      {
        id: '2',
        title: 'Another Resume',
        content: '# World',
        themeId: 'modern',
        fontSize: null,
        lineHeight: null,
        sectionSpacing: 'normal',
        createdAt: '2024-02-01',
        updatedAt: '2024-02-02',
      },
    )
  })

  it('renders resume list', () => {
    renderHomePage()
    expect(screen.getByText('My Resume')).toBeTruthy()
    expect(screen.getByText('Another Resume')).toBeTruthy()
  })

  it('renders delete button on each resume card', () => {
    renderHomePage()
    const deleteButtons = screen.getAllByTitle('Delete')
    expect(deleteButtons).toHaveLength(2)
  })

  it('opens confirm dialog when delete button clicked', async () => {
    renderHomePage()
    const deleteButtons = screen.getAllByTitle('Delete')
    await userEvent.click(deleteButtons[0])
    expect(screen.getByText('Delete Resume')).toBeTruthy()
    expect(screen.getByText(/this action cannot be undone/i)).toBeTruthy()
    expect(screen.getByText(/delete "My Resume"/)).toBeTruthy()
  })

  it('closes dialog when cancel clicked', async () => {
    renderHomePage()
    await userEvent.click(screen.getAllByTitle('Delete')[0])
    expect(screen.getByText('Delete Resume')).toBeTruthy()
    await userEvent.click(screen.getByText('Cancel'))
    expect(screen.queryByText('Delete Resume')).toBeNull()
  })

  it('calls deleteResume and shows toast on confirm', async () => {
    renderHomePage()
    await userEvent.click(screen.getAllByTitle('Delete')[0])
    await userEvent.click(screen.getByText('Delete'))
    expect(mockDeleteResume).toHaveBeenCalledWith('1')
    await vi.waitFor(() => {
      expect(mockToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: 'Resume deleted' }),
      )
    })
  })

  it('loads resume list on mount', () => {
    renderHomePage()
    expect(mockFetchResumes).toHaveBeenCalled()
  })

  it('shows error toast when deletion fails', async () => {
    mockDeleteResume.mockRejectedValueOnce(new Error('Network error'))
    renderHomePage()
    await userEvent.click(screen.getAllByTitle('Delete')[0])
    await userEvent.click(screen.getByText('Delete'))
    await vi.waitFor(() => {
      expect(mockToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: 'Delete failed' }),
      )
    })
  })

  it('renders empty-state steps as spotlight cards', () => {
    mockResumes.splice(0, mockResumes.length)

    renderHomePage()

    expect(screen.getByText('1. Write').closest('[data-slot="spotlight-card"]')).toBeTruthy()
    expect(screen.getByText('2. Style').closest('[data-slot="spotlight-card"]')).toBeTruthy()
    expect(screen.getByText('3. Export').closest('[data-slot="spotlight-card"]')).toBeTruthy()
  })
})
