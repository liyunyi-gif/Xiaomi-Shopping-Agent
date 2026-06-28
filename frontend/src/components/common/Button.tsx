import type { ButtonHTMLAttributes, PropsWithChildren } from 'react'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger'
  size?: 'sm' | 'md'
}

const variants = {
  primary: 'border border-brand/70 bg-gradient-to-r from-brand to-[#ff8a1f] text-white shadow-[0_0_22px_rgba(255,105,0,0.22)] hover:shadow-[0_0_30px_rgba(255,105,0,0.34)]',
  secondary: 'border border-ai-primary/60 bg-ai-primary/12 text-ai-primary shadow-[0_0_18px_rgba(34,211,238,0.18)] hover:bg-ai-primary/18',
  ghost: 'border border-border bg-surface text-foreground hover:border-ai-primary/70 hover:bg-ai-primary/10',
  danger: 'border border-danger/70 bg-danger/18 text-danger hover:bg-danger/25',
}

const sizes = {
  sm: 'min-h-11 px-3 py-2 text-sm',
  md: 'min-h-11 px-4 py-2.5 text-base',
}

export function Button({ children, className = '', variant = 'primary', size = 'md', ...props }: PropsWithChildren<ButtonProps>) {
  return (
    <button
      className={`inline-flex items-center justify-center gap-2 rounded-md font-medium transition duration-200 disabled:opacity-50 ${variants[variant]} ${sizes[size]} ${className}`}
      {...props}
    >
      {children}
    </button>
  )
}
