import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import VersionDiff from '@/components/editor/VersionDiff'
import type { VersionDiffResponse, ResumeVersion } from '@/types/resume'

const { mockToast } = vi.hoisted(() => ({
  mockToast: vi.fn(),
}))

const mockVersions: ResumeVersion[] = [
  {
    id: 1, resumeId: 'r1', versionNumber: 1, title: 'Initial', content: '# A',
    themeId: 'classic', fontSize: null, lineHeight: null, sectionSpacing: 'normal',
    createdAt: '2026-01-01T00:00:00Z',
  },
  {
    id: 2, resumeId: 'r1', versionNumber: 2, title: 'Updated', content: '# B',
    themeId: 'classic', fontSize: null, lineHeight: null, sectionSpacing: 'normal',
    createdAt: '2026-01-02T00:00:00Z',
  },
]

const mockDiffResponse: VersionDiffResponse = {
  versionA: { versionNumber: 1, title: 'Initial', createdAt: '2026-01-01T00:00:00Z' },
  versionB: { versionNumber: 2, title: 'Updated', createdAt: '2026-01-02T00:00:00Z' },
  hunks: [
    {
      oldStart: 1, oldCount: 1, newStart: 1, newCount: 1,
      lines: [
        { type: 'REMOVED', text: '# A' },
        { type: 'ADDED', text: '# B' },
      ],
    },
  ],
}

vi.mock('@/hooks/use-toast', () => ({
  toast: mockToast,
}))

vi.mock('@/lib/api', () => {
  const mockList = vi.fn()
  const mockDiff = vi.fn()
  return {
    versionApi: {
      list: mockList,
      diff: mockDiff,
    },
  }
})

import { versionApi } from '@/lib/api'

function selectVersion(selectIndex: number, value: string) {
  const selects = document.querySelectorAll('select')
  fireEvent.change(selects[selectIndex], { target: { value } })
}

describe('VersionDiff', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockToast.mockClear()
    vi.mocked(versionApi.list).mockResolvedValue(mockVersions)
    vi.mocked(versionApi.diff).mockResolvedValue(mockDiffResponse)
  })

  it('shows version selectors and compare button', async () => {
    render(<VersionDiff resumeId="r1" open={true} onClose={() => {}} />)
    await screen.findByText('Compare')
    const selects = document.querySelectorAll('select')
    expect(selects.length).toBe(2)
  })

  it('calls diff API and renders result', async () => {
    render(<VersionDiff resumeId="r1" open={true} onClose={() => {}} />)
    await screen.findByText('Compare')

    selectVersion(0, '2')
    selectVersion(1, '1')
    fireEvent.click(screen.getByText('Compare'))

    expect(versionApi.diff).toHaveBeenCalledWith('r1', 2, 1)
    await screen.findByText('# A')
    await screen.findByText('# B')
  })

  it('shows added lines with green background', async () => {
    render(<VersionDiff resumeId="r1" open={true} onClose={() => {}} />)
    await screen.findByText('Compare')

    selectVersion(0, '2')
    selectVersion(1, '1')
    fireEvent.click(screen.getByText('Compare'))

    await screen.findByText('# B')
    const addedLine = screen.getByText('# B').closest('div')
    expect(addedLine).toBeTruthy()
    expect(addedLine?.className).toContain('bg-green')
  })

  it('shows removed lines with red background', async () => {
    render(<VersionDiff resumeId="r1" open={true} onClose={() => {}} />)
    await screen.findByText('Compare')

    selectVersion(0, '2')
    selectVersion(1, '1')
    fireEvent.click(screen.getByText('Compare'))

    await screen.findByText('# A')
    const removedLine = screen.getByText('# A').closest('div')
    expect(removedLine).toBeTruthy()
    expect(removedLine?.className).toContain('bg-red')
  })

  it('shows error toast when diff fails', async () => {
    vi.mocked(versionApi.diff).mockRejectedValue(new Error('Network error'))

    render(<VersionDiff resumeId="r1" open={true} onClose={() => {}} />)
    await screen.findByText('Compare')

    selectVersion(0, '2')
    selectVersion(1, '1')
    fireEvent.click(screen.getByText('Compare'))

    await screen.findByText('Select two versions and click Compare to see the diff.')
    expect(mockToast).toHaveBeenCalledWith(
      expect.objectContaining({ variant: 'destructive' })
    )
  })

  it('shows toast when same version selected in both dropdowns', async () => {
    render(<VersionDiff resumeId="r1" open={true} onClose={() => {}} />)
    await screen.findByText('Compare')

    selectVersion(0, '2')
    selectVersion(1, '2')
    fireEvent.click(screen.getByText('Compare'))

    expect(mockToast).toHaveBeenCalledWith(
      expect.objectContaining({ title: 'Please select two different versions' })
    )
  })

  it('shows empty state when no versions exist', async () => {
    vi.mocked(versionApi.list).mockResolvedValue([])

    render(<VersionDiff resumeId="r1" open={true} onClose={() => {}} />)
    await screen.findByText(/no versions/i)
  })
})
