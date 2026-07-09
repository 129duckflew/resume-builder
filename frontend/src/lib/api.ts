import axios from 'axios'
import type { DesensitizeRule } from '@/types/desensitize'
import type { Resume, ResumeStyle, ResumeVersion, ShareLink, VariableDeclaration, ThemeDTO } from '@/types/resume'
import type { SectionTemplate } from '@/types/sectionTemplate'

export const http = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  },
)

function blobDownload(data: Blob, filename: string) {
  const url = URL.createObjectURL(new Blob([data], { type: data.type }))
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

export const resumeApi = {
  list: () => http.get<Resume[]>('/resumes').then(r => r.data),

  get: (id: string) => http.get<Resume>(`/resumes/${id}`).then(r => r.data),

  create: (data: Partial<Resume>) =>
    http.post<Resume>('/resumes', data).then(r => r.data),

  update: (id: string, data: Partial<Resume>) =>
    http.put<Resume>(`/resumes/${id}`, data).then(r => r.data),

  delete: (id: string) => http.delete(`/resumes/${id}`),

  preview: (id: string, smartOnePage: boolean = false, desensitize: boolean = false) =>
    http.post<string>(`/resumes/${id}/preview?smartOnePage=${smartOnePage}&desensitize=${desensitize}`).then(r => r.data),

  exportHtml: (id: string, smartOnePage: boolean = false, desensitize: boolean = false) =>
    http.post(`/resumes/${id}/export/html?smartOnePage=${smartOnePage}&desensitize=${desensitize}`, null, { responseType: 'blob' })
      .then(r => blobDownload(r.data, 'resume.html')),

  exportPdf: (id: string, smart: boolean = true, desensitize: boolean = false) =>
    http.post(`/resumes/${id}/export/pdf?smartOnePage=${smart}&desensitize=${desensitize}`, null,
      { responseType: 'blob' })
      .then(r => blobDownload(r.data, 'resume.pdf')),
}

export const themeApi = {
  list: () => http.get('/themes').then(r => r.data),

  getCss: (id: string) =>
    http.get(`/themes/${id}/css`, { responseType: 'text' }).then(r => r.data),

  getVariables: (id: string) =>
    http.get<VariableDeclaration[]>(`/themes/${id}/variables`).then(r => r.data),

  create: (data: ThemeDTO) =>
    http.post('/themes', data).then(r => r.data),

  update: (id: string, data: ThemeDTO) =>
    http.put(`/themes/${id}`, data).then(r => r.data),

  delete: (id: string) =>
    http.delete(`/themes/${id}`),
}

export const desensitizeApi = {
  getRules: () =>
    http.get<DesensitizeRule[]>('/users/desensitize-rules').then(r => r.data),

  saveRules: (rules: DesensitizeRule[]) =>
    http.put('/users/desensitize-rules', rules),
}

export const sectionTemplateApi = {
  list: () =>
    http.get<SectionTemplate[]>('/section-templates').then(r => r.data),

  create: (template: Partial<SectionTemplate>) =>
    http.post<SectionTemplate>('/section-templates', template).then(r => r.data),

  update: (id: number, template: Partial<SectionTemplate>) =>
    http.put<SectionTemplate>(`/section-templates/${id}`, template).then(r => r.data),

  delete: (id: number) =>
    http.delete(`/section-templates/${id}`),
}

export const aiApi = {
  rewrite: (resumeId: string, instruction: string) =>
    http.post<{ content: string }>(`/resumes/${resumeId}/ai/rewrite`, { instruction }).then(r => r.data),

  suggest: (resumeId: string, jobDescription: string) =>
    http.post<{ content: string }>(`/resumes/${resumeId}/ai/suggest`, { jobDescription }).then(r => r.data),
}

export const userApi = {
  getApiKey: () =>
    http.get<{ apiKey: string }>('/users/api-key').then(r => r.data),

  updateApiKey: (apiKey: string) =>
    http.put('/users/api-key', { apiKey }),
}

export const shareApi = {
  list: (resumeId: string) =>
    http.get<ShareLink[]>(`/resumes/${resumeId}/shares`).then(r => r.data),

  create: (resumeId: string, desensitize: boolean = false) =>
    http.post<ShareLink>(`/resumes/${resumeId}/shares`, { desensitize }).then(r => r.data),

  delete: (linkId: string) =>
    http.delete(`/shares/${linkId}`),
}

export const versionApi = {
  list: (resumeId: string) =>
    http.get<ResumeVersion[]>(`/resumes/${resumeId}/versions`).then(r => r.data),

  get: (resumeId: string, version: number) =>
    http.get<ResumeVersion>(`/resumes/${resumeId}/versions/${version}`).then(r => r.data),

  restore: (resumeId: string, version: number) =>
    http.post<Resume>(`/resumes/${resumeId}/versions/${version}/restore`).then(r => r.data),
}

export const styleApi = {
  getStyle: (resumeId: string, themeId: string) =>
    http.get<ResumeStyle>(`/resumes/${resumeId}/styles`, { params: { themeId } })
      .then(r => r.status === 204 ? null : r.data),

  saveStyle: (resumeId: string, themeId: string, data: {
    fontSize?: number | null
    lineHeight?: number | null
    sectionSpacing?: string | null
    customVariables?: Record<string, string>
  }) =>
    http.put<ResumeStyle>(`/resumes/${resumeId}/styles`, data, { params: { themeId } }).then(r => r.data),
}

export const jsonResumeApi = {
  importJson: (data: any) =>
    http.post('/resumes/import/json', data).then(r => r.data),

  exportJson: (id: string) =>
    http.get(`/resumes/${id}/export/json`).then(r => r.data),
}
