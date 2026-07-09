export interface Resume {
  id: string
  title: string
  content: string
  themeId: string
  fontSize: number | null
  lineHeight: number | null
  sectionSpacing: string
  createdAt: string
  updatedAt: string
}

export interface Theme {
  id: string
  name: string
  description: string
  builtIn: boolean
  layout: string
  userId?: number | null
  variablesSchema?: string
}

export interface ThemeDTO {
  name?: string
  description?: string
  cssContent?: string
  variablesSchema?: string
  layout?: string
}

export interface VariableDeclaration {
  name: string
  type: string
  defaultValue: string
  label: string
  group?: string
  options?: { label: string; value: string }[]
}

export interface Section {
  id: string
  type: 'heading'
  level: number
  title: string
  content: string
  startLine: number
  endLine: number
}

export interface ResumeStyle {
  id: number | null
  resumeId: string
  themeId: string
  fontSize: number | null
  lineHeight: number | null
  sectionSpacing: string | null
  customVariables?: string
}

export interface ShareLink {
  id: string
  resumeId: string
  enabled: boolean
  desensitize: boolean
  expiresAt: string | null
  createdAt: string
}

export interface ResumeVersion {
  id: number
  resumeId: string
  versionNumber: number
  title: string
  content: string | null
  themeId: string
  fontSize: number | null
  lineHeight: number | null
  sectionSpacing: string | null
  createdAt: string
}
