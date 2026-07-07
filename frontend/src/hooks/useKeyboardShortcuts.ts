import { useEffect } from 'react'
import { useHistoryStore } from '@/stores/historyStore'
import { useResumeStore } from '@/stores/resumeStore'

interface ShortcutHandlers {
  onSave: () => void
}

export function useKeyboardShortcuts({ onSave }: ShortcutHandlers) {
  const currentResume = useResumeStore((s) => s.currentResume)
  const setContent = useResumeStore((s) => s.setContent)
  const undo = useHistoryStore((s) => s.undo)
  const redo = useHistoryStore((s) => s.redo)

  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      const isMod = e.metaKey || e.ctrlKey

      if (isMod && e.key === 's') {
        e.preventDefault()
        onSave()
        return
      }

      if (isMod && e.key === 'z' && !e.shiftKey) {
        e.preventDefault()
        if (!currentResume) return
        const previous = undo()
        if (previous !== null) {
          setContent(previous)
        }
        return
      }

      if (isMod && e.key === 'z' && e.shiftKey) {
        e.preventDefault()
        if (!currentResume) return
        const next = redo()
        if (next !== null) {
          setContent(next)
        }
        return
      }
    }

    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [onSave, currentResume, undo, redo, setContent])
}
