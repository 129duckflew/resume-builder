import { describe, it, expect, beforeEach } from 'vitest'
import { useResumeStore } from '@/stores/resumeStore'

describe('resumeStore', () => {
  beforeEach(() => {
    useResumeStore.setState({
      resumes: [],
      currentResume: null,
      themes: [],
      currentThemeCss: '',
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
})
