import { useState, useCallback, useEffect } from 'react'
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
import { GripVertical, Plus } from 'lucide-react'
import { parseSections } from '@/lib/markdown'
import { useResumeStore } from '@/stores/resumeStore'
import SortableSection from './SortableSection'
import SectionTemplatePicker from './SectionTemplatePicker'
import type { SectionTemplate } from '@/types/sectionTemplate'

interface Props {
  markdown: string
  onSectionClick?: (lineNumber: number) => void
}

export default function SectionDragList({ markdown, onSectionClick }: Props) {
  const [sections, setSections] = useState(() => parseSections(markdown))
  const [showPicker, setShowPicker] = useState(false)
  const updateResume = useResumeStore((s) => s.updateResume)
  const currentResume = useResumeStore((s) => s.currentResume)

  useEffect(() => {
    setSections(parseSections(markdown))
  }, [markdown])

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

      const lines = markdown.split('\n')
      const result: string[] = []
      let currentLine = 0

      for (const section of newSections) {
        while (currentLine < section.startLine) {
          result.push(lines[currentLine])
          currentLine++
        }
        for (let i = section.startLine; i <= section.endLine; i++) {
          if (i < lines.length) result.push(lines[i])
        }
        currentLine = section.endLine + 1
      }
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

  const handleTemplateSelect = useCallback((template: SectionTemplate) => {
    if (!currentResume) return
    const newContent = currentResume.content
      ? currentResume.content + '\n\n' + template.prompt
      : template.prompt
    useResumeStore.setState({
      currentResume: { ...currentResume, content: newContent },
    })
    updateResume(currentResume.id, { content: newContent })
  }, [currentResume, updateResume])

  return (
    <>
      <div className="flex items-center justify-between mb-2">
        <h3 className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
          Sections
        </h3>
        <button
          onClick={() => setShowPicker(true)}
          className="text-xs text-muted-foreground hover:text-foreground flex items-center gap-0.5 transition-colors"
          title="Add Section"
        >
          <Plus className="h-3.5 w-3.5" />
          Add
        </button>
      </div>
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
      <SectionTemplatePicker open={showPicker} onOpenChange={setShowPicker} onSelect={handleTemplateSelect} />
    </>
  )
}
