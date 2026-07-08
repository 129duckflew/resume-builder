import axios from 'axios'
import type { DesensitizeRule } from '@/types/desensitize'
import type { Resume } from '@/types/resume'
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
