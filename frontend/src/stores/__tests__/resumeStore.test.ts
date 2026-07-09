import { describe, it, expect, beforeEach } from 'vitest'
import { useResumeStore } from '@/stores/resumeStore'

describe('resumeStore', () => {
  beforeEach(() => {
    useResumeStore.setState({
      resumes: [],
      currentResume: null,
      themes: [],
      currentThemeCss: '',
      currentThemeVariables: [],
      customVariables: {},
      loading: false,
      error: null,
    })
  })

  it('has correct initial state', () => {
    const state = useResumeStore.getState()
    expect(state.resumes).toEqual([])
    expect(state.currentResume).toBeNull()
    expect(state.themes).toEqual([])
    expect(state.currentThemeCss).toBe('')
    expect(state.currentThemeVariables).toEqual([])
    expect(state.customVariables).toEqual({})
    expect(state.loading).toBe(false)
    expect(state.error).toBeNull()
  })

  it('setContent updates currentResume content', () => {
    useResumeStore.setState({
      currentResume: {
        id: '1',
        title: 'Test',
        content: 'old',
        themeId: 'classic',
        fontSize: null,
        lineHeight: null,
        sectionSpacing: 'normal',
        createdAt: '',
        updatedAt: '',
      },
    })

    useResumeStore.getState().setContent('# New Content')
    const content = useResumeStore.getState().currentResume?.content
    expect(content).toBe('# New Content')
  })

  it('setTitle updates currentResume title', () => {
    useResumeStore.setState({
      currentResume: {
        id: '1',
        title: 'Old Title',
        content: '',
        themeId: 'classic',
        fontSize: null,
        lineHeight: null,
        sectionSpacing: 'normal',
        createdAt: '',
        updatedAt: '',
      },
    })

    useResumeStore.getState().setTitle('New Title')
    const title = useResumeStore.getState().currentResume?.title
    expect(title).toBe('New Title')
  })

  it('setContent does nothing when no currentResume', () => {
    useResumeStore.setState({ currentResume: null })
    useResumeStore.getState().setContent('# Test')
    const state = useResumeStore.getState()
    expect(state.currentResume).toBeNull()
  })

  it('updateCustomVariable sets a variable', () => {
    useResumeStore.setState({
      customVariables: {},
    })

    useResumeStore.getState().updateCustomVariable('--primary-color', '#ff0000')
    const vars = useResumeStore.getState().customVariables
    expect(vars['--primary-color']).toBe('#ff0000')
  })

  it('updateCustomVariable overwrites existing variable', () => {
    useResumeStore.setState({
      customVariables: { '--primary-color': '#000' },
    })

    useResumeStore.getState().updateCustomVariable('--primary-color', '#ff0000')
    const vars = useResumeStore.getState().customVariables
    expect(vars['--primary-color']).toBe('#ff0000')
  })

  it('resetCustomVariables clears all variables', () => {
    useResumeStore.setState({
      customVariables: { '--primary-color': '#ff0000', '--font-size': '12pt' },
    })

    useResumeStore.getState().resetCustomVariables()
    expect(useResumeStore.getState().customVariables).toEqual({})
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

    useResumeStore.getState().applyStyle({
      id: 1,
      resumeId: '1',
      themeId: 'classic',
      fontSize: null,
      lineHeight: null,
      sectionSpacing: null,
      customVariables: '{"--color":"#123"}',
    })

    expect(useResumeStore.getState().customVariables).toEqual({ '--color': '#123' })
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
      customVariables: { '--old': 'value' },
    })

    useResumeStore.getState().applyStyle({
      id: 1,
      resumeId: '1',
      themeId: 'classic',
      fontSize: 11,
      lineHeight: null,
      sectionSpacing: null,
    })

    expect(useResumeStore.getState().customVariables).toEqual({})
  })

  it('createTheme action exists', () => {
    const store = useResumeStore.getState()
    expect(typeof store.createTheme).toBe('function')
  })

  it('updateTheme action exists', () => {
    const store = useResumeStore.getState()
    expect(typeof store.updateTheme).toBe('function')
  })

  it('deleteTheme action exists', () => {
    const store = useResumeStore.getState()
    expect(typeof store.deleteTheme).toBe('function')
  })
})
