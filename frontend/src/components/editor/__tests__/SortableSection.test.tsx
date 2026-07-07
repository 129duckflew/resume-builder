import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { DndContext } from '@dnd-kit/core'
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable'
import SortableSection from '@/components/editor/SortableSection'
import type { Section } from '@/types/resume'

function section(overrides: Partial<Section> = {}): Section {
  return {
    id: 's0',
    type: 'heading',
    level: 2,
    title: 'Experience',
    content: '## Experience',
    startLine: 3,
    endLine: 8,
    ...overrides,
  }
}

function renderInDnd(section: Section, onClick?: (line: number) => void) {
  return render(
    <DndContext>
      <SortableContext items={[section.id]} strategy={verticalListSortingStrategy}>
        <SortableSection section={section} index={0} onClick={onClick} />
      </SortableContext>
    </DndContext>,
  )
}

describe('SortableSection', () => {
  it('renders section title', () => {
    renderInDnd(section())
    expect(screen.getByText('Experience')).toBeTruthy()
  })

  it('calls onClick with startLine when title is clicked', async () => {
    const onClick = vi.fn()
    renderInDnd(section({ startLine: 3 }), onClick)
    await userEvent.click(screen.getByText('Experience'))
    expect(onClick).toHaveBeenCalledWith(3)
  })

  it('does not call onClick when drag handle is clicked', async () => {
    const onClick = vi.fn()
    renderInDnd(section(), onClick)
    const handle = screen.getByLabelText('drag-handle')
    await userEvent.click(handle)
    expect(onClick).not.toHaveBeenCalled()
  })
})
