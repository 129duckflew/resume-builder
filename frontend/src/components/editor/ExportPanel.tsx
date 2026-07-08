import { useState } from 'react'
import { FileText, FileDown, Loader2, AlertTriangle, Shield, Settings2, FileCode } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog'
import { toast } from '@/hooks/use-toast'
import { resumeApi, jsonResumeApi } from '@/lib/api'
import { useResumeStore } from '@/stores/resumeStore'
import DesensitizeSettings from './DesensitizeSettings'

export default function ExportPanel({ smartOnePage, onSmartOnePageChange, desensitize, onDesensitizeChange }: {
  smartOnePage: boolean
  onSmartOnePageChange: (v: boolean) => void
  desensitize: boolean
  onDesensitizeChange: (v: boolean) => void
}) {
  const [exporting, setExporting] = useState<'pdf' | 'html' | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [showSettings, setShowSettings] = useState(false)
  const currentResume = useResumeStore((s) => s.currentResume)

  if (!currentResume) return null

  const doExport = async (type: 'pdf' | 'html') => {
    setExporting(type)
    setError(null)
    try {
      if (type === 'pdf') {
        await resumeApi.exportPdf(currentResume.id, smartOnePage, desensitize)
      } else {
        await resumeApi.exportHtml(currentResume.id, smartOnePage, desensitize)
      }
      toast({
        title: 'Export successful',
        description: `Resume exported as ${type.toUpperCase()}${desensitize ? ' (desensitized)' : ''}`,
        variant: 'success',
      })
    } catch (err: any) {
      const msg =
        err.response?.data?.error ||
        (typeof err.response?.data === 'string' ? err.response.data : null)
      if (msg) {
        setError(msg)
      } else {
        toast({
          title: 'Export failed',
          description: err.message || 'Unknown error',
          variant: 'destructive',
        })
      }
    } finally {
      setExporting(null)
    }
  }

  return (
    <>
      <div className="flex items-center gap-2">
        <label className="flex items-center gap-1 text-xs text-muted-foreground cursor-pointer select-none hover:text-foreground transition-colors">
          <input
            type="checkbox"
            checked={smartOnePage}
            onChange={(e) => onSmartOnePageChange(e.target.checked)}
            className="accent-primary rounded"
          />
          Smart One-Page
        </label>
        <label className="flex items-center gap-1 text-xs text-muted-foreground cursor-pointer select-none hover:text-foreground transition-colors">
          <input
            type="checkbox"
            checked={desensitize}
            onChange={(e) => onDesensitizeChange(e.target.checked)}
            className="accent-primary rounded"
          />
          <Shield className="h-3 w-3" />
          Desensitize
        </label>
        <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => setShowSettings(true)} title="Desensitize Settings">
          <Settings2 className="h-3.5 w-3.5" />
        </Button>
        <Button
          variant="outline"
          size="sm"
          onClick={() => doExport('pdf')}
          disabled={exporting !== null}
        >
          {exporting === 'pdf' ? (
            <Loader2 className="h-4 w-4 mr-1 animate-spin" />
          ) : (
            <FileDown className="h-4 w-4 mr-1" />
          )}
          {exporting === 'pdf' ? 'Exporting...' : 'PDF'}
        </Button>
        <Button
          variant="outline"
          size="sm"
          onClick={() => doExport('html')}
          disabled={exporting !== null}
        >
          {exporting === 'html' ? (
            <Loader2 className="h-4 w-4 mr-1 animate-spin" />
          ) : (
            <FileText className="h-4 w-4 mr-1" />
          )}
          {exporting === 'html' ? 'Exporting...' : 'HTML'}
        </Button>
        <Button
          variant="outline"
          size="sm"
          onClick={() => {
            if (!currentResume) return
            jsonResumeApi.exportJson(currentResume.id)
              .then(json => {
                const blob = new Blob([JSON.stringify(json, null, 2)], { type: 'application/json' })
                const url = URL.createObjectURL(blob)
                const a = document.createElement('a')
                a.href = url
                a.download = 'resume.json'
                a.click()
                URL.revokeObjectURL(url)
                toast({ title: 'JSON exported', variant: 'success' })
              })
              .catch(() => toast({ title: 'Export failed', variant: 'destructive' }))
          }}
        >
          <FileCode className="h-4 w-4 mr-1" />
          JSON
        </Button>
      </div>

      <Dialog open={!!error} onOpenChange={(open) => !open && setError(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <AlertTriangle className="h-5 w-5 text-amber-500" />
              Export Warning
            </DialogTitle>
            <DialogDescription className="pt-2">
              {error}
            </DialogDescription>
          </DialogHeader>
          <div className="flex justify-end">
            <Button variant="outline" onClick={() => setError(null)}>
              Got it
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      <DesensitizeSettings open={showSettings} onOpenChange={setShowSettings} />
    </>
  )
}
