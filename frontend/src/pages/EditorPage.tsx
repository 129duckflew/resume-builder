import { useEffect, useRef, useCallback, useState } from 'react'
import { useParams } from 'react-router-dom'
import MDEditor from '@uiw/react-md-editor'
import { Save } from 'lucide-react'
import { Panel, PanelGroup, PanelResizeHandle } from 'react-resizable-panels'
import { useResumeStore } from '@/stores/resumeStore'
import { useHistoryStore } from '@/stores/historyStore'
import { useKeyboardShortcuts } from '@/hooks/useKeyboardShortcuts'
import { useDraftBackup } from '@/hooks/useDraftBackup'
import { resumeApi } from '@/lib/api'
import { toast } from '@/hooks/use-toast'
import SectionDragList from '@/components/editor/SectionDragList'
import ThemeSelector from '@/components/editor/ThemeSelector'
import ExportPanel from '@/components/editor/ExportPanel'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog'

export default function EditorPage() {
  const { id } = useParams<{ id: string }>()
  const timerRef = useRef<ReturnType<typeof setTimeout>>()
  const previewTimerRef = useRef<ReturnType<typeof setTimeout>>()
  const { currentResume, currentThemeCss, loading, fetchResume, updateResume, setContent } =
    useResumeStore()
  const pushState = useHistoryStore((s) => s.pushState)
  const reset = useHistoryStore((s) => s.reset)
  const { getDraft, clearDraft } = useDraftBackup()
  const [previewHtml, setPreviewHtml] = useState('')
  const [showDraftDialog, setShowDraftDialog] = useState(false)
  const [draftContent, setDraftContent] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    fetchResume(id).then(() => {
      const draft = getDraft(id)
      if (draft) {
        const serverContent = useResumeStore.getState().currentResume?.content
        if (draft !== serverContent) {
          setDraftContent(draft)
          setShowDraftDialog(true)
        } else {
          clearDraft(id)
        }
      }
    })
  }, [id, fetchResume])

  useEffect(() => {
    if (currentResume) {
      reset(currentResume.content)
    }
  }, [currentResume?.id])

  const save = useCallback(() => {
    if (!id || !currentResume) return
    updateResume(id, { content: currentResume.content, title: currentResume.title })
    clearDraft(id)
    toast({ title: 'Saved', variant: 'success' })
  }, [id, currentResume, updateResume, clearDraft])

  const debouncedSave = useCallback(
    (content: string) => {
      setContent(content)
      pushState(content)
      if (timerRef.current) clearTimeout(timerRef.current)
      timerRef.current = setTimeout(() => {
        if (id) updateResume(id, { content })
      }, 800)
    },
    [id, setContent, updateResume, pushState],
  )

  const updateTitle = useCallback(
    (title: string) => {
      if (!id || !currentResume) return
      useResumeStore.setState({ currentResume: { ...currentResume, title } })
      if (timerRef.current) clearTimeout(timerRef.current)
      timerRef.current = setTimeout(() => updateResume(id, { title }), 800)
    },
    [id, currentResume, updateResume],
  )

  // Debounced backend preview — re-fetches on content or theme change
  useEffect(() => {
    setPreviewHtml('')
    if (!id || !currentResume?.content) return
    if (previewTimerRef.current) clearTimeout(previewTimerRef.current)
    previewTimerRef.current = setTimeout(async () => {
      try {
        const html = await resumeApi.preview(id)
        setPreviewHtml(html)
      } catch {
        // fallback to client-side preview
        setPreviewHtml('')
      }
    }, 800)
    return () => {
      if (previewTimerRef.current) clearTimeout(previewTimerRef.current)
    }
  }, [id, currentResume?.content, currentResume?.themeId])

  useKeyboardShortcuts({ onSave: save })

  const scrollEditorToLine = useCallback((line: number) => {
    const el = document.querySelector('.w-md-editor-text')
    if (!el) return
    el.scrollTop = line * 22
  }, [])

  const restoreDraft = () => {
    if (draftContent && id) {
      setContent(draftContent)
      updateResume(id, { content: draftContent })
    }
    setShowDraftDialog(false)
  }

  if (loading || !currentResume) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-muted-foreground">Loading...</p>
      </div>
    )
  }

  const clientPreview = currentResume.content
    ? `<style>${currentThemeCss}</style><div class="resume-page">${renderMarkdown(currentResume.content)}</div>`
    : ''

  const displayHtml = previewHtml || clientPreview

  return (
    <div className="h-[calc(100vh-57px)] flex flex-col">
      {/* Toolbar */}
      <div className="border-b px-4 py-2 flex items-center gap-3 bg-white">
        <input
          type="text"
          value={currentResume.title}
          onChange={(e) => updateTitle(e.target.value)}
          className="flex-1 text-lg font-semibold border-none outline-none bg-transparent"
          placeholder="Resume Title"
        />
        <Button variant="ghost" size="sm" onClick={save} title="Save (Cmd+S)">
          <Save className="h-4 w-4 mr-1" />
          Save
        </Button>
        <ThemeSelector />
        <ExportPanel />
      </div>

      {/* Main editing area */}
      <PanelGroup direction="horizontal" className="flex-1 overflow-hidden" data-testid="panel-group">
        {/* Sections sidebar */}
        <Panel defaultSize={15} minSize={10} maxSize={25} data-panel>
          <div className="h-full border-r bg-gray-50 p-3 overflow-y-auto">
            <h3 className="text-xs font-medium text-muted-foreground uppercase tracking-wider mb-2">
              Sections
            </h3>
            <SectionDragList markdown={currentResume.content} onSectionClick={scrollEditorToLine} />
          </div>
        </Panel>

        <PanelResizeHandle
          data-resize-handle
          className="w-1.5 bg-transparent hover:bg-blue-400/30 transition-colors cursor-col-resize shrink-0"
        />

        {/* Markdown editor */}
        <Panel defaultSize={50} minSize={20} data-panel>
          <div className="h-full border-r overflow-y-auto" data-color-mode="light">
            <MDEditor
              value={currentResume.content}
              onChange={(val) => debouncedSave(val || '')}
              height="100%"
              preview="edit"
              hideToolbar={false}
            />
          </div>
        </Panel>

        <PanelResizeHandle
          data-resize-handle
          className="w-1.5 bg-transparent hover:bg-blue-400/30 transition-colors cursor-col-resize shrink-0"
        />

        {/* Live A4 preview */}
        <Panel defaultSize={35} minSize={15} data-panel>
          <div className="h-full bg-gray-100 overflow-y-auto flex justify-center p-2">
            <div className="shadow-lg bg-white w-[210mm] min-h-[297mm] self-start">
              {displayHtml ? (
                <div
                  className="resume-page"
                  dangerouslySetInnerHTML={{ __html: displayHtml }}
                />
              ) : (
                <div className="flex items-center justify-center h-full text-muted-foreground text-sm p-8">
                  Start typing markdown to see preview
                </div>
              )}
            </div>
          </div>
        </Panel>
      </PanelGroup>

      {/* Draft recovery dialog */}
      <Dialog open={showDraftDialog} onOpenChange={setShowDraftDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Unsaved Draft Found</DialogTitle>
            <DialogDescription className="pt-2">
              We found a local draft that may contain unsaved changes.
              Would you like to restore it?
            </DialogDescription>
          </DialogHeader>
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setShowDraftDialog(false)}>
              Discard Draft
            </Button>
            <Button onClick={restoreDraft}>
              Restore Draft
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  )
}

function renderMarkdown(md: string): string {
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
