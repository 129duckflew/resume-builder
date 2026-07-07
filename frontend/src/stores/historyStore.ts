import { create } from 'zustand'

const MAX_HISTORY = 50

interface HistoryState {
  past: string[]
  future: string[]
  pushState: (content: string) => void
  undo: () => string | null
  redo: () => string | null
  canUndo: () => boolean
  canRedo: () => boolean
  reset: (content: string) => void
}

export const useHistoryStore = create<HistoryState>((set, get) => ({
  past: [],
  future: [],

  pushState: (content: string) => {
    const { past } = get()
    if (past[past.length - 1] === content) return
    set({
      past: [...past.slice(-MAX_HISTORY + 1), content],
      future: [],
    })
  },

  undo: () => {
    const { past } = get()
    if (past.length < 2) return null
    const current = past[past.length - 1]
    const previous = past[past.length - 2]
    set((state) => ({
      past: state.past.slice(0, -1),
      future: [current, ...state.future],
    }))
    return previous
  },

  redo: () => {
    const { past, future } = get()
    if (future.length === 0) return null
    const next = future[0]
    set({
      past: [...past, next],
      future: future.slice(1),
    })
    return next
  },

  canUndo: () => get().past.length >= 2,

  canRedo: () => get().future.length > 0,

  reset: (content: string) => {
    set({ past: [content], future: [] })
  },
}))
