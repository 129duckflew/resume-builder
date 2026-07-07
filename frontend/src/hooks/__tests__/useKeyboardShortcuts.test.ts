import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook } from '@testing-library/react'
import { useKeyboardShortcuts } from '@/hooks/useKeyboardShortcuts'
import * as historyStoreModule from '@/stores/historyStore'
import * as resumeStoreModule from '@/stores/resumeStore'

vi.mock('@/stores/resumeStore', () => ({
  useResumeStore: vi.fn(),
}))

vi.mock('@/stores/historyStore', () => ({
  useHistoryStore: vi.fn(),
}))

describe('useKeyboardShortcuts', () => {
  const mockOnSave = vi.fn()
  const mockUndo = vi.fn()
  const mockRedo = vi.fn()
  const mockSetContent = vi.fn()

  const resumeState = {
    currentResume: { id: '1', content: 'current' },
    setContent: mockSetContent,
  }

  const historyState = {
    undo: mockUndo,
    redo: mockRedo,
  }

  beforeEach(() => {
    vi.clearAllMocks()
    ;(resumeStoreModule.useResumeStore as any).mockImplementation(
      (selector: (s: typeof resumeState) => any) => selector(resumeState),
    )
    ;(historyStoreModule.useHistoryStore as any).mockImplementation(
      (selector: (s: typeof historyState) => any) => selector(historyState),
    )
  })

  it('calls onSave on Cmd+S', () => {
    renderHook(() => useKeyboardShortcuts({ onSave: mockOnSave }))

    window.dispatchEvent(new KeyboardEvent('keydown', {
      key: 's',
      metaKey: true,
      bubbles: true,
    }))

    expect(mockOnSave).toHaveBeenCalledTimes(1)
  })

  it('calls undo on Cmd+Z', () => {
    mockUndo.mockReturnValue('previous')
    renderHook(() => useKeyboardShortcuts({ onSave: mockOnSave }))

    window.dispatchEvent(new KeyboardEvent('keydown', {
      key: 'z',
      metaKey: true,
      bubbles: true,
    }))

    expect(mockUndo).toHaveBeenCalledTimes(1)
    expect(mockSetContent).toHaveBeenCalledWith('previous')
  })

  it('calls redo on Cmd+Shift+Z', () => {
    mockRedo.mockReturnValue('next')
    renderHook(() => useKeyboardShortcuts({ onSave: mockOnSave }))

    window.dispatchEvent(new KeyboardEvent('keydown', {
      key: 'z',
      metaKey: true,
      shiftKey: true,
      bubbles: true,
    }))

    expect(mockRedo).toHaveBeenCalledTimes(1)
    expect(mockSetContent).toHaveBeenCalledWith('next')
  })
})
