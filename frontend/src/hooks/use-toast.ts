import { useState, useCallback } from 'react'

export interface Toast {
  id: string
  title?: string
  description?: string
  variant?: 'default' | 'success' | 'destructive'
}

let toastCount = 0

// Singleton store outside React to allow usage from anywhere
let globalToasts: Toast[] = []
let globalListeners: Array<(toasts: Toast[]) => void> = []

function notifyListeners() {
  const copy = [...globalToasts]
  globalListeners.forEach((l) => l(copy))
}

export function toast({ title, description, variant = 'default' }: Omit<Toast, 'id'>) {
  const id = String(++toastCount)
  globalToasts = [...globalToasts, { id, title, description, variant }]
  notifyListeners()

  setTimeout(() => {
    globalToasts = globalToasts.filter((t) => t.id !== id)
    notifyListeners()
  }, 4000)
}

export function useToast() {
  const [toasts, setToasts] = useState<Toast[]>([])

  useState(() => {
    globalListeners.push(setToasts)
    return () => {
      globalListeners = globalListeners.filter((l) => l !== setToasts)
    }
  })

  const dismiss = useCallback((id: string) => {
    globalToasts = globalToasts.filter((t) => t.id !== id)
    notifyListeners()
  }, [])

  return { toasts, toast, dismiss }
}
