import { useEffect, useRef, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, Download } from 'lucide-react'
import { resumeApi } from '@/lib/api'
import { Button } from '@/components/ui/button'

export default function PreviewPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [html, setHtml] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!id) return
    setLoading(true)
    resumeApi.preview(id).then((h) => {
      setHtml(h)
      setLoading(false)
    }).catch(() => setLoading(false))
  }, [id])

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
        <Button variant="outline" size="sm" onClick={() => resumeApi.exportPdf(id!)}>
          <Download className="h-4 w-4 mr-1" />
          Download PDF
        </Button>
        <Button variant="outline" size="sm" onClick={() => resumeApi.exportHtml(id!)}>
          <Download className="h-4 w-4 mr-1" />
          Download HTML
        </Button>
      </div>
      <div className="flex-1 bg-gray-100 overflow-y-auto flex justify-center p-8">
        <div className="shadow-2xl bg-white w-[210mm] min-h-[297mm]">
          <iframe ref={iframeRef} className="w-full h-[297mm]" title="Resume Preview" />
        </div>
      </div>
    </div>
  )
}
