import type { HTMLAttributes, PropsWithChildren } from 'react'

export function Card({ children, className = '', ...props }: PropsWithChildren<HTMLAttributes<HTMLDivElement>>) {
  return (
    <section className={`rounded-xl border border-border bg-surface shadow-sm ${className}`} {...props}>
      {children}
    </section>
  )
}
