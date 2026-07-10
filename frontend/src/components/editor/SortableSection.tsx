import { useSortable } from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { GripVertical } from 'lucide-react'
import type { Section } from '@/types/resume'

interface Props {
  section: Section
  index: number
  onClick?: (startLine: number) => void
}

export default function SortableSection({ section, index, onClick }: Props) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: section.id })

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  }

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={`group flex items-center gap-2 px-2 py-1.5 rounded text-sm border border-transparent transition-all duration-150
        ${isDragging
          ? 'bg-blue-50 border-blue-200 shadow-sm'
          : 'hover:bg-white hover:border-blue-200/50 hover:shadow-sm'}`}
    >
      <button
        aria-label="drag-handle"
        className="cursor-grab active:cursor-grabbing text-muted-foreground hover:text-foreground opacity-30 group-hover:opacity-100 transition-opacity"
        {...attributes}
        {...listeners}
      >
        <GripVertical className="h-4 w-4" />
      </button>
      <span
        role="button"
        tabIndex={0}
        onClick={() => onClick?.(section.startLine)}
        onKeyDown={(e) => { if (e.key === 'Enter') onClick?.(section.startLine) }}
        className={`truncate flex-1 cursor-pointer hover:text-blue-600 transition-colors ${
          section.level === 1
            ? 'font-semibold'
            : section.level === 2
              ? 'font-medium'
              : 'text-muted-foreground'
        }`}
      >
        {section.title}
      </span>
      <span className="text-xs text-muted-foreground">
        H{section.level}
      </span>
    </div>
  )
}
