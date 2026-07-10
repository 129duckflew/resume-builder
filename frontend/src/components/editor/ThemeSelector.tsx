import { useEffect, useRef, useState } from 'react'
import {
  Palette, Check, ChevronDown, PanelLeft, PanelRight, Layout, Plus,
  Pencil, Trash2, X,
} from 'lucide-react'
import { useResumeStore } from '@/stores/resumeStore'
import type { Theme, ThemeDTO } from '@/types/resume'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog'
import { ConfirmDialogAction } from '@/components/ui/confirm-dialog'
import { SpotlightCard } from '@/components/effects/SpotlightCard'

const THEME_DOT: Record<string, string> = {
  classic: '#000000',
  modern: '#2563eb',
  minimal: '#999999',
  sidebar: '#1a365d',
  stackoverflow: '#f48024',
  elegant: '#1b4332',
  compact: '#555555',
}

function layoutGroup(layout: string): string {
  if (layout === 'sidebar-left' || layout === 'sidebar-right') return 'Two-Column'
  if (layout === 'header-bar') return 'Header Bar'
  return 'Single'
}

function LayoutIcon({ layout }: { layout: string }) {
  if (layout === 'sidebar-left') return <PanelLeft className="h-3.5 w-3.5 shrink-0 text-muted-foreground" />
  if (layout === 'sidebar-right') return <PanelRight className="h-3.5 w-3.5 shrink-0 text-muted-foreground" />
  if (layout === 'header-bar') return <Layout className="h-3.5 w-3.5 shrink-0 text-muted-foreground" />
  return null
}

export default function ThemeSelector() {
  const { themes, currentResume, fetchThemes, setTheme, createTheme, updateTheme, deleteTheme } = useResumeStore()
  const currentName = themes.find((t) => t.id === currentResume?.themeId)?.name ?? 'Theme'

  const [open, setOpen] = useState(false)
  const [showCreate, setShowCreate] = useState(false)
  const [editTheme, setEditTheme] = useState<Theme | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<Theme | null>(null)
  const [formName, setFormName] = useState('')
  const [formDescription, setFormDescription] = useState('')
  const [formLayout, setFormLayout] = useState('single')
  const [formCss, setFormCss] = useState('')
  const [saving, setSaving] = useState(false)
  const popoverRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    if (themes.length === 0) fetchThemes()
  }, [themes.length, fetchThemes])

  useEffect(() => {
    if (!open) return

    function onPointerDown(event: PointerEvent) {
      if (!popoverRef.current?.contains(event.target as Node)) setOpen(false)
    }

    document.addEventListener('pointerdown', onPointerDown)
    return () => document.removeEventListener('pointerdown', onPointerDown)
  }, [open])

  // Group themes by layout
  const grouped = themes.reduce<Record<string, Theme[]>>((acc, t) => {
    const group = layoutGroup(t.layout || 'single')
    if (!acc[group]) acc[group] = []
    acc[group].push(t)
    return acc
  }, {})

  function openCreate() {
    setOpen(false)
    setEditTheme(null)
    setFormName('')
    setFormDescription('')
    setFormLayout('single')
    setFormCss('')
    setShowCreate(true)
  }

  function openEdit(theme: Theme) {
    setOpen(false)
    setEditTheme(theme)
    setFormName(theme.name)
    setFormDescription(theme.description || '')
    setFormLayout(theme.layout || 'single')
    setFormCss('')
    setShowCreate(true)
  }

  async function handleSave() {
    setSaving(true)
    try {
      if (editTheme) {
        const dto: ThemeDTO = { name: formName, description: formDescription, layout: formLayout }
        if (formCss.trim()) dto.cssContent = formCss
        await updateTheme(editTheme.id, dto)
      } else {
        await createTheme({ name: formName, description: formDescription, layout: formLayout, cssContent: formCss || undefined })
      }
      setShowCreate(false)
    } catch (e) {
      // error handled by api interceptor
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return
    try {
      await deleteTheme(deleteTarget.id)
      setDeleteTarget(null)
    } catch {
      // error handled by api interceptor
    }
  }

  if (themes.length === 0) return null

  const groupOrders = ['Single', 'Two-Column', 'Header Bar']

  return (
    <>
      <div className="relative" ref={popoverRef}>
        <Button
          variant="outline"
          size="sm"
          aria-expanded={open}
          aria-haspopup="dialog"
          onClick={() => setOpen((value) => !value)}
        >
          <Palette className="h-4 w-4 mr-1.5" />
          {currentName}
          <ChevronDown className="h-3 w-3 ml-1 text-muted-foreground" />
        </Button>

        {open && (
          <div
            role="dialog"
            aria-label="Choose a theme"
            className="absolute left-0 top-full z-50 mt-2 w-[22rem] max-w-[calc(100vw-2rem)] rounded-xl border bg-white p-3 shadow-lg"
          >
            <div className="mb-3 flex items-center justify-between">
              <div>
                <p className="text-sm font-semibold">Choose a theme</p>
                <p className="text-xs text-muted-foreground">Preview layouts at a glance.</p>
              </div>
              <button
                type="button"
                className="rounded-md p-1 text-muted-foreground hover:bg-accent hover:text-accent-foreground"
                onClick={() => setOpen(false)}
                aria-label="Close theme selector"
              >
                <X className="h-4 w-4" />
              </button>
            </div>

          {groupOrders.map((group) => {
            const items = grouped[group]
            if (!items || items.length === 0) return null
            return (
              <div key={group} className="mb-3 last:mb-0">
                <div className="mb-1.5 px-0.5 text-xs font-semibold text-muted-foreground">{group}</div>
                <div className="grid grid-cols-1 gap-2 sm:grid-cols-2">
                  {items.map((theme) => (
                    <SpotlightCard key={theme.id} className="p-0">
                      <button
                        type="button"
                        className="block w-full p-3 text-left"
                        aria-label={`${theme.name}${currentResume?.themeId === theme.id ? ' selected' : ''}`}
                        onClick={() => {
                          setTheme(theme.id)
                          setOpen(false)
                        }}
                      >
                        <div className="mb-2 flex items-center justify-between gap-2">
                          <span
                            className="h-3 w-3 rounded-full border"
                            style={{ backgroundColor: THEME_DOT[theme.id] || '#94a3b8' }}
                          />
                          <span className="flex items-center gap-1">
                            <LayoutIcon layout={theme.layout} />
                            {currentResume?.themeId === theme.id && <Check className="h-4 w-4 text-primary" />}
                          </span>
                        </div>
                        <span className="block truncate text-sm font-medium">{theme.name}</span>
                        <span className="mt-1 block text-xs text-muted-foreground">
                          {theme.layout || 'single'}
                          {!theme.builtIn && <span className="ml-1">(Custom)</span>}
                        </span>
                      </button>
                      {!theme.builtIn && (
                        <div className="absolute bottom-2 right-2 z-20 flex gap-1">
                          <button
                            type="button"
                            className="rounded bg-white/90 p-1 text-muted-foreground shadow-sm hover:text-primary"
                            onClick={(e) => { e.stopPropagation(); openEdit(theme) }}
                            title="Edit"
                          >
                            <Pencil className="h-3 w-3" />
                          </button>
                          <button
                            type="button"
                            className="rounded bg-white/90 p-1 text-muted-foreground shadow-sm hover:text-destructive"
                            onClick={(e) => { e.stopPropagation(); setOpen(false); setDeleteTarget(theme) }}
                            title="Delete"
                          >
                            <Trash2 className="h-3 w-3" />
                          </button>
                        </div>
                      )}
                    </SpotlightCard>
                  ))}
                </div>
              </div>
            )
          })}
          <button
            type="button"
            onClick={openCreate}
            className="mt-2 flex w-full items-center rounded-lg border border-dashed px-3 py-2 text-sm hover:bg-accent hover:text-accent-foreground"
          >
            <Plus className="h-4 w-4 mr-2" />
            <span>Create Theme</span>
          </button>
          </div>
        )}
      </div>

      {/* Create/Edit Dialog */}
      <Dialog open={showCreate} onOpenChange={setShowCreate}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>{editTheme ? 'Edit Theme' : 'Create Custom Theme'}</DialogTitle>
            <DialogDescription>
              {editTheme ? 'Modify your custom theme settings.' : 'Create a new custom theme with your own CSS.'}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-3 mt-2">
            <div>
              <label className="text-sm font-medium block mb-1">Name *</label>
              <input
                className="w-full border rounded px-3 py-1.5 text-sm"
                value={formName}
                onChange={(e) => setFormName(e.target.value)}
                placeholder="My Theme"
              />
            </div>
            <div>
              <label className="text-sm font-medium block mb-1">Description</label>
              <input
                className="w-full border rounded px-3 py-1.5 text-sm"
                value={formDescription}
                onChange={(e) => setFormDescription(e.target.value)}
                placeholder="Optional description"
              />
            </div>
            <div>
              <label className="text-sm font-medium block mb-1">Layout</label>
              <select
                className="w-full border rounded px-3 py-1.5 text-sm"
                value={formLayout}
                onChange={(e) => setFormLayout(e.target.value)}
              >
                <option value="single">Single Column</option>
                <option value="sidebar-left">Two-Column (Sidebar Left)</option>
                <option value="sidebar-right">Two-Column (Sidebar Right)</option>
                <option value="header-bar">Header Bar</option>
              </select>
            </div>
            <div>
              <label className="text-sm font-medium block mb-1">CSS (optional)</label>
              <textarea
                className="w-full border rounded px-3 py-1.5 text-sm font-mono h-32"
                value={formCss}
                onChange={(e) => setFormCss(e.target.value)}
                placeholder="/* Custom CSS */"
              />
            </div>
          </div>
          <div className="flex justify-end gap-2 mt-4">
            <Button variant="outline" size="sm" onClick={() => setShowCreate(false)}>Cancel</Button>
            <Button size="sm" onClick={handleSave} disabled={!formName.trim() || saving}>
              {saving ? 'Saving...' : editTheme ? 'Update' : 'Create'}
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation */}
      {deleteTarget && (
        <ConfirmDialogAction
          open={!!deleteTarget}
          onOpenChange={(open) => { if (!open) setDeleteTarget(null) }}
          title="Delete Custom Theme"
          description={`Are you sure you want to delete "${deleteTarget.name}"? This action cannot be undone.`}
          confirmLabel="Delete"
          variant="destructive"
          onConfirm={handleDelete}
        />
      )}
    </>
  )
}
