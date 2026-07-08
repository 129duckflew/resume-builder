import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ConfirmDialogAction } from '../confirm-dialog'

describe('ConfirmDialogAction', () => {
  const defaultProps = {
    open: true,
    onOpenChange: vi.fn(),
    title: 'Delete Item',
    description: 'Are you sure?',
    onConfirm: vi.fn(),
  }

  it('renders with title and description', () => {
    render(<ConfirmDialogAction {...defaultProps} />)
    expect(screen.getByText('Delete Item')).toBeTruthy()
    expect(screen.getByText('Are you sure?')).toBeTruthy()
  })

  it('renders custom button labels', () => {
    render(
      <ConfirmDialogAction
        {...defaultProps}
        confirmLabel="Remove"
        cancelLabel="Keep"
      />,
    )
    expect(screen.getByText('Remove')).toBeTruthy()
    expect(screen.getByText('Keep')).toBeTruthy()
  })

  it('calls onConfirm when confirm button clicked', async () => {
    const onConfirm = vi.fn()
    render(<ConfirmDialogAction {...defaultProps} onConfirm={onConfirm} />)
    await userEvent.click(screen.getByText('Delete'))
    expect(onConfirm).toHaveBeenCalledTimes(1)
  })

  it('calls onOpenChange(false) when cancel clicked', async () => {
    const onOpenChange = vi.fn()
    render(<ConfirmDialogAction {...defaultProps} onOpenChange={onOpenChange} />)
    await userEvent.click(screen.getByText('Cancel'))
    expect(onOpenChange).toHaveBeenCalledWith(false)
  })

  it('shows loading state with spinner', () => {
    render(<ConfirmDialogAction {...defaultProps} loading={true} />)
    expect(screen.getByText('Delete...')).toBeTruthy()
    expect(screen.getByText('Cancel').closest('button')).toBeDisabled()
    expect(screen.getByText('Delete...').closest('button')).toBeDisabled()
  })

  it('shows custom loading label', () => {
    render(
      <ConfirmDialogAction
        {...defaultProps}
        loading={true}
        loadingLabel="Removing..."
      />,
    )
    expect(screen.getByText('Removing...')).toBeTruthy()
  })

  it('renders warning variant with amber styling', () => {
    render(<ConfirmDialogAction {...defaultProps} variant="warning" />)
    expect(screen.getByText('Delete')).toBeTruthy()
  })

  it('does not render when closed', () => {
    render(<ConfirmDialogAction {...defaultProps} open={false} />)
    expect(screen.queryByText('Delete Item')).toBeNull()
  })
})
