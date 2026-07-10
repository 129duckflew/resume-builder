import type { ReactNode } from 'react'

interface FadeInProps {
  children: ReactNode
}

export function FadeIn({ children }: FadeInProps) {
  return <div className="animate-fade-in-up motion-reduce:animate-none">{children}</div>
}
