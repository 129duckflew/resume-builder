import * as React from 'react'
import * as DialogPrimitive from '@radix-ui/react-dialog'
import { AlertTriangle } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Button } from './button'
import { DialogOverlay, DialogPortal } from './dialog'

const ConfirmDialog = DialogPrimitive.Root
const ConfirmDialogTrigger = DialogPrimitive.Trigger

interface ConfirmDialogContentProps extends React.ComponentPropsWithoutRef<typeof DialogPrimitive.Content> {
  variant?: 'destructive' | 'warning'
}

const ConfirmDialogContent = React.forwardRef<
  React.ElementRef<typeof DialogPrimitive.Content>,
  ConfirmDialogContentProps
>(({ className, children, variant = 'destructive', title: _title, ...props }, ref) => (
  <DialogPortal>
    <DialogOverlay className="bg-black/20 backdrop-blur-sm" />
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <DialogPrimitive.Content
        ref={ref}
        className={cn(
          'relative z-50 w-full max-w-md outline-none',
          'data-[state=open]:animate-in data-[state=closed]:animate-out',
          'data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0',
          'data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95',
          'data-[state=closed]:slide-out-to-top-[48%] data-[state=open]:slide-in-from-top-[48%]',
          className,
        )}
        {...props}
      >
        <div
          className={cn(
            'relative rounded-2xl bg-white p-[1px] shadow-2xl',
            variant === 'destructive'
              ? 'bg-gradient-to-br from-red-400 via-orange-300 to-rose-400 bg-300% animate-gradient-shift'
              : 'bg-gradient-to-br from-amber-400 via-yellow-300 to-orange-400 bg-300% animate-gradient-shift',
          )}
        >
          <div className="rounded-[15px] bg-white p-6">
            <div className="flex flex-col items-center text-center">
              <div
                className={cn(
                  'flex items-center justify-center w-14 h-14 rounded-full mb-4 animate-icon-pulse',
                  variant === 'destructive'
                    ? 'bg-gradient-to-br from-red-50 to-red-100'
                    : 'bg-gradient-to-br from-amber-50 to-amber-100',
                )}
              >
                <AlertTriangle
                  className={cn(
                    'h-7 w-7',
                    variant === 'destructive' ? 'text-red-500' : 'text-amber-500',
                  )}
                />
              </div>
              <DialogPrimitive.Title className="text-lg font-semibold mb-2">
                {_title || 'Confirm'}
              </DialogPrimitive.Title>
              <DialogPrimitive.Description className="text-sm text-muted-foreground mb-6 leading-relaxed">
                {children}
              </DialogPrimitive.Description>
            </div>
          </div>
        </div>
      </DialogPrimitive.Content>
    </div>
  </DialogPortal>
))
ConfirmDialogContent.displayName = 'ConfirmDialogContent'

interface ConfirmDialogActionProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  title: string
  description: string
  confirmLabel?: string
  cancelLabel?: string
  loadingLabel?: string
  variant?: 'destructive' | 'warning'
  onConfirm: () => void | Promise<void>
  loading?: boolean
  onCloseAutoFocus?: (e: Event) => void
}

function ConfirmDialogAction({
  open,
  onOpenChange,
  title,
  description,
  confirmLabel = 'Delete',
  cancelLabel = 'Cancel',
  loadingLabel,
  variant = 'destructive',
  onConfirm,
  loading = false,
  onCloseAutoFocus,
}: ConfirmDialogActionProps) {
  return (
    <ConfirmDialog open={open} onOpenChange={onOpenChange}>
      <ConfirmDialogContent variant={variant} title={title} onCloseAutoFocus={onCloseAutoFocus}>
        {description}
        <div className="flex items-center justify-center gap-3 mt-6">
          <DialogPrimitive.Close asChild>
            <Button variant="outline" disabled={loading}>
              {cancelLabel}
            </Button>
          </DialogPrimitive.Close>
          <Button
            variant={variant === 'destructive' ? 'destructive' : 'default'}
            onClick={onConfirm}
            disabled={loading}
            className={cn(
              'relative overflow-hidden min-w-[80px]',
              variant === 'destructive' && 'shadow-[0_0_20px_rgba(239,68,68,0.3)] hover:shadow-[0_0_30px_rgba(239,68,68,0.5)] transition-shadow',
            )}
          >
            {loading ? (
              <span className="flex items-center gap-2">
                <span className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                {loadingLabel || `${confirmLabel}...`}
              </span>
            ) : (
              confirmLabel
            )}
          </Button>
        </div>
      </ConfirmDialogContent>
    </ConfirmDialog>
  )
}

export {
  ConfirmDialog,
  ConfirmDialogTrigger,
  ConfirmDialogContent,
  ConfirmDialogAction,
}
