import { useEffect, useState } from 'react'
import { Clock, RotateCcw, Loader2 } from 'lucide-react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { toast } from '@/hooks/use-toast'
import { versionApi } from '@/lib/api'
import type { ResumeVersion } from '@/types/resume'

interface Props {
  resumeId: string
  open: boolean
  onOpenChange: (v: boolean) => void
  onRestore: () => void
}

export default function VersionPanel({ resumeId, open, onOpenChange, onRestore }: Props) {
  const [versions, setVersions] = useState<ResumeVersion[]>([])
  const [loading, setLoading] = useState(false)
  const [restoring, setRestoring] = useState<number | null>(null)

  useEffect(() => {
    if (open) {
      setLoading(true)
      versionApi.list(resumeId)
        .then(setVersions)
        .catch(() => toast({ title: 'Failed to load versions', variant: 'destructive' }))
        .finally(() => setLoading(false))
    }
  }, [open, resumeId])

  const doRestore = async (v: ResumeVersion) => {
    if (!confirm(`Restore version ${v.versionNumber} from ${new Date(v.createdAt).toLocaleString()}?`)) return
    setRestoring(v.versionNumber)
    try {
      await versionApi.restore(resumeId, v.versionNumber)
      toast({ title: `Restored version ${v.versionNumber}`, variant: 'success' })
      onRestore()
      onOpenChange(false)
    } catch {
      toast({ title: 'Restore failed', variant: 'destructive' })
    } finally {
      setRestoring(null)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Clock className="h-5 w-5" />
            Version History
          </DialogTitle>
        </DialogHeader>
        {loading ? (
          <div className="flex justify-center py-8">
            <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
          </div>
        ) : versions.length === 0 ? (
          <p className="text-sm text-muted-foreground py-4 text-center">No versions yet. Save your resume to create a version.</p>
        ) : (
          <div className="space-y-2 max-h-96 overflow-y-auto">
            {versions.map((v) => (
              <div key={v.id} className="flex items-center justify-between border rounded-lg p-3">
                <div className="min-w-0">
                  <p className="text-sm font-medium">v{v.versionNumber} — {v.title}</p>
                  <p className="text-xs text-muted-foreground">
                    {new Date(v.createdAt).toLocaleString()}
                    {v.content ? ` · ${v.content.length} chars` : ''}
                  </p>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => doRestore(v)}
                  disabled={restoring !== null}
                >
                  {restoring === v.versionNumber ? (
                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                  ) : (
                    <RotateCcw className="h-3.5 w-3.5" />
                  )}
                </Button>
              </div>
            ))}
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}
