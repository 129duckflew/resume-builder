import { create } from 'zustand'
import type { Resume, Theme, ResumeStyle } from '@/types/resume'
import { resumeApi, themeApi, styleApi } from '@/lib/api'

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
  applyStyle: (style: ResumeStyle | null) => void
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
    // Save current style for old theme before switching
    if (current.themeId && current.themeId !== themeId) {
      styleApi.saveStyle(current.id, current.themeId, {
        fontSize: current.fontSize,
        lineHeight: current.lineHeight,
        sectionSpacing: current.sectionSpacing,
      }).catch(() => {})
    }
    // Load new theme CSS
    const css = await themeApi.getCss(themeId)
    await get().updateResume(current.id, { themeId })
    set({ currentThemeCss: css })
    // Load saved style for new theme
    try {
      const saved = await styleApi.getStyle(current.id, themeId)
      if (saved) {
        get().applyStyle(saved)
      }
    } catch {
      // 204 No Content — no saved style, ignore
    }
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

  applyStyle: (style: ResumeStyle | null) => {
    const current = get().currentResume
    if (!current || !style) return
    set({
      currentResume: {
        ...current,
        fontSize: style.fontSize ?? current.fontSize,
        lineHeight: style.lineHeight ?? current.lineHeight,
        sectionSpacing: style.sectionSpacing ?? current.sectionSpacing,
      },
    })
  },
}))
