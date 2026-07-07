import type { Section } from '@/types/resume'

const HEADING_RE = /^(#{1,6})\s+(.+)$/gm

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
  const ordered = [...sections]
  const [moved] = ordered.splice(fromIndex, 1)
  ordered.splice(toIndex, 0, moved)

  const result: string[] = []
  let insertBefore = ordered[0]?.startLine ?? 0

  for (const section of ordered) {
    for (let i = insertBefore; i <= section.endLine; i++) {
      if (i < lines.length) {
        result.push(lines[i])
      }
    }
    insertBefore = section.endLine + 1
  }

  return result.join('\n')
}
