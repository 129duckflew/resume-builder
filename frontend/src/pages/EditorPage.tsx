import { useEffect, useRef, useCallback } from 'react'
import { useParams } from 'react-router-dom'
import MDEditor from '@uiw/react-md-editor'
import { useResumeStore } from '@/stores/resumeStore'
import SectionDragList from '@/components/editor/SectionDragList'
import ThemeSelector from '@/components/editor/ThemeSelector'
import ExportPanel from '@/components/editor/ExportPanel'

export default function EditorPage() {
  const { id } = useParams<{ id: string }>()
  const timerRef = useRef<ReturnType<typeof setTimeout>>()
  const { currentResume, currentThemeCss, loading, fetchResume, updateResume, setContent } =
    useResumeStore()

  useEffect(() => {
    if (id) fetchResume(id)
  }, [id, fetchResume])

  const debouncedSave = useCallback(
    (content: string) => {
      setContent(content)
      if (timerRef.current) clearTimeout(timerRef.current)
      timerRef.current = setTimeout(() => {
        if (id) updateResume(id, { content })
      }, 500)
    },
    [id, setContent, updateResume],
  )

  if (loading || !currentResume) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-muted-foreground">Loading...</p>
      </div>
    )
  }

  const previewHtml = currentResume.content
    ? `<style>${currentThemeCss}</style><div class="resume-page">${renderMarkdown(currentResume.content)}</div>`
    : ''

  return (
    <div className="h-[calc(100vh-57px)] flex flex-col">
      {/* Toolbar */}
      <div className="border-b px-4 py-2 flex items-center gap-3 bg-white">
        <input
          type="text"
          value={currentResume.title}
          onChange={(e) => {
            const val = e.target.value
            useResumeStore.setState({
              currentResume: { ...currentResume, title: val },
            })
            if (timerRef.current) clearTimeout(timerRef.current)
            timerRef.current = setTimeout(() => {
              if (id) updateResume(id, { title: val })
            }, 500)
          }}
          className="flex-1 text-lg font-semibold border-none outline-none bg-transparent"
          placeholder="Resume Title"
        />
        <ThemeSelector />
        <ExportPanel />
      </div>

      {/* Main editing area */}
      <div className="flex flex-1 overflow-hidden">
        {/* Sections sidebar */}
        <div className="w-56 border-r bg-gray-50 p-3 overflow-y-auto">
          <h3 className="text-xs font-medium text-muted-foreground uppercase tracking-wider mb-2">
            Sections
          </h3>
          <SectionDragList markdown={currentResume.content} />
        </div>

        {/* Markdown editor */}
        <div className="flex-1 border-r overflow-y-auto" data-color-mode="light">
          <MDEditor
            value={currentResume.content}
            onChange={(val) => debouncedSave(val || '')}
            height="100%"
            preview="edit"
            hideToolbar={false}
          />
        </div>

        {/* Live A4 preview */}
        <div className="w-[500px] bg-gray-100 overflow-y-auto flex justify-center p-4">
          <div className="shadow-lg bg-white w-[210mm] min-h-[297mm]">
            {previewHtml ? (
              <div
                className="resume-page"
                dangerouslySetInnerHTML={{ __html: previewHtml }}
              />
            ) : (
              <div className="flex items-center justify-center h-full text-muted-foreground text-sm p-8">
                Start typing markdown to see preview
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

function renderMarkdown(md: string): string {
  // Simple markdown to HTML rendering
  // In production, use the backend API for this
  let html = md
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>')
    .replace(/\|/g, '<span class="separator">|</span>')
    .replace(/\n\n/g, '</p><p>')
    .replace(/^(.+)$/gm, (match) => {
      if (match.startsWith('<')) return match
      return `<p>${match}</p>`
    })
  return html
}
