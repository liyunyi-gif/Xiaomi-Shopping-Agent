import { useChatStore } from '../../stores/chatStore'
import { Card } from '../common/Card'
import { InputComposer } from './InputComposer'
import { MessageList } from './MessageList'
import { PromptChips } from './PromptChips'

export function ChatPanel() {
  const messages = useChatStore(state => state.messages)
  const draft = useChatStore(state => state.draft)
  const loading = useChatStore(state => state.loading)
  const lastReply = useChatStore(state => state.lastReply)
  const setDraft = useChatStore(state => state.setDraft)
  const sendMessage = useChatStore(state => state.sendMessage)

  return (
    <Card className="relative flex min-h-[72dvh] flex-col overflow-hidden border-ai-primary/35 p-4 shadow-[0_0_46px_rgba(34,211,238,0.16),inset_0_1px_0_rgba(255,255,255,0.06)] lg:h-full lg:min-h-0">
      <div className="pointer-events-none absolute inset-x-6 top-0 h-px bg-gradient-to-r from-transparent via-ai-primary to-transparent" />
      <header className="shrink-0 border-b border-border pb-3">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-ai-primary">Chat Cockpit</p>
            <h2 className="font-heading text-xl font-bold text-foreground">导购对话</h2>
            <p className="text-sm text-muted">咨询、推荐、加购、下单和物流查询统一从这里进入</p>
          </div>
          <div className="flex flex-wrap gap-2 text-xs text-muted">
            {lastReply?.intent && <span className="rounded-full border border-border bg-surface-strong px-2 py-1">意图 {lastReply.intent}</span>}
            {lastReply?.qualityLevel && <span className="rounded-full border border-border bg-surface-strong px-2 py-1">质量 {lastReply.qualityLevel}</span>}
            {typeof lastReply?.retryCount === 'number' && <span className="rounded-full border border-border bg-surface-strong px-2 py-1">重检 {lastReply.retryCount}</span>}
            {typeof lastReply?.childCalls === 'number' && <span className="rounded-full border border-border bg-surface-strong px-2 py-1">调用 {lastReply.childCalls}</span>}
          </div>
        </div>
      </header>
      <MessageList messages={messages} loading={loading} />
      <div className="shrink-0 space-y-3 border-t border-border pt-3">
        <PromptChips onPick={setDraft} />
        <InputComposer
          draft={draft}
          loading={loading}
          needClarify={lastReply?.needClarify}
          onDraftChange={setDraft}
          onSend={() => void sendMessage()}
        />
      </div>
    </Card>
  )
}
