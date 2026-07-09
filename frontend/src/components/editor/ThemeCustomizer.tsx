import { useResumeStore } from '@/stores/resumeStore'
import { Button } from '@/components/ui/button'

const FONT_OPTIONS = [
  { label: 'System UI (Sans-serif)', value: 'system-ui, -apple-system, sans-serif' },
  { label: 'Inter', value: "'Inter', 'Helvetica Neue', Arial, sans-serif" },
  { label: 'Helvetica Neue', value: "'Helvetica Neue', Helvetica, Arial, sans-serif" },
  { label: 'Times New Roman (Serif)', value: "'Times New Roman', Times, serif" },
  { label: 'Georgia (Serif)', value: "'Georgia', 'Palatino Linotype', serif" },
  { label: 'Custom...', value: '__custom__' },
]

const GROUP_ORDER = ['Colors', 'Typography', 'Layout']
const GROUP_LABELS: Record<string, string> = {
  Colors: 'Colors',
  Typography: 'Typography',
  Layout: 'Layout',
}

export default function ThemeCustomizer() {
  const {
    currentThemeVariables,
    customVariables,
    updateCustomVariable,
    resetCustomVariables,
  } = useResumeStore()

  if (!currentThemeVariables || currentThemeVariables.length === 0) return null

  // Group variables
  const grouped: Record<string, typeof currentThemeVariables> = {}
  for (const v of currentThemeVariables) {
    const g = v.group || 'Other'
    if (!grouped[g]) grouped[g] = []
    grouped[g].push(v)
  }

  const getValue = (v: typeof currentThemeVariables[0]) => {
    return customVariables[v.name] ?? v.defaultValue ?? ''
  }

  const handleChange = (name: string, value: string) => {
    updateCustomVariable(name, value)
  }

  const renderControl = (v: typeof currentThemeVariables[0]) => {
    const value = getValue(v)

    switch (v.type) {
      case 'color':
        return (
          <div key={v.name} className="flex items-center justify-between gap-2 py-1.5">
            <label className="text-xs text-muted-foreground flex-1">{v.label || v.name}</label>
            <div className="flex items-center gap-1.5">
              <input
                type="color"
                value={value}
                onChange={(e) => handleChange(v.name, e.target.value)}
                className="w-7 h-7 p-0 border rounded cursor-pointer"
              />
              <input
                type="text"
                value={value}
                onChange={(e) => handleChange(v.name, e.target.value)}
                className="w-20 h-7 text-xs border rounded px-1 bg-background"
              />
            </div>
          </div>
        )

      case 'font':
        return (
          <div key={v.name} className="flex items-center justify-between gap-2 py-1.5">
            <label className="text-xs text-muted-foreground flex-1">{v.label || v.name}</label>
            <select
              value={value}
              onChange={(e) => {
                const val = e.target.value
                if (val === '__custom__') {
                  const custom = prompt('Enter custom font family:')
                  if (custom) handleChange(v.name, custom)
                } else {
                  handleChange(v.name, val)
                }
              }}
              className="w-36 h-7 text-xs border rounded px-1 bg-background"
            >
              {FONT_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
          </div>
        )

      case 'size':
        return (
          <div key={v.name} className="flex items-center justify-between gap-2 py-1.5">
            <label className="text-xs text-muted-foreground flex-1">{v.label || v.name}</label>
            <input
              type="text"
              value={value}
              onChange={(e) => handleChange(v.name, e.target.value)}
              className="w-24 h-7 text-xs border rounded px-1 bg-background text-right"
            />
          </div>
        )

      default:
        // select type with options
        if (v.options && v.options.length > 0) {
          return (
            <div key={v.name} className="flex items-center justify-between gap-2 py-1.5">
              <label className="text-xs text-muted-foreground flex-1">{v.label || v.name}</label>
              <select
                value={value}
                onChange={(e) => handleChange(v.name, e.target.value)}
                className="w-36 h-7 text-xs border rounded px-1 bg-background"
              >
                {v.options.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </div>
          )
        }
        // fallback: text input
        return (
          <div key={v.name} className="flex items-center justify-between gap-2 py-1.5">
            <label className="text-xs text-muted-foreground flex-1">{v.label || v.name}</label>
            <input
              type="text"
              value={value}
              onChange={(e) => handleChange(v.name, e.target.value)}
              className="w-24 h-7 text-xs border rounded px-1 bg-background text-right"
            />
          </div>
        )
    }
  }

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <h4 className="text-sm font-medium">Theme Customization</h4>
        <Button variant="ghost" size="sm" onClick={resetCustomVariables} className="h-7 text-xs">
          Reset
        </Button>
      </div>
      {GROUP_ORDER.filter((g) => grouped[g]).map((group) => (
        <div key={group}>
          <h5 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1">
            {GROUP_LABELS[group] || group}
          </h5>
          <div className="border rounded-md px-3 py-1.5 bg-card">
            {grouped[group].map((v) => renderControl(v))}
          </div>
        </div>
      ))}
      {/* Other groups not in GROUP_ORDER */}
      {Object.keys(grouped)
        .filter((g) => !GROUP_ORDER.includes(g))
        .map((group) => (
          <div key={group}>
            <h5 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1">
              {group}
            </h5>
            <div className="border rounded-md px-3 py-1.5 bg-card">
              {grouped[group].map((v) => renderControl(v))}
            </div>
          </div>
        ))}
    </div>
  )
}
