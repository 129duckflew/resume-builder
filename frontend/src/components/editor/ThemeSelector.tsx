import { useEffect, useState } from 'react'
import {
  Palette, Check, ChevronDown, PanelLeft, PanelRight, Layout, Plus,
  Pencil, Trash2, X,
} from 'lucide-react'
import { useResumeStore } from '@/stores/resumeStore'
import type { Theme, ThemeDTO } from '@/types/resume'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuGroup,
} from '@/components/ui/dropdown-menu'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog'
import { ConfirmDialogAction } from '@/components/ui/confirm-dialog'

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
  if (layout === 'sidebar-left') return <PanelLeft className="h-3.5 w-3.5 mr-2 shrink-0 text-muted-foreground" />
  if (layout === 'sidebar-right') return <PanelRight className="h-3.5 w-3.5 mr-2 shrink-0 text-muted-foreground" />
  if (layout === 'header-bar') return <Layout className="h-3.5 w-3.5 mr-2 shrink-0 text-muted-foreground" />
  return null
}

export default function ThemeSelector() {
  const { themes, currentResume, fetchThemes, setTheme, createTheme, updateTheme, deleteTheme } = useResumeStore()
  const currentName = themes.find((t) => t.id === currentResume?.themeId)?.name ?? 'Theme'

  const [showCreate, setShowCreate] = useState(false)
  const [editTheme, setEditTheme] = useState<Theme | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<Theme | null>(null)
  const [formName, setFormName] = useState('')
  const [formDescription, setFormDescription] = useState('')
  const [formLayout, setFormLayout] = useState('single')
  const [formCss, setFormCss] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (themes.length === 0) fetchThemes()
  }, [themes.length, fetchThemes])

  // Group themes by layout
  const grouped = themes.reduce<Record<string, Theme[]>>((acc, t) => {
    const group = layoutGroup(t.layout || 'single')
    if (!acc[group]) acc[group] = []
    acc[group].push(t)
    return acc
  }, {})

  function openCreate() {
    setEditTheme(null)
    setFormName('')
    setFormDescription('')
    setFormLayout('single')
    setFormCss('')
    setShowCreate(true)
  }

  function openEdit(theme: Theme) {
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
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="outline" size="sm">
            <Palette className="h-4 w-4 mr-1.5" />
            {currentName}
            <ChevronDown className="h-3 w-3 ml-1 text-muted-foreground" />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="start" className="w-56">
          {groupOrders.map((group) => {
            const items = grouped[group]
            if (!items || items.length === 0) return null
            return (
              <div key={group}>
                <DropdownMenuLabel>{group}</DropdownMenuLabel>
                <DropdownMenuGroup>
                  {items.map((theme) => (
                    <DropdownMenuItem key={theme.id} onClick={() => setTheme(theme.id)} className="pr-1">
                      <LayoutIcon layout={theme.layout} />
                      <span className="flex-1 truncate">
                        {theme.name}
                        {!theme.builtIn && <span className="text-xs text-muted-foreground ml-1">(Custom)</span>}
                      </span>
                      {currentResume?.themeId === theme.id && (
                        <Check className="h-4 w-4 text-primary shrink-0" />
                      )}
                      {!theme.builtIn && (
                        <span className="flex shrink-0 ml-1">
                          <button
                            className="p-0.5 hover:text-primary rounded"
                            onClick={(e) => { e.stopPropagation(); e.preventDefault(); openEdit(theme) }}
                            title="Edit"
                          >
                            <Pencil className="h-3 w-3" />
                          </button>
                          <button
                            className="p-0.5 hover:text-destructive rounded ml-0.5"
                            onClick={(e) => { e.stopPropagation(); e.preventDefault(); setDeleteTarget(theme) }}
                            title="Delete"
                          >
                            <Trash2 className="h-3 w-3" />
                          </button>
                        </span>
                      )}
                    </DropdownMenuItem>
                  ))}
                </DropdownMenuGroup>
                <DropdownMenuSeparator />
              </div>
            )
          })}
          <DropdownMenuItem onClick={openCreate}>
            <Plus className="h-4 w-4 mr-2" />
            <span>Create Theme</span>
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>

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
