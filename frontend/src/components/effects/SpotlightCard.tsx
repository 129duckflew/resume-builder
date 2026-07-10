import type { HTMLAttributes, MouseEvent, ReactNode } from 'react'
import { cn } from '@/lib/utils'

interface SpotlightCardProps extends HTMLAttributes<HTMLDivElement> {
  children: ReactNode
}

export function SpotlightCard({ children, className, onMouseMove, ...props }: SpotlightCardProps) {
  function handleMouseMove(event: MouseEvent<HTMLDivElement>) {
    const rect = event.currentTarget.getBoundingClientRect()
    event.currentTarget.style.setProperty('--spotlight-x', `${event.clientX - rect.left}px`)
    event.currentTarget.style.setProperty('--spotlight-y', `${event.clientY - rect.top}px`)
    onMouseMove?.(event)
  }

  return (
    <div
      data-slot="spotlight-card"
      onMouseMove={handleMouseMove}
      className={cn(
        'group relative overflow-hidden rounded-lg border bg-white transition-shadow hover:shadow-md',
        'before:pointer-events-none before:absolute before:inset-0 before:opacity-0 before:transition-opacity hover:before:opacity-100',
        'before:bg-[radial-gradient(180px_circle_at_var(--spotlight-x,50%)_var(--spotlight-y,50%),hsl(var(--primary)/0.12),transparent_60%)] motion-reduce:before:hidden',
        className,
      )}
      {...props}
    >
      <div className="relative z-10">{children}</div>
    </div>
  )
}
