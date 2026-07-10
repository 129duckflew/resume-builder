import { useEffect, useState } from 'react'
import { Plus, Loader2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { sectionTemplateApi } from '@/lib/api'
import type { SectionTemplate } from '@/types/sectionTemplate'

const ICON_MAP: Record<string, string> = {
  user: '\uD83D\uDC64',
  briefcase: '\uD83D\uDCBC',
  'graduation-cap': '\uD83C\uDF93',
  code: '\uD83D\uDCBB',
  folder: '\uD83D\uDCC1',
  award: '\uD83C\uDFC6',
  globe: '\uD83C\uDF10',
  users: '\uD83D\uDC65',
}

interface Props {
  open: boolean
  onOpenChange: (v: boolean) => void
  onSelect: (template: SectionTemplate) => void
}

export default function SectionTemplatePicker({ open, onOpenChange, onSelect }: Props) {
  const [templates, setTemplates] = useState<SectionTemplate[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (open) {
      setLoading(true)
      sectionTemplateApi.list()
        .then(setTemplates)
        .finally(() => setLoading(false))
    }
  }, [open])

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Plus className="h-5 w-5" />
            Add Section
          </DialogTitle>
        </DialogHeader>
        {loading ? (
          <div className="grid grid-cols-2 gap-2 py-4">
            <Skeleton className="h-14 w-full" />
            <Skeleton className="h-14 w-full" />
            <Skeleton className="h-14 w-full" />
          </div>
        ) : (
          <div className="grid grid-cols-2 gap-2 max-h-80 overflow-y-auto">
            {templates.map((t) => (
              <button
                key={t.id}
                onClick={() => { onSelect(t); onOpenChange(false) }}
                className="flex items-center gap-3 border rounded-lg p-3 text-left hover:bg-accent hover:text-accent-foreground transition-colors cursor-pointer"
              >
                <span className="text-xl shrink-0">
                  {ICON_MAP[t.icon ?? ''] ?? '\uD83D\uDCC4'}
                </span>
                <span className="text-sm font-medium">{t.name}</span>
              </button>
            ))}
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}
