import { create } from 'zustand'
import { http } from '@/lib/api'

interface AuthState {
  token: string | null
  username: string | null
  isAuthenticated: boolean
  login: (username: string, password: string) => Promise<void>
  register: (username: string, email: string, password: string) => Promise<void>
  logout: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  token: localStorage.getItem('token'),
  username: localStorage.getItem('username'),
  get isAuthenticated() {
    return !!this.token
  },

  login: async (username: string, password: string) => {
    const res = await http.post('/auth/login', { username, password })
    const { token, username: uname } = res.data
    localStorage.setItem('token', token)
    localStorage.setItem('username', uname)
    set({ token, username: uname, isAuthenticated: true })
  },

  register: async (username: string, email: string, password: string) => {
    const res = await http.post('/auth/register', { username, email, password })
    const { token, username: uname } = res.data
    localStorage.setItem('token', token)
    localStorage.setItem('username', uname)
    set({ token, username: uname, isAuthenticated: true })
  },

  logout: () => {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    set({ token: null, username: null, isAuthenticated: false })
  },
}))
