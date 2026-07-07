import { useEffect, useRef } from 'react'
import { useResumeStore } from '@/stores/resumeStore'

const DRAFT_PREFIX = 'resume-draft-'

export function useDraftBackup() {
  const currentResume = useResumeStore((s) => s.currentResume)
  const lastSaved = useRef<string>('')

  // Auto-save draft to localStorage on content change
  useEffect(() => {
    if (!currentResume) return
    if (currentResume.content === lastSaved.current) return
    lastSaved.current = currentResume.content
    try {
      localStorage.setItem(DRAFT_PREFIX + currentResume.id, currentResume.content)
    } catch {
      // localStorage quota exceeded, ignore
    }
  }, [currentResume?.content, currentResume?.id])

  return {
    getDraft: (id: string): string | null => {
      try {
        return localStorage.getItem(DRAFT_PREFIX + id)
      } catch {
        return null
      }
    },
    clearDraft: (id: string) => {
      try {
        localStorage.removeItem(DRAFT_PREFIX + id)
      } catch {
        // ignore
      }
    },
  }
}
