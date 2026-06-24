import type { ButtonHTMLAttributes, PropsWithChildren } from 'react'

interface IconButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  label: string
}

export function IconButton({ label, children, className = '', ...props }: PropsWithChildren<IconButtonProps>) {
  return (
    <button
      aria-label={label}
      title={label}
      className={`inline-flex min-h-11 min-w-11 items-center justify-center rounded-md border border-border bg-white text-foreground transition hover:bg-brand-soft disabled:opacity-50 ${className}`}
      {...props}
    >
      {children}
    </button>
  )
}
