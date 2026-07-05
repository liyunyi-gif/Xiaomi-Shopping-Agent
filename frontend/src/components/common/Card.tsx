import type { HTMLAttributes, PropsWithChildren } from 'react'

export function Card({ children, className = '', ...props }: PropsWithChildren<HTMLAttributes<HTMLDivElement>>) {
  return (
    <section
      className={`rounded-xl border border-border bg-surface shadow-[0_18px_60px_rgba(0,0,0,0.24),0_0_26px_rgba(34,211,238,0.08)] backdrop-blur-xl ${className}`}
      {...props}
    >
      {children}
    </section>
  )
}
