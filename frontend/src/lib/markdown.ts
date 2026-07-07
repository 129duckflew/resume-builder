import type { Section } from '@/types/resume'

const HEADING_RE = /^(#{1,6})\s+(.+)$/

export function parseSections(markdown: string): Section[] {
  const sections: Section[] = []
  const lines = markdown.split('\n')
  let current: Section | null = null

  for (let i = 0; i < lines.length; i++) {
    const match = lines[i].match(HEADING_RE)
    if (match) {
      if (current) {
        current.endLine = i - 1
      }
      current = {
        id: `section-${sections.length}`,
        type: 'heading',
        level: match[1].length,
        title: match[2],
        content: lines[i],
        startLine: i,
        endLine: i,
      }
      sections.push(current)
    }
  }

  if (current) {
    current.endLine = lines.length - 1
  }

  return sections
}

export function reorderSections(
  markdown: string,
  sections: Section[],
  fromIndex: number,
  toIndex: number,
): string {
  const lines = markdown.split('\n')

  // Split document into alternating non-section / section blocks
  const blocks: { lines: string[]; isSection: boolean }[] = []
  let prevEnd = 0

  for (const section of sections) {
    if (section.startLine > prevEnd) {
      blocks.push({
        lines: lines.slice(prevEnd, section.startLine),
        isSection: false,
      })
    }
    blocks.push({
      lines: lines.slice(section.startLine, section.endLine + 1),
      isSection: true,
    })
    prevEnd = section.endLine + 1
  }

  if (prevEnd < lines.length) {
    blocks.push({ lines: lines.slice(prevEnd), isSection: false })
  }

  // Reorder only section blocks
  const sectionBlocks = blocks.filter((b) => b.isSection)
  const [moved] = sectionBlocks.splice(fromIndex, 1)
  sectionBlocks.splice(toIndex, 0, moved)

  // Reassemble
  let si = 0
  const result: string[] = []
  for (const block of blocks) {
    if (block.isSection) {
      result.push(...sectionBlocks[si].lines)
      si++
    } else {
      result.push(...block.lines)
    }
  }

  return result.join('\n')
}
