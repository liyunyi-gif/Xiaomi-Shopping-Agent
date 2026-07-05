import { SendHorizonal } from 'lucide-react'
import type { KeyboardEvent } from 'react'
import { Button } from '../common/Button'

interface InputComposerProps {
  draft: string
  loading: boolean
  needClarify?: boolean
  onDraftChange: (value: string) => void
  onSend: () => void
}

export function InputComposer({ draft, loading, needClarify = false, onDraftChange, onSend }: InputComposerProps) {
  const canSend = draft.trim().length > 0 && !loading
  const placeholder = loading
    ? '正在等待 Agent 回复...'
    : needClarify
      ? '请补充上面 Agent 追问的信息...'
      : '问我商品参数、推荐手机，或让我帮你加购...'

  function handleKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      if (canSend) onSend()
    }
  }

  return (
    <div className="rounded-xl border border-border bg-surface-strong p-3 shadow-[inset_0_1px_0_rgba(255,255,255,0.04)]">
      <label className="sr-only" htmlFor="chat-input">输入导购问题</label>
      <textarea
        id="chat-input"
        value={draft}
        onChange={event => onDraftChange(event.target.value)}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        rows={3}
        className="min-h-20 w-full resize-none rounded-lg border border-transparent bg-[#070a1a]/72 px-3 py-2 text-base leading-6 text-foreground outline-none transition placeholder:text-muted focus:border-ai-primary focus:bg-[#0b1028]"
      />
      <div className="mt-3 flex items-center justify-between gap-3">
        <p className="text-xs text-muted">Enter 发送，Shift + Enter 换行</p>
        <Button onClick={onSend} disabled={!canSend} aria-label="发送消息">
          <SendHorizonal size={18} aria-hidden="true" />
          {loading ? '发送中' : '发送'}
        </Button>
      </div>
    </div>
  )
}
