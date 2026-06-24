import { statusLabel, statusTone } from '../../utils/statusMapping'

const tones = {
  success: 'border-green-200 bg-green-50 text-green-700',
  warning: 'border-yellow-200 bg-yellow-50 text-yellow-700',
  danger: 'border-red-200 bg-red-50 text-red-700',
  neutral: 'border-slate-200 bg-slate-50 text-slate-600',
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
