import { create } from 'zustand'
import type { Resume } from '@/types/resume'
import { resumeApi, themeApi, styleApi } from '@/lib/api'
import { useThemeStore } from '@/stores/themeStore'

interface ResumeState {
  resumes: Resume[]
  currentResume: Resume | null
  loading: boolean
  error: string | null

  fetchResumes: () => Promise<void>
  fetchResume: (id: string) => Promise<void>
  createResume: (title: string) => Promise<Resume>
  updateResume: (id: string, data: Partial<Resume>) => Promise<void>
  deleteResume: (id: string) => Promise<void>
  setContent: (content: string) => void
  setTitle: (title: string) => void
}

export const useResumeStore = create<ResumeState>((set, get) => ({
  resumes: [],
  currentResume: null,
  loading: false,
  error: null,

  fetchResumes: async () => {
    set({ loading: true, error: null })
    try {
      const resumes = await resumeApi.list()
      set({ resumes, loading: false })
    } catch {
      set({ error: 'Failed to load resumes', loading: false })
    }
  },

  fetchResume: async (id: string) => {
    set({ loading: true, error: null })
    try {
      const resume = await resumeApi.get(id)
      set({ currentResume: resume, loading: false })
      const themeStore = useThemeStore.getState()
      const css = await themeApi.getCss(resume.themeId)
      useThemeStore.setState({ currentThemeCss: css })
      set({ currentResume: resume, loading: false })
      await themeStore.fetchThemeVariables(resume.themeId)
      try {
        const savedStyle = await styleApi.getStyle(id, resume.themeId)
        if (savedStyle) {
          themeStore.applyStyle(savedStyle)
        }
      } catch {
        // 204 No Content — no saved style, ignore
      }
    } catch {
      set({ error: 'Failed to load resume', loading: false })
    }
  },

  createResume: async (title: string) => {
    const resume = await resumeApi.create({ title })
    await get().fetchResumes()
    return resume
  },

  updateResume: async (id: string, data: Partial<Resume>) => {
    const updated = await resumeApi.update(id, data)
    set({ currentResume: updated })
  },

  deleteResume: async (id: string) => {
    await resumeApi.delete(id)
    await get().fetchResumes()
  },

  setContent: (content: string) => {
    const current = get().currentResume
    if (!current) return
    set({ currentResume: { ...current, content } })
  },

  setTitle: (title: string) => {
    const current = get().currentResume
    if (!current) return
    set({ currentResume: { ...current, title } })
  },
}))
