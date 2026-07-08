import { useEffect, useState } from 'react'
import { Share2, Link, Trash2, Copy, Loader2 } from 'lucide-react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { toast } from '@/hooks/use-toast'
import { shareApi } from '@/lib/api'
import type { ShareLink } from '@/types/resume'

interface Props {
  resumeId: string
  open: boolean
  onOpenChange: (v: boolean) => void
}

export default function SharePanel({ resumeId, open, onOpenChange }: Props) {
  const [links, setLinks] = useState<ShareLink[]>([])
  const [loading, setLoading] = useState(false)
  const [creating, setCreating] = useState(false)
  const [desensitize, setDesensitize] = useState(false)

  const loadLinks = () => {
    setLoading(true)
    shareApi.list(resumeId)
      .then(setLinks)
      .catch(() => toast({ title: 'Failed to load share links', variant: 'destructive' }))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    if (open) loadLinks()
  }, [open, resumeId])

  const createLink = async () => {
    setCreating(true)
    try {
      await shareApi.create(resumeId, desensitize)
      toast({ title: 'Share link created', variant: 'success' })
      loadLinks()
    } catch {
      toast({ title: 'Failed to create link', variant: 'destructive' })
    } finally {
      setCreating(false)
    }
  }

  const copyLink = (id: string) => {
    const url = `${window.location.origin}/s/${id}`
    navigator.clipboard.writeText(url)
    toast({ title: 'Link copied to clipboard', variant: 'success' })
  }

  const deleteLink = async (id: string) => {
    try {
      await shareApi.delete(id)
      toast({ title: 'Link deleted', variant: 'success' })
      loadLinks()
    } catch {
      toast({ title: 'Failed to delete link', variant: 'destructive' })
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Share2 className="h-5 w-5" />
            Share Resume
          </DialogTitle>
          <DialogDescription>
            Create a public link to share this resume. Anyone with the link can view it.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-3">
          <label className="flex items-center gap-2 text-sm cursor-pointer select-none">
            <input
              type="checkbox"
              checked={desensitize}
              onChange={(e) => setDesensitize(e.target.checked)}
              className="accent-primary rounded"
            />
            Apply desensitization
          </label>
          <Button onClick={createLink} disabled={creating} className="w-full">
            {creating ? <Loader2 className="h-4 w-4 mr-1 animate-spin" /> : <Link className="h-4 w-4 mr-1" />}
            Create Share Link
          </Button>
        </div>

        {loading ? (
          <div className="flex justify-center py-4"><Loader2 className="h-5 w-5 animate-spin" /></div>
        ) : links.length > 0 && (
          <div className="space-y-2 max-h-60 overflow-y-auto">
            <p className="text-xs font-medium text-muted-foreground">Existing links</p>
            {links.map((link) => (
              <div key={link.id} className="flex items-center justify-between border rounded-lg p-2.5">
                <div className="min-w-0 flex-1 mr-2">
                  <p className="text-xs font-mono truncate">{`${window.location.origin}/s/${link.id}`}</p>
                  <p className="text-xs text-muted-foreground">
                    {link.enabled ? 'Active' : 'Disabled'}
                    {link.desensitize ? ' · Desensitized' : ''}
                    {' · '}Created {new Date(link.createdAt).toLocaleDateString()}
                  </p>
                </div>
                <div className="flex gap-1 shrink-0">
                  <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => copyLink(link.id)} title="Copy link">
                    <Copy className="h-3.5 w-3.5" />
                  </Button>
                  <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => deleteLink(link.id)} title="Delete link">
                    <Trash2 className="h-3.5 w-3.5 text-destructive" />
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}
