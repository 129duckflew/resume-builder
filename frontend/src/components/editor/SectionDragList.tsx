import { useState, useCallback } from 'react'
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from '@dnd-kit/core'
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable'
import { restrictToVerticalAxis } from '@dnd-kit/modifiers'
import { GripVertical } from 'lucide-react'
import { parseSections } from '@/lib/markdown'
import { useResumeStore } from '@/stores/resumeStore'
import SortableSection from './SortableSection'

interface Props {
  markdown: string
  onSectionClick?: (lineNumber: number) => void
}

export default function SectionDragList({ markdown, onSectionClick }: Props) {
  const [sections, setSections] = useState(() => parseSections(markdown))
  const updateResume = useResumeStore((s) => s.updateResume)
  const currentResume = useResumeStore((s) => s.currentResume)

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  )

  const handleDragEnd = useCallback(
    (event: DragEndEvent) => {
      const { active, over } = event
      if (!over || active.id === over.id) return

      const oldIndex = sections.findIndex((s) => s.id === active.id)
      const newIndex = sections.findIndex((s) => s.id === over.id)

      if (oldIndex === -1 || newIndex === -1) return

      const newSections = arrayMove(sections, oldIndex, newIndex)
      setSections(newSections)

      // Rebuild markdown from reordered sections
      const lines = markdown.split('\n')
      const result: string[] = []
      let currentLine = 0

      for (const section of newSections) {
        // Add lines before this section (non-section content)
        while (currentLine < section.startLine) {
          result.push(lines[currentLine])
          currentLine++
        }
        // Add section content
        for (let i = section.startLine; i <= section.endLine; i++) {
          if (i < lines.length) result.push(lines[i])
        }
        currentLine = section.endLine + 1
      }
      // Add remaining lines after last section
      while (currentLine < lines.length) {
        result.push(lines[currentLine])
        currentLine++
      }

      const newContent = result.join('\n')
      if (currentResume) {
        useResumeStore.setState({
          currentResume: { ...currentResume, content: newContent },
        })
        updateResume(currentResume.id, { content: newContent })
      }
    },
    [sections, markdown, currentResume, updateResume],
  )

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCenter}
      onDragEnd={handleDragEnd}
      modifiers={[restrictToVerticalAxis]}
    >
      <SortableContext
        items={sections.map((s) => s.id)}
        strategy={verticalListSortingStrategy}
      >
        <div className="space-y-1">
          {sections.map((section, i) => (
            <SortableSection key={section.id} section={section} index={i} onClick={onSectionClick} />
          ))}
        </div>
      </SortableContext>
    </DndContext>
  )
}
