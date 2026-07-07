import { useSortable } from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { GripVertical } from 'lucide-react'
import type { Section } from '@/types/resume'

interface Props {
  section: Section
  index: number
}

export default function SortableSection({ section, index }: Props) {
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
      className={`flex items-center gap-2 px-2 py-1.5 rounded text-sm
        ${isDragging ? 'bg-blue-50 border border-blue-200' : 'hover:bg-white'}`}
    >
      <button
        className="cursor-grab active:cursor-grabbing text-muted-foreground hover:text-foreground"
        {...attributes}
        {...listeners}
      >
        <GripVertical className="h-4 w-4" />
      </button>
      <span
        className={`truncate flex-1 ${
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
