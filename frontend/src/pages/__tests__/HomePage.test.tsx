import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import type { Resume } from '@/types/resume'

const { mockToast, mockDeleteResume, mockCreateResume, mockFetchResumes } = vi.hoisted(() => ({
  mockToast: vi.fn(),
  mockDeleteResume: vi.fn().mockResolvedValue(undefined),
  mockCreateResume: vi.fn(),
  mockFetchResumes: vi.fn(),
}))

vi.mock('@/hooks/use-toast', () => ({
  toast: mockToast,
}))

vi.mock('@/stores/resumeStore', () => ({
  useResumeStore: () => ({
    resumes: [
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
    ],
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
})
