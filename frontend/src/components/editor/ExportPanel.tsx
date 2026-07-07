import { useState } from 'react'
import { Download, FileText, FileDown } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { resumeApi } from '@/lib/api'
import { useResumeStore } from '@/stores/resumeStore'

export default function ExportPanel() {
  const [exporting, setExporting] = useState(false)
  const currentResume = useResumeStore((s) => s.currentResume)

  if (!currentResume) return null

  const doExport = async (type: 'pdf' | 'html') => {
    setExporting(true)
    try {
      if (type === 'pdf') {
        await resumeApi.exportPdf(currentResume.id)
      } else {
        await resumeApi.exportHtml(currentResume.id)
      }
    } catch (err: any) {
      if (err.response?.data) {
        alert(typeof err.response.data === 'string'
          ? err.response.data
          : 'Export failed - content may be too long for one page')
      } else {
        alert('Export failed')
      }
    } finally {
      setExporting(false)
    }
  }

  return (
    <div className="flex items-center gap-2">
      <Button
        variant="outline"
        size="sm"
        onClick={() => doExport('pdf')}
        disabled={exporting}
      >
        <FileDown className="h-4 w-4 mr-1" />
        {exporting ? 'Exporting...' : 'PDF'}
      </Button>
      <Button
        variant="outline"
        size="sm"
        onClick={() => doExport('html')}
        disabled={exporting}
      >
        <FileText className="h-4 w-4 mr-1" />
        HTML
      </Button>
    </div>
  )
}
