import * as React from 'react'
import { Slot } from '@radix-ui/react-slot'
import { cva, type VariantProps } from 'class-variance-authority'
import { cn } from '@/lib/utils'

const buttonVariants = cva(
  'inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50',
  {
    variants: {
      variant: {
        default: 'bg-primary text-primary-foreground shadow hover:bg-primary/90',
        destructive: 'bg-destructive text-destructive-foreground shadow-sm hover:bg-destructive/90',
        outline: 'border border-input bg-background shadow-sm transition-all duration-300 hover:bg-accent hover:text-accent-foreground motion-reduce:transition-none',
        secondary: 'bg-secondary text-secondary-foreground shadow-sm hover:bg-secondary/80',
        ghost: 'hover:bg-accent hover:text-accent-foreground',
        link: 'text-primary underline-offset-4 hover:underline',
        shimmer:
          'relative overflow-hidden bg-primary text-primary-foreground shadow ' +
          'hover:bg-primary/90 ' +
          'before:pointer-events-none before:absolute before:inset-0 before:-translate-x-full before:animate-shimmer ' +
          'before:bg-[linear-gradient(90deg,transparent,rgba(255,255,255,0.2),transparent)] ' +
          'motion-reduce:before:hidden',
      },
      size: {
        default: 'h-9 px-4 py-2',
        sm: 'h-8 rounded-md px-3 text-xs',
        lg: 'h-10 rounded-md px-8',
        icon: 'h-9 w-9',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  },
)

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, onClick, ...props }, ref) => {
    const ripples = React.useRef<Array<{ id: number; x: number; y: number }>>([])
    const [ticks, setTicks] = React.useState(0)
    const prefersReduced =
      typeof window !== 'undefined'
        ? window.matchMedia('(prefers-reduced-motion: reduce)').matches
        : true

    const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
      if (!prefersReduced) {
        const rect = event.currentTarget.getBoundingClientRect()
        const x = event.clientX - rect.left
        const y = event.clientY - rect.top
        const id = Date.now()
        ripples.current = [...ripples.current, { id, x, y }]
        setTicks((t) => t + 1)
        setTimeout(() => {
          ripples.current = ripples.current.filter((r) => r.id !== id)
          setTicks((t) => t + 1)
        }, 600)
      }
      onClick?.(event)
    }

    const Comp = asChild ? Slot : 'button'
    return (
      <Comp
        className={cn(buttonVariants({ variant, size, className }))}
        ref={ref}
        onClick={handleClick}
        {...props}
      >
        {ripples.current.map((r) => (
          <span
            key={r.id}
            data-ripple
            className="pointer-events-none absolute rounded-full bg-white/30 animate-ripple motion-reduce:animate-none"
            style={{ left: r.x - 10, top: r.y - 10, width: 20, height: 20 }}
          />
        ))}
        {props.children}
      </Comp>
    )
  },
)
Button.displayName = 'Button'

export { Button, buttonVariants }
