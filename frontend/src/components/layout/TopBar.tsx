import { Bot, RotateCcw } from 'lucide-react'
import { useChatStore } from '../../stores/chatStore'
import { useStatusStore } from '../../stores/statusStore'
import { IconButton } from '../common/IconButton'
import { StatusChip } from '../common/StatusChip'

export function TopBar() {
  const conversationId = useChatStore(state => state.conversationId)
  const resetConversation = useChatStore(state => state.resetConversation)
  const ready = useStatusStore(state => state.ready)

  return (
    <header className="sticky top-0 z-20 border-b border-border bg-white/90 px-4 py-3 backdrop-blur md:px-6">
      <div className="mx-auto flex max-w-[1440px] items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="rounded-xl bg-gradient-to-br from-brand to-ai-primary p-2 text-white shadow-sm">
            <Bot size={24} aria-hidden="true" />
          </div>
          <div>
            <h1 className="font-heading text-lg font-bold text-foreground md:text-2xl">小米智能导购 Agent</h1>
            <p className="hidden text-sm text-muted md:block">商品咨询 · 智能推荐 · 加购下单 · 物流查询</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <span className="hidden rounded-full bg-slate-100 px-3 py-1 text-xs text-muted lg:inline-flex">{conversationId}</span>
          <StatusChip value={ready?.status} label="状态" />
          <IconButton label="重置会话" onClick={resetConversation}>
            <RotateCcw size={18} aria-hidden="true" />
          </IconButton>
        </div>
      </div>
    </header>
  )
}
