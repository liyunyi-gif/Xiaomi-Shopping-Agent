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
    <Card className="flex min-h-[70dvh] flex-col overflow-hidden p-4 lg:min-h-[calc(100dvh-8rem)]">
      <header className="border-b border-border pb-3">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="font-heading text-xl font-bold text-foreground">导购对话</h2>
            <p className="text-sm text-muted">咨询、推荐、加购、下单和物流查询统一从这里进入</p>
          </div>
          <div className="flex flex-wrap gap-2 text-xs text-muted">
            {lastReply?.intent && <span>意图 {lastReply.intent}</span>}
            {lastReply?.qualityLevel && <span>质量 {lastReply.qualityLevel}</span>}
            {typeof lastReply?.retryCount === 'number' && <span>重检 {lastReply.retryCount}</span>}
            {typeof lastReply?.childCalls === 'number' && <span>调用 {lastReply.childCalls}</span>}
          </div>
        </div>
      </header>
      <MessageList messages={messages} loading={loading} />
      <div className="space-y-3 border-t border-border pt-3">
        <PromptChips onPick={setDraft} />
        <InputComposer draft={draft} loading={loading} onDraftChange={setDraft} onSend={() => void sendMessage()} />
      </div>
    </Card>
  )
}
