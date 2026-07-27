import { describe, it, expect, beforeEach } from 'vitest'
import { useThemeStore } from '@/stores/themeStore'
import { useResumeStore } from '@/stores/resumeStore'

describe('themeStore', () => {
  beforeEach(() => {
    useThemeStore.setState({
      themes: [],
      currentThemeCss: '',
      currentThemeVariables: [],
      customVariables: {},
    })
  })

  it('has correct initial state', () => {
    const state = useThemeStore.getState()
    expect(state.themes).toEqual([])
    expect(state.currentThemeCss).toBe('')
    expect(state.currentThemeVariables).toEqual([])
    expect(state.customVariables).toEqual({})
  })

  it('updateCustomVariable sets a variable', () => {
    useThemeStore.setState({
      customVariables: {},
    })

    useThemeStore.getState().updateCustomVariable('--primary-color', '#ff0000')
    const vars = useThemeStore.getState().customVariables
    expect(vars['--primary-color']).toBe('#ff0000')
  })

  it('updateCustomVariable overwrites existing variable', () => {
    useThemeStore.setState({
      customVariables: { '--primary-color': '#000' },
    })

    useThemeStore.getState().updateCustomVariable('--primary-color', '#ff0000')
    const vars = useThemeStore.getState().customVariables
    expect(vars['--primary-color']).toBe('#ff0000')
  })

  it('resetCustomVariables clears all variables', () => {
    useThemeStore.setState({
      customVariables: { '--primary-color': '#ff0000', '--font-size': '12pt' },
    })

    useThemeStore.getState().resetCustomVariables()
    expect(useThemeStore.getState().customVariables).toEqual({})
  })

  it('applyStyle parses customVariables string into map', () => {
    useResumeStore.setState({
      currentResume: {
        id: '1',
        title: 'Test',
        content: '',
        themeId: 'classic',
        fontSize: null,
        lineHeight: null,
        sectionSpacing: 'normal',
        createdAt: '',
        updatedAt: '',
      },
    })

    useThemeStore.getState().applyStyle({
      id: 1,
      resumeId: '1',
      themeId: 'classic',
      fontSize: null,
      lineHeight: null,
      sectionSpacing: null,
      customVariables: '{"--color":"#123"}',
    })

    expect(useThemeStore.getState().customVariables).toEqual({ '--color': '#123' })
  })

  it('applyStyle handles null customVariables', () => {
    useResumeStore.setState({
      currentResume: {
        id: '1',
        title: 'Test',
        content: '',
        themeId: 'classic',
        fontSize: 12,
        lineHeight: 1.5,
        sectionSpacing: 'compact',
        createdAt: '',
        updatedAt: '',
      },
    })
    useThemeStore.setState({
      customVariables: { '--old': 'value' },
    })

    useThemeStore.getState().applyStyle({
      id: 1,
      resumeId: '1',
      themeId: 'classic',
      fontSize: 11,
      lineHeight: null,
      sectionSpacing: null,
    })

    expect(useThemeStore.getState().customVariables).toEqual({})
  })

  it('createTheme action exists', () => {
    const store = useThemeStore.getState()
    expect(typeof store.createTheme).toBe('function')
  })

  it('updateTheme action exists', () => {
    const store = useThemeStore.getState()
    expect(typeof store.updateTheme).toBe('function')
  })

  it('deleteTheme action exists', () => {
    const store = useThemeStore.getState()
    expect(typeof store.deleteTheme).toBe('function')
  })
})
