import { useEffect, useState } from 'react'
import { Clock, RotateCcw, Loader2, GitCompare, Check } from 'lucide-react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { ConfirmDialogAction } from '@/components/ui/confirm-dialog'
import { toast } from '@/hooks/use-toast'
import { versionApi } from '@/lib/api'
import type { ResumeVersion } from '@/types/resume'
import VersionDiff from '@/components/editor/VersionDiff'

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
  const [restoreTarget, setRestoreTarget] = useState<ResumeVersion | null>(null)
  const [selectedVersions, setSelectedVersions] = useState<Set<number>>(new Set())
  const [showDiff, setShowDiff] = useState(false)

  useEffect(() => {
    if (open) {
      setLoading(true)
      setSelectedVersions(new Set())
      versionApi.list(resumeId)
        .then(setVersions)
        .catch(() => toast({ title: 'Failed to load versions', variant: 'destructive' }))
        .finally(() => setLoading(false))
    }
  }, [open, resumeId])

  const toggleSelect = (versionNumber: number) => {
    setSelectedVersions((prev) => {
      const next = new Set(prev)
      if (next.has(versionNumber)) {
        next.delete(versionNumber)
      } else {
        if (next.size >= 2) return prev // max 2
        next.add(versionNumber)
      }
      return next
    })
  }

  const doRestore = (v: ResumeVersion) => {
    setRestoreTarget(v)
  }

  const handleConfirmRestore = async () => {
    if (!restoreTarget) return
    setRestoring(restoreTarget.versionNumber)
    try {
      await versionApi.restore(resumeId, restoreTarget.versionNumber)
      toast({ title: `Restored version ${restoreTarget.versionNumber}`, variant: 'success' })
      onRestore()
      onOpenChange(false)
    } catch {
      toast({ title: 'Restore failed', variant: 'destructive' })
    } finally {
      setRestoring(null)
      setRestoreTarget(null)
    }
  }

  return (
    <>
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
            <>
              <div className="space-y-2 max-h-96 overflow-y-auto">
                {versions.map((v) => (
                  <div
                    key={v.id}
                    className={`flex items-center justify-between border rounded-lg p-3 cursor-pointer transition-colors ${
                      selectedVersions.has(v.versionNumber) ? 'border-primary bg-primary/5' : ''
                    }`}
                    onClick={() => toggleSelect(v.versionNumber)}
                  >
                    <div className="flex items-center gap-3 min-w-0 flex-1">
                      <div className={`w-5 h-5 rounded border flex items-center justify-center shrink-0 ${
                        selectedVersions.has(v.versionNumber)
                          ? 'bg-primary border-primary text-primary-foreground'
                          : 'border-muted-foreground/30'
                      }`}>
                        {selectedVersions.has(v.versionNumber) && <Check className="h-3.5 w-3.5" />}
                      </div>
                      <div className="min-w-0">
                        <p className="text-sm font-medium">v{v.versionNumber} — {v.title}</p>
                        <p className="text-xs text-muted-foreground">
                          {new Date(v.createdAt).toLocaleString()}
                          {v.content ? ` · ${v.content.length} chars` : ''}
                        </p>
                      </div>
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
              {selectedVersions.size === 2 && (
                <Button
                  className="mt-4 w-full"
                  size="sm"
                  variant="outline"
                  onClick={() => setShowDiff(true)}
                >
                  <GitCompare className="h-4 w-4 mr-2" />
                  Compare Diff
                </Button>
              )}
            </>
          )}
        </DialogContent>
      </Dialog>
      <VersionDiff resumeId={resumeId} open={showDiff} onClose={() => setShowDiff(false)} />
      <ConfirmDialogAction
        open={restoreTarget !== null}
        onOpenChange={(open) => { if (!open) setRestoreTarget(null) }}
        title="Restore Version"
        description={`Restore version ${restoreTarget?.versionNumber} from ${restoreTarget ? new Date(restoreTarget.createdAt).toLocaleString() : ''}?`}
        confirmLabel="Restore"
        variant="warning"
        loading={restoring !== null}
        onConfirm={handleConfirmRestore}
      />
    </>
  )
}
