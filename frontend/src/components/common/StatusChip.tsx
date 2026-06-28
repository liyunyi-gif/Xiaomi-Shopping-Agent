import { statusLabel, statusTone } from '../../utils/statusMapping'

const tones = {
  success: 'border-success/35 bg-success/12 text-success',
  warning: 'border-warning/40 bg-warning/14 text-warning',
  danger: 'border-danger/40 bg-danger/14 text-danger',
  neutral: 'border-border bg-surface-strong text-muted',
}

interface StatusChipProps {
  value?: string
  label?: string
  className?: string
}

export function StatusChip({ value, label, className = '' }: StatusChipProps) {
  const tone = statusTone(value)
  return (
    <span className={`inline-flex min-h-7 items-center rounded-full border px-2.5 text-xs font-semibold ${tones[tone]} ${className}`}>
      {label ? `${label}: ` : ''}{statusLabel(value)}
    </span>
  )
}
