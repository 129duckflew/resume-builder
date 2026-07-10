import type { HTMLAttributes, ReactNode } from 'react'
import { cn } from '@/lib/utils'

interface GradientTextProps extends HTMLAttributes<HTMLSpanElement> {
  children: ReactNode
}

export function GradientText({ children, className, ...props }: GradientTextProps) {
  return (
    <span
      className={cn(
        'bg-gradient-to-r from-primary via-blue-500 to-primary bg-300% bg-clip-text text-transparent animate-gradient-shift motion-reduce:animate-none',
        className,
      )}
      {...props}
    >
      {children}
    </span>
  )
}
