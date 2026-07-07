import { useEffect } from 'react'
import { Palette } from 'lucide-react'
import { useResumeStore } from '@/stores/resumeStore'
import { Button } from '@/components/ui/button'

export default function ThemeSelector() {
  const { themes, currentResume, fetchThemes, setTheme } = useResumeStore()

  useEffect(() => {
    if (themes.length === 0) fetchThemes()
  }, [themes.length, fetchThemes])

  if (themes.length === 0) return null

  return (
    <div className="flex items-center gap-2">
      <Palette className="h-4 w-4 text-muted-foreground" />
      <div className="flex gap-1">
        {themes.map((theme) => (
          <button
            key={theme.id}
            onClick={() => setTheme(theme.id)}
            className={`px-3 py-1.5 text-xs rounded-md border transition-colors
              ${
                currentResume?.themeId === theme.id
                  ? 'bg-primary text-primary-foreground border-primary'
                  : 'bg-white hover:bg-accent border-input'
              }`}
          >
            {theme.name}
          </button>
        ))}
      </div>
    </div>
  )
}
