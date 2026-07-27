import { create } from 'zustand'
import type { Theme, ThemeDTO, ResumeStyle, VariableDeclaration } from '@/types/resume'
import { themeApi, styleApi } from '@/lib/api'
import { useResumeStore } from '@/stores/resumeStore'

interface ThemeState {
  themes: Theme[]
  currentThemeCss: string
  currentThemeVariables: VariableDeclaration[]
  customVariables: Record<string, string>

  fetchThemes: () => Promise<void>
  setTheme: (themeId: string) => Promise<void>
  applyStyle: (style: ResumeStyle | null) => void
  fetchThemeVariables: (themeId: string) => Promise<void>
  updateCustomVariable: (name: string, value: string) => void
  resetCustomVariables: () => void
  createTheme: (data: ThemeDTO) => Promise<Theme>
  updateTheme: (id: string, data: ThemeDTO) => Promise<Theme>
  deleteTheme: (id: string) => Promise<void>
}

let saveTimer: ReturnType<typeof setTimeout> | null = null

export const useThemeStore = create<ThemeState>((set, get) => ({
  themes: [],
  currentThemeCss: '',
  currentThemeVariables: [],
  customVariables: {},

  fetchThemes: async () => {
    const themes = await themeApi.list()
    set({ themes })
  },

  setTheme: async (themeId: string) => {
    const resumeStore = useResumeStore.getState()
    const current = resumeStore.currentResume
    if (!current) return
    if (current.themeId && current.themeId !== themeId) {
      styleApi.saveStyle(current.id, current.themeId, {
        fontSize: current.fontSize,
        lineHeight: current.lineHeight,
        sectionSpacing: current.sectionSpacing,
        customVariables: get().customVariables,
      }).catch(() => {})
    }
    const css = await themeApi.getCss(themeId)
    await resumeStore.updateResume(current.id, { themeId })
    set({ currentThemeCss: css })
    await get().fetchThemeVariables(themeId)
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

  applyStyle: (style: ResumeStyle | null) => {
    const resumeStore = useResumeStore.getState()
    const current = resumeStore.currentResume
    if (!current || !style) return
    useResumeStore.setState({
      currentResume: {
        ...current,
        fontSize: style.fontSize ?? current.fontSize,
        lineHeight: style.lineHeight ?? current.lineHeight,
        sectionSpacing: style.sectionSpacing ?? current.sectionSpacing,
      },
    })
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

    if (saveTimer) clearTimeout(saveTimer)
    saveTimer = setTimeout(async () => {
      const resumeStore = useResumeStore.getState()
      const current = resumeStore.currentResume
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
    const resumeStore = useResumeStore.getState()
    const current = resumeStore.currentResume
    if (!current) return
    styleApi.saveStyle(current.id, current.themeId, {
      customVariables: {},
    }).catch(() => {})
  },

  createTheme: async (data: ThemeDTO) => {
    const theme = await themeApi.create(data)
    await get().fetchThemes()
    return theme
  },

  updateTheme: async (id: string, data: ThemeDTO) => {
    const theme = await themeApi.update(id, data)
    await get().fetchThemes()
    return theme
  },

  deleteTheme: async (id: string) => {
    await themeApi.delete(id)
    await get().fetchThemes()
  },
}))
