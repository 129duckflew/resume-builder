import axios from 'axios'
import type { Resume } from '@/types/resume'

const http = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

export const resumeApi = {
  list: () => http.get<Resume[]>('/resumes').then(r => r.data),

  get: (id: string) => http.get<Resume>(`/resumes/${id}`).then(r => r.data),

  create: (data: Partial<Resume>) =>
    http.post<Resume>('/resumes', data).then(r => r.data),

  update: (id: string, data: Partial<Resume>) =>
    http.put<Resume>(`/resumes/${id}`, data).then(r => r.data),

  delete: (id: string) => http.delete(`/resumes/${id}`),

  preview: (id: string) =>
    http.post<string>(`/resumes/${id}/preview`).then(r => r.data),

  exportHtml: (id: string) =>
    http.post(`/resumes/${id}/export/html`, null, { responseType: 'blob' })
      .then(r => {
        const url = URL.createObjectURL(new Blob([r.data], { type: 'text/html' }))
        const a = document.createElement('a')
        a.href = url
        a.download = 'resume.html'
        a.click()
        URL.revokeObjectURL(url)
      }),

  exportPdf: (id: string, smart: boolean = true) =>
    http.post(`/resumes/${id}/export/pdf?smartOnePage=${smart}`, null,
      { responseType: 'blob' })
      .then(r => {
        const url = URL.createObjectURL(new Blob([r.data], { type: 'application/pdf' }))
        const a = document.createElement('a')
        a.href = url
        a.download = 'resume.pdf'
        a.click()
        URL.revokeObjectURL(url)
      }),
}

export const themeApi = {
  list: () => http.get('/themes').then(r => r.data),

  getCss: (id: string) =>
    http.get(`/themes/${id}/css`, { responseType: 'text' }).then(r => r.data),
}
