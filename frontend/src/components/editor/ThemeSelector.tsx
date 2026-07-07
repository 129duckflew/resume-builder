import { useEffect } from 'react'
import { Palette, Check, ChevronDown } from 'lucide-react'
import { useResumeStore } from '@/stores/resumeStore'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from '@/components/ui/dropdown-menu'

const THEME_DOT: Record<string, string> = {
  classic: '#000000',
  modern: '#2563eb',
  minimal: '#999999',
  sidebar: '#1a365d',
  stackoverflow: '#f48024',
  elegant: '#1b4332',
  compact: '#555555',
}

export default function ThemeSelector() {
  const { themes, currentResume, fetchThemes, setTheme } = useResumeStore()
  const currentName = themes.find((t) => t.id === currentResume?.themeId)?.name ?? 'Theme'

  useEffect(() => {
    if (themes.length === 0) fetchThemes()
  }, [themes.length, fetchThemes])

  if (themes.length === 0) return null

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline" size="sm">
          <Palette className="h-4 w-4 mr-1.5" />
          {currentName}
          <ChevronDown className="h-3 w-3 ml-1 text-muted-foreground" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="start" className="w-48">
        {themes.map((theme) => (
          <DropdownMenuItem key={theme.id} onClick={() => setTheme(theme.id)}>
            <span
              className="w-2 h-2 rounded-full mr-2.5 shrink-0"
              style={{ backgroundColor: THEME_DOT[theme.id] ?? '#888' }}
            />
            <span className="flex-1">{theme.name}</span>
            {currentResume?.themeId === theme.id && (
              <Check className="h-4 w-4 text-primary shrink-0" />
            )}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
