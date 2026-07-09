import { useEffect, useRef, useState } from 'react'
import { GitCompare, Loader2 } from 'lucide-react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { toast } from '@/hooks/use-toast'
import { versionApi } from '@/lib/api'
import type { ResumeVersion, VersionDiffResponse } from '@/types/resume'

interface Props {
  resumeId: string
  open: boolean
  onClose: () => void
}

export default function VersionDiff({ resumeId, open, onClose }: Props) {
  const [versions, setVersions] = useState<ResumeVersion[]>([])
  const [loading, setLoading] = useState(false)
  const [versionA, setVersionA] = useState<number | null>(null)
  const [versionB, setVersionB] = useState<number | null>(null)
  const [diff, setDiff] = useState<VersionDiffResponse | null>(null)
  const [diffLoading, setDiffLoading] = useState(false)

  useEffect(() => {
    let cancelled = false
    if (open) {
      setLoading(true)
      setDiff(null)
      setVersionA(null)
      setVersionB(null)
      versionApi.list(resumeId)
        .then(data => { if (!cancelled) setVersions(data); })
        .catch(() => { if (!cancelled) toast({ title: 'Failed to load versions', variant: 'destructive' }); })
        .finally(() => { if (!cancelled) setLoading(false); })
    }
    return () => { cancelled = true }
  }, [open, resumeId])

  const diffCancelledRef = useRef(false)

  const doDiff = async () => {
    if (versionA == null || versionB == null) {
      toast({ title: 'Please select two versions', variant: 'default' })
      return
    }
    if (versionA === versionB) {
      toast({ title: 'Please select two different versions', variant: 'default' })
      return
    }
    diffCancelledRef.current = false
    setDiffLoading(true)
    setDiff(null)
    try {
      const result = await versionApi.diff(resumeId, versionA, versionB)
      if (!diffCancelledRef.current) setDiff(result)
    } catch {
      if (!diffCancelledRef.current) toast({ title: 'Failed to load diff', variant: 'destructive' })
    } finally {
      if (!diffCancelledRef.current) setDiffLoading(false)
    }
  }

  // Cancel diff if dialog closes while a request is in-flight
  useEffect(() => {
    if (!open) diffCancelledRef.current = true
  }, [open])

  return (
    <Dialog open={open} onOpenChange={(v) => { if (!v) onClose() }}>
      <DialogContent className="max-w-3xl max-h-[80vh] flex flex-col">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <GitCompare className="h-5 w-5" />
            Version Diff
          </DialogTitle>
        </DialogHeader>

        {loading ? (
          <div className="flex justify-center py-8">
            <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
          </div>
        ) : versions.length === 0 ? (
          <p className="text-sm text-muted-foreground py-4 text-center">
            No versions available for comparison.
          </p>
        ) : (
          <>
            {/* Selectors */}
            <div className="flex items-center gap-4 mb-4">
              <div className="flex-1">
                <label className="block text-xs font-medium mb-1 text-muted-foreground">Version A</label>
                <select
                  className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  value={versionA ?? ''}
                  onChange={(e) => setVersionA(e.target.value ? Number(e.target.value) : null)}
                >
                  <option value="">Select...</option>
                  {versions.map((v) => (
                    <option key={v.id} value={v.versionNumber}>
                      v{v.versionNumber} — {v.title}
                    </option>
                  ))}
                </select>
              </div>
              <div className="flex-1">
                <label className="block text-xs font-medium mb-1 text-muted-foreground">Version B</label>
                <select
                  className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  value={versionB ?? ''}
                  onChange={(e) => setVersionB(e.target.value ? Number(e.target.value) : null)}
                >
                  <option value="">Select...</option>
                  {versions.map((v) => (
                    <option key={v.id} value={v.versionNumber}>
                      v{v.versionNumber} — {v.title}
                    </option>
                  ))}
                </select>
              </div>
              <Button
                className="mt-5"
                size="sm"
                onClick={doDiff}
                disabled={versionA == null || versionB == null || diffLoading}
              >
                {diffLoading ? <Loader2 className="h-3.5 w-3.5 animate-spin mr-1" /> : null}
                Compare
              </Button>
            </div>

            {/* Diff result */}
            {diffLoading ? (
              <div className="flex justify-center py-8">
                <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
              </div>
            ) : diff ? (
              <div className="border rounded-md overflow-auto max-h-96 text-sm font-mono">
                {diff.hunks.length === 0 ? (
                  <p className="p-4 text-muted-foreground text-center">No differences found.</p>
                ) : (
                  diff.hunks.map((hunk, hi) => (
                    <div key={hi}>
                      {/* Hunk header */}
                      <div className="sticky top-0 bg-muted px-3 py-1 text-xs text-muted-foreground border-b">
                        @@ -{hunk.oldStart},{hunk.oldCount} +{hunk.newStart},{hunk.newCount} @@
                      </div>
                      {/* Lines */}
                      {hunk.lines.map((line, li) => (
                        <div
                          key={li}
                          className={`flex px-3 py-0.5 ${
                            line.type === 'ADDED'
                              ? 'bg-green-50 dark:bg-green-950'
                              : line.type === 'REMOVED'
                                ? 'bg-red-50 dark:bg-red-950'
                                : ''
                          }`}
                        >
                          <span className="w-8 shrink-0 text-right text-muted-foreground select-none">
                            {line.type === 'ADDED' ? '+' : line.type === 'REMOVED' ? '-' : ' '}
                          </span>
                          <span className="whitespace-pre-wrap break-all flex-1">{line.text}</span>
                        </div>
                      ))}
                    </div>
                  ))
                )}
              </div>
            ) : (
              <p className="text-sm text-muted-foreground py-4 text-center">
                Select two versions and click Compare to see the diff.
              </p>
            )}
          </>
        )}
      </DialogContent>
    </Dialog>
  )
}
