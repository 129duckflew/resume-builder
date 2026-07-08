import { useEffect, useState } from 'react'
import { Plus, Trash2, Shield } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog'
import { toast } from '@/hooks/use-toast'
import { desensitizeApi } from '@/lib/api'
import type { DesensitizeRule } from '@/types/desensitize'

export default function DesensitizeSettings({ open, onOpenChange }: {
  open: boolean
  onOpenChange: (v: boolean) => void
}) {
  const [rules, setRules] = useState<DesensitizeRule[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (open) {
      setLoading(true)
      desensitizeApi.getRules()
        .then(setRules)
        .catch(() => toast({ title: 'Failed to load rules', variant: 'destructive' }))
        .finally(() => setLoading(false))
    }
  }, [open])

  const toggleEnabled = (index: number) => {
    setRules(prev => prev.map((r, i) =>
      i === index ? { ...r, enabled: !r.enabled } : r
    ))
  }

  const updateRule = (index: number, field: keyof DesensitizeRule, value: any) => {
    setRules(prev => prev.map((r, i) =>
      i === index ? { ...r, [field]: value } : r
    ))
  }

  const addRule = () => {
    setRules(prev => [...prev, {
      id: null,
      name: '',
      description: '',
      pattern: '',
      replacement: '',
      enabled: true,
      defaultRule: false,
      sortOrder: (prev.length > 0 ? Math.max(...prev.map(r => r.sortOrder)) : 0) + 1,
    }])
  }

  const removeRule = (index: number) => {
    setRules(prev => prev.filter((_, i) => i !== index))
  }

  const save = async () => {
    try {
      await desensitizeApi.saveRules(rules)
      toast({ title: 'Rules saved', variant: 'success' })
      onOpenChange(false)
    } catch {
      toast({ title: 'Failed to save rules', variant: 'destructive' })
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Shield className="h-5 w-5" />
            Desensitize Rules
          </DialogTitle>
          <DialogDescription>
            Configure which fields are masked during export. Default rules can be toggled on/off; custom rules can be added, edited, or removed.
          </DialogDescription>
        </DialogHeader>

        {loading ? (
          <p className="text-sm text-muted-foreground py-4 text-center">Loading...</p>
        ) : (
          <div className="space-y-3 max-h-96 overflow-y-auto">
            {rules.map((rule, i) => (
              <div key={i} className={`border rounded-lg p-3 space-y-2 ${rule.defaultRule ? 'bg-gray-50' : ''}`}>
                <div className="flex items-center justify-between gap-2">
                  <div className="flex items-center gap-2 min-w-0">
                    <input
                      type="checkbox"
                      checked={rule.enabled}
                      onChange={() => toggleEnabled(i)}
                      className="accent-primary rounded shrink-0"
                    />
                    <input
                      className="text-sm font-medium bg-transparent border-0 p-0 focus:outline-none focus:ring-0 w-32"
                      value={rule.name}
                      onChange={e => updateRule(i, 'name', e.target.value)}
                      readOnly={rule.defaultRule}
                      placeholder="Rule name"
                    />
                    {rule.defaultRule && (
                      <span className="text-xs text-muted-foreground bg-gray-200 rounded px-1.5 py-0.5">default</span>
                    )}
                  </div>
                  {!rule.defaultRule && (
                    <Button variant="ghost" size="icon" className="h-7 w-7 shrink-0" onClick={() => removeRule(i)}>
                      <Trash2 className="h-3.5 w-3.5 text-red-500" />
                    </Button>
                  )}
                </div>
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="text-xs text-muted-foreground">Pattern</label>
                    <input
                      className="w-full text-xs font-mono border rounded px-2 py-1 bg-white"
                      value={rule.pattern}
                      onChange={e => updateRule(i, 'pattern', e.target.value)}
                      readOnly={rule.defaultRule}
                      placeholder="Regex pattern"
                    />
                  </div>
                  <div>
                    <label className="text-xs text-muted-foreground">Replacement</label>
                    <input
                      className="w-full text-xs font-mono border rounded px-2 py-1 bg-white"
                      value={rule.replacement}
                      onChange={e => updateRule(i, 'replacement', e.target.value)}
                      readOnly={rule.defaultRule}
                      placeholder="Replacement"
                    />
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        <div className="flex items-center justify-between">
          <Button variant="outline" size="sm" onClick={addRule}>
            <Plus className="h-4 w-4 mr-1" />
            Add Rule
          </Button>
          <div className="flex gap-2">
            <Button variant="outline" size="sm" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button size="sm" onClick={save}>
              Save
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}
