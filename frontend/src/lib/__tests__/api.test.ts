import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

const { mockToast, mockLogout } = vi.hoisted(() => ({
  mockToast: vi.fn(),
  mockLogout: vi.fn(),
}))

// Mock toast (static import in api.ts)
vi.mock('@/hooks/use-toast', () => ({
  toast: mockToast,
}))

// Mock authStore (dynamic import inside interceptor to avoid circular dep)
vi.mock('@/stores/authStore', () => ({
  useAuthStore: {
    getState: () => ({ logout: mockLogout }),
  },
}))

import { http } from '@/lib/api'

function getErrorHandler(): ((err: any) => Promise<any>) | undefined {
  const handlers = (http.interceptors.response as any).handlers
  if (!handlers || !handlers.length) return undefined
  return handlers[0].rejected
}

describe('API response interceptor — 401 handling', () => {
  let store: Record<string, string>
  let removeItemSpy: any
  let locationHref: string

  beforeEach(() => {
    vi.clearAllMocks()
    store = {}

    // Define localStorage on globalThis (unavailable in this jsdom env)
    const mockStorage: Storage = {
      get length() {
        return Object.keys(store).length
      },
      key: (index: number) => Object.keys(store)[index] ?? null,
      getItem: (key: string) => store[key] ?? null,
      setItem: (key: string, val: string) => {
        store[key] = val
      },
      removeItem: (key: string) => {
        delete store[key]
      },
      clear: () => {
        store = {}
      },
    }
    globalThis.localStorage = mockStorage
    removeItemSpy = vi.spyOn(mockStorage, 'removeItem')

    // Mock window.location.href
    locationHref = ''
    Object.defineProperty(window, 'location', {
      configurable: true,
      value: {
        get href() {
          return locationHref
        },
        set href(v: string) {
          locationHref = v
        },
      },
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('test 1: shows toast, clears auth, and redirects on 401 for non-auth endpoint', async () => {
    // Pre-set token in mock store
    store['token'] = 'fake-token'
    store['username'] = 'testuser'

    const handler = getErrorHandler()
    expect(handler).toBeDefined()

    const error = {
      response: { status: 401 },
      config: { url: '/resumes' },
    }

    await expect(handler!(error)).rejects.toBe(error)

    expect(mockToast).toHaveBeenCalledTimes(1)
    expect(mockToast).toHaveBeenCalledWith({
      title: '登录已过期',
      description: '您的登录状态已失效，请重新登录',
      variant: 'destructive',
    })

    expect(removeItemSpy).toHaveBeenCalledWith('token')
    expect(removeItemSpy).toHaveBeenCalledWith('username')
    expect(mockLogout).toHaveBeenCalledTimes(1)
    expect(window.location.href).toBe('/login')
  })

  it('test 2: does NOT intercept 401 on /auth/** endpoints', async () => {
    const handler = getErrorHandler()
    expect(handler).toBeDefined()

    const error = {
      response: { status: 401 },
      config: { url: '/auth/login' },
    }

    await expect(handler!(error)).rejects.toBe(error)

    expect(mockToast).not.toHaveBeenCalled()
    expect(removeItemSpy).not.toHaveBeenCalled()
    expect(mockLogout).not.toHaveBeenCalled()
    expect(window.location.href).not.toBe('/login')
  })

  it('test 3: does NOT intercept non-401 errors (e.g. 500)', async () => {
    const handler = getErrorHandler()
    expect(handler).toBeDefined()

    const error = {
      response: { status: 500 },
      config: { url: '/resumes' },
    }

    await expect(handler!(error)).rejects.toBe(error)

    expect(mockToast).not.toHaveBeenCalled()
    expect(removeItemSpy).not.toHaveBeenCalled()
    expect(mockLogout).not.toHaveBeenCalled()
    expect(window.location.href).not.toBe('/login')
  })

  it('test 4: safely passes through network errors without response (e.g. timeout, DNS failure)', async () => {
    const handler = getErrorHandler()
    expect(handler).toBeDefined()

    const error = {
      config: { url: '/resumes' },
    }

    await expect(handler!(error)).rejects.toBe(error)

    expect(mockToast).not.toHaveBeenCalled()
    expect(removeItemSpy).not.toHaveBeenCalled()
    expect(mockLogout).not.toHaveBeenCalled()
    expect(window.location.href).not.toBe('/login')
  })
})
