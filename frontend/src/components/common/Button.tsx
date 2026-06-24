import type { ButtonHTMLAttributes, PropsWithChildren } from 'react'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger'
  size?: 'sm' | 'md'
}

const variants = {
  primary: 'bg-brand text-white shadow-sm hover:bg-[#e85f00]',
  secondary: 'bg-ai-primary text-white shadow-sm hover:bg-ai-secondary',
  ghost: 'bg-white text-foreground border border-border hover:bg-brand-soft',
  danger: 'bg-danger text-white hover:bg-red-700',
}

const sizes = {
  sm: 'min-h-11 px-3 py-2 text-sm',
  md: 'min-h-11 px-4 py-2.5 text-base',
}

export function Button({ children, className = '', variant = 'primary', size = 'md', ...props }: PropsWithChildren<ButtonProps>) {
  return (
    <button
      className={`inline-flex items-center justify-center gap-2 rounded-md font-medium transition disabled:opacity-50 ${variants[variant]} ${sizes[size]} ${className}`}
      {...props}
    >
      {children}
    </button>
  )
}
