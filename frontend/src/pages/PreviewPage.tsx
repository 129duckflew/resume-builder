import { useEffect, useRef, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, Download, AlertTriangle, Shield, FileCode } from 'lucide-react'
import { resumeApi, jsonResumeApi } from '@/lib/api'
import { Button } from '@/components/ui/button'

export default function PreviewPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [html, setHtml] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [smartOnePage, setSmartOnePage] = useState(false)
  const [desensitize, setDesensitize] = useState(false)

  useEffect(() => {
    if (!id) return
    setLoading(true)
    setError(null)
    resumeApi.preview(id, smartOnePage, desensitize).then((h) => {
      setHtml(h)
      setLoading(false)
    }).catch((err) => {
      setError(err.response?.data?.error || 'Preview generation failed')
      setLoading(false)
    })
  }, [id, smartOnePage, desensitize])

  const iframeRef = useRef<HTMLIFrameElement>(null)

  useEffect(() => {
    if (html && iframeRef.current) {
      const doc = iframeRef.current.contentDocument
      if (doc) {
        doc.open()
        doc.write(html)
        doc.close()
      }
    }
  }, [html])

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-muted-foreground">Loading preview...</p>
      </div>
    )
  }

  return (
    <div className="h-[calc(100vh-57px)] flex flex-col">
      <div className="border-b px-4 py-2 flex items-center gap-3 bg-white">
        <Button variant="ghost" size="sm" onClick={() => navigate(`/editor/${id}`)}>
          <ArrowLeft className="h-4 w-4 mr-1" />
          Back to Editor
        </Button>
        <div className="flex-1" />
        <label className="flex items-center gap-1 text-xs text-muted-foreground cursor-pointer select-none hover:text-foreground transition-colors">
          <input
            type="checkbox"
            checked={smartOnePage}
            onChange={(e) => setSmartOnePage(e.target.checked)}
            className="accent-primary rounded"
          />
          Smart One-Page
        </label>
        <label className="flex items-center gap-1 text-xs text-muted-foreground cursor-pointer select-none hover:text-foreground transition-colors">
          <input
            type="checkbox"
            checked={desensitize}
            onChange={(e) => setDesensitize(e.target.checked)}
            className="accent-primary rounded"
          />
          <Shield className="h-3 w-3" />
          Desensitize
        </label>
        <Button variant="outline" size="sm" onClick={() => resumeApi.exportPdf(id!, smartOnePage, desensitize)}>
          <Download className="h-4 w-4 mr-1" />
          Download PDF
        </Button>
        <Button variant="outline" size="sm" onClick={() => resumeApi.exportHtml(id!, smartOnePage, desensitize)}>
          <Download className="h-4 w-4 mr-1" />
          Download HTML
        </Button>
        <Button variant="outline" size="sm" onClick={() => {
          if (!id) return
          jsonResumeApi.exportJson(id)
            .then(json => {
              const blob = new Blob([JSON.stringify(json, null, 2)], { type: 'application/json' })
              const url = URL.createObjectURL(blob)
              const a = document.createElement('a')
              a.href = url
              a.download = 'resume.json'
              a.click()
              URL.revokeObjectURL(url)
            })
        }}>
          <FileCode className="h-4 w-4 mr-1" />
          JSON
        </Button>
      </div>
      <div className="flex-1 bg-gray-100 overflow-y-auto flex justify-center p-8">
        {error && (
          <div className="flex items-center gap-2 text-amber-600 bg-amber-50 border border-amber-200 rounded px-4 py-2 mb-4">
            <AlertTriangle className="h-4 w-4 shrink-0" />
            <span className="text-sm">{error}</span>
          </div>
        )}
        <div className="shadow-2xl bg-white w-[210mm] min-h-[297mm]">
          <iframe ref={iframeRef} className="w-full h-[297mm]" title="Resume Preview" />
        </div>
      </div>
    </div>
  )
}
