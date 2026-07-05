import { quickPrompts } from '../../data/quickPrompts'

interface PromptChipsProps {
  onPick: (prompt: string) => void
}

export function PromptChips({ onPick }: PromptChipsProps) {
  return (
    <div className="flex gap-2 overflow-x-auto pb-1" aria-label="快捷操作">
      {quickPrompts.map(item => (
        <button
          key={item.id}
          type="button"
          onClick={() => onPick(item.prompt)}
          className="min-h-11 shrink-0 rounded-full border border-border bg-surface-strong px-3 text-sm font-medium text-foreground transition duration-200 hover:-translate-y-0.5 hover:border-ai-primary/70 hover:bg-ai-primary/10 hover:shadow-[0_0_18px_rgba(34,211,238,0.16)] focus-visible:outline-ai-primary"
        >
          {item.label}
        </button>
      ))}
    </div>
  )
}
