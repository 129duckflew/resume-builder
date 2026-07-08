export interface DesensitizeRule {
  id: number | null
  name: string
  description: string | null
  pattern: string
  replacement: string
  enabled: boolean
  defaultRule: boolean
  sortOrder: number
}
