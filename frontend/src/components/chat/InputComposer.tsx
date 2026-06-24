import { SendHorizonal } from 'lucide-react'
import type { KeyboardEvent } from 'react'
import { Button } from '../common/Button'

interface InputComposerProps {
  draft: string
  loading: boolean
  onDraftChange: (value: string) => void
  onSend: () => void
}

export function InputComposer({ draft, loading, onDraftChange, onSend }: InputComposerProps) {
  const canSend = draft.trim().length > 0 && !loading

  function handleKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      if (canSend) onSend()
    }
  }

  return (
    <div className="rounded-xl border border-border bg-white p-3 shadow-sm">
      <label className="sr-only" htmlFor="chat-input">输入导购问题</label>
      <textarea
        id="chat-input"
        value={draft}
        onChange={event => onDraftChange(event.target.value)}
        onKeyDown={handleKeyDown}
        placeholder={loading ? '正在等待 Agent 回复...' : '问我商品参数、推荐手机，或让我帮你加购...'}
        rows={3}
        className="min-h-20 w-full resize-none rounded-lg border border-transparent bg-slate-50 px-3 py-2 text-base leading-6 outline-none transition focus:border-ai-primary focus:bg-white"
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
