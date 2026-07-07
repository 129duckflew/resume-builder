import { describe, it, expect } from 'vitest'
import { parseSections, reorderSections } from '@/lib/markdown'

describe('parseSections', () => {
  it('returns sections for h1 and h2 headings', () => {
    const md = `# Title\n\n## Experience\nfoo\n## Education\nbar`
    const sections = parseSections(md)
    expect(sections).toHaveLength(3)
    expect(sections[0].title).toBe('Title')
    expect(sections[0].level).toBe(1)
    expect(sections[1].title).toBe('Experience')
    expect(sections[1].level).toBe(2)
    expect(sections[2].title).toBe('Education')
    expect(sections[2].level).toBe(2)
  })

  it('returns empty array when no headings', () => {
    expect(parseSections('plain text\nno headings')).toEqual([])
  })

  it('handles empty string', () => {
    expect(parseSections('')).toEqual([])
  })

  it('captures correct line ranges', () => {
    const md = '# A\n\n## B\nb1\nb2\n\n## C\nc1'
    const sections = parseSections(md)
    expect(sections[0].startLine).toBe(0)
    expect(sections[0].endLine).toBe(1)
    expect(sections[1].startLine).toBe(2)
    expect(sections[1].endLine).toBe(5)
    expect(sections[2].startLine).toBe(6)
    expect(sections[2].endLine).toBe(7)
  })
})

describe('reorderSections', () => {
  it('swaps two sections and rebuilds markdown', () => {
    const md = `# Header

## B
b-content

## A
a-content`
    const sections = parseSections(md)
    const result = reorderSections(md, sections, 1, 2)
    expect(result).toContain('## A')
    expect(result).toContain('## B')
    expect(result.indexOf('## A')).toBeLessThan(result.indexOf('## B'))
  })

  it('moves first section to end', () => {
    const md = `## First
content1
## Second
content2
## Third
content3`
    const sections = parseSections(md)
    const result = reorderSections(md, sections, 0, 2)
    expect(result.indexOf('## Second')).toBeLessThan(result.indexOf('## Third'))
    expect(result.indexOf('## Third')).toBeLessThan(result.indexOf('## First'))
  })

  it('preserves non-section content before the first heading', () => {
    const md = `# Title

## Exp
e1

## Edu
e2`
    const sections = parseSections(md)
    const result = reorderSections(md, sections, 1, 2)
    expect(result).toContain('# Title')
  })
})
