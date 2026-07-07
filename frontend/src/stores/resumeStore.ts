import { create } from 'zustand'
import type { Resume, Theme } from '@/types/resume'
import { resumeApi, themeApi } from '@/lib/api'

interface ResumeState {
  resumes: Resume[]
  currentResume: Resume | null
  themes: Theme[]
  currentThemeCss: string
  loading: boolean
  error: string | null

  fetchResumes: () => Promise<void>
  fetchResume: (id: string) => Promise<void>
  createResume: (title: string) => Promise<Resume>
  updateResume: (id: string, data: Partial<Resume>) => Promise<void>
  deleteResume: (id: string) => Promise<void>
  fetchThemes: () => Promise<void>
  setTheme: (themeId: string) => Promise<void>
  setContent: (content: string) => void
  setTitle: (title: string) => void
}

export const useResumeStore = create<ResumeState>((set, get) => ({
  resumes: [],
  currentResume: null,
  themes: [],
  currentThemeCss: '',
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
      const css = await themeApi.getCss(resume.themeId)
      set({ currentThemeCss: css })
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

  fetchThemes: async () => {
    const themes = await themeApi.list()
    set({ themes })
  },

  setTheme: async (themeId: string) => {
    const current = get().currentResume
    if (!current) return
    const css = await themeApi.getCss(themeId)
    set({ currentThemeCss: css })
    await get().updateResume(current.id, { themeId })
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
