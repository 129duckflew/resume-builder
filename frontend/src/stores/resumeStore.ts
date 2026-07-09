import { create } from 'zustand'
import type { Resume, Theme, ResumeStyle, VariableDeclaration } from '@/types/resume'
import { resumeApi, themeApi, styleApi } from '@/lib/api'

interface ResumeState {
  resumes: Resume[]
  currentResume: Resume | null
  themes: Theme[]
  currentThemeCss: string
  currentThemeVariables: VariableDeclaration[]
  customVariables: Record<string, string>
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
  fetchThemeVariables: (themeId: string) => Promise<void>
  updateCustomVariable: (name: string, value: string) => void
  resetCustomVariables: () => void
}

let saveTimer: ReturnType<typeof setTimeout> | null = null

export const useResumeStore = create<ResumeState>((set, get) => ({
  resumes: [],
  currentResume: null,
  themes: [],
  currentThemeCss: '',
  currentThemeVariables: [],
  customVariables: {},
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
      // Load theme variables and saved style
      const store = get()
      await store.fetchThemeVariables(resume.themeId)
      try {
        const savedStyle = await styleApi.getStyle(id, resume.themeId)
        if (savedStyle) {
          store.applyStyle(savedStyle)
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
        customVariables: get().customVariables,
      }).catch(() => {})
    }
    // Load new theme CSS
    const css = await themeApi.getCss(themeId)
    await get().updateResume(current.id, { themeId })
    set({ currentThemeCss: css })
    // Load variables for new theme
    await get().fetchThemeVariables(themeId)
    // Load saved style for new theme
    try {
      const saved = await styleApi.getStyle(current.id, themeId)
      if (saved) {
        get().applyStyle(saved)
      } else {
        set({ customVariables: {} })
      }
    } catch {
      set({ customVariables: {} })
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
    // Parse customVariables string into map
    if (style.customVariables) {
      try {
        const parsed = JSON.parse(style.customVariables) as Record<string, string>
        set({ customVariables: parsed })
      } catch {
        set({ customVariables: {} })
      }
    } else {
      set({ customVariables: {} })
    }
  },

  fetchThemeVariables: async (themeId: string) => {
    try {
      const vars = await themeApi.getVariables(themeId)
      set({ currentThemeVariables: vars || [] })
    } catch {
      set({ currentThemeVariables: [] })
    }
  },

  updateCustomVariable: (name: string, value: string) => {
    const { customVariables } = get()
    const updated = { ...customVariables, [name]: value }
    set({ customVariables: updated })

    // Debounce auto-save
    if (saveTimer) clearTimeout(saveTimer)
    saveTimer = setTimeout(async () => {
      const current = get().currentResume
      if (!current) return
      try {
        await styleApi.saveStyle(current.id, current.themeId, {
          customVariables: updated,
        })
      } catch {
        // ignore save errors
      }
    }, 300)
  },

  resetCustomVariables: () => {
    set({ customVariables: {} })
    const current = get().currentResume
    if (!current) return
    styleApi.saveStyle(current.id, current.themeId, {
      customVariables: {},
    }).catch(() => {})
  },
}))
