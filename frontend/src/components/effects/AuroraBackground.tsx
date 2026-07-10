import type { HTMLAttributes } from 'react'
import { cn } from '@/lib/utils'

export function AuroraBackground({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      aria-hidden="true"
      className={cn('pointer-events-none absolute inset-0 overflow-hidden', className)}
      {...props}
    >
      <div className="absolute -top-28 left-1/2 h-72 w-[42rem] -translate-x-1/2 rounded-full bg-primary/10 blur-3xl animate-pulse motion-reduce:animate-none" />
      <div className="absolute bottom-10 left-10 h-64 w-64 rounded-full bg-blue-400/10 blur-3xl animate-gradient-shift motion-reduce:animate-none" />
      <div className="absolute right-8 top-24 h-56 w-56 rounded-full bg-slate-400/15 blur-3xl animate-icon-pulse motion-reduce:animate-none" />
    </div>
  )
}
