import { describe, it, expect, beforeEach } from 'vitest'
import { useHistoryStore } from '@/stores/historyStore'

describe('historyStore', () => {
  beforeEach(() => {
    useHistoryStore.setState({ past: [], future: [] })
  })

  it('pushState adds content to past', () => {
    useHistoryStore.getState().pushState('v1')
    expect(useHistoryStore.getState().past).toEqual(['v1'])
  })

  it('pushState ignores duplicate consecutive content', () => {
    useHistoryStore.getState().pushState('v1')
    useHistoryStore.getState().pushState('v1')
    expect(useHistoryStore.getState().past).toEqual(['v1'])
  })

  it('undo returns previous content and moves current to future', () => {
    useHistoryStore.getState().pushState('v1')
    useHistoryStore.getState().pushState('v2')
    const result = useHistoryStore.getState().undo()
    expect(result).toBe('v1')
    expect(useHistoryStore.getState().past).toEqual(['v1'])
    expect(useHistoryStore.getState().future).toEqual(['v2'])
  })

  it('undo returns null when no history', () => {
    expect(useHistoryStore.getState().undo()).toBeNull()
  })

  it('redo returns next content', () => {
    useHistoryStore.getState().pushState('v1')
    useHistoryStore.getState().pushState('v2')
    useHistoryStore.getState().undo()
    const result = useHistoryStore.getState().redo()
    expect(result).toBe('v2')
    expect(useHistoryStore.getState().past).toEqual(['v1', 'v2'])
    expect(useHistoryStore.getState().future).toEqual([])
  })

  it('redo returns null when no future', () => {
    expect(useHistoryStore.getState().redo()).toBeNull()
  })

  it('pushState clears future on new content', () => {
    useHistoryStore.getState().pushState('v1')
    useHistoryStore.getState().pushState('v2')
    useHistoryStore.getState().undo()
    useHistoryStore.getState().pushState('v3')
    expect(useHistoryStore.getState().past).toEqual(['v1', 'v3'])
    expect(useHistoryStore.getState().future).toEqual([])
  })

  it('canUndo returns true when past has multiple items', () => {
    expect(useHistoryStore.getState().canUndo()).toBe(false)
    useHistoryStore.getState().pushState('v1')
    expect(useHistoryStore.getState().canUndo()).toBe(false)
    useHistoryStore.getState().pushState('v2')
    expect(useHistoryStore.getState().canUndo()).toBe(true)
  })

  it('canRedo returns true when future has items', () => {
    expect(useHistoryStore.getState().canRedo()).toBe(false)
    useHistoryStore.getState().pushState('v1')
    useHistoryStore.getState().pushState('v2')
    useHistoryStore.getState().undo()
    expect(useHistoryStore.getState().canRedo()).toBe(true)
  })

  it('reset sets initial state', () => {
    useHistoryStore.getState().pushState('v1')
    useHistoryStore.getState().pushState('v2')
    useHistoryStore.getState().reset('new')
    expect(useHistoryStore.getState().past).toEqual(['new'])
    expect(useHistoryStore.getState().future).toEqual([])
    expect(useHistoryStore.getState().canUndo()).toBe(false)
  })
})
