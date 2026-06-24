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
          className="min-h-11 shrink-0 rounded-full border border-border bg-white px-3 text-sm font-medium text-foreground transition hover:border-brand hover:bg-brand-soft focus-visible:outline-ai-primary"
        >
          {item.label}
        </button>
      ))}
    </div>
  )
}
