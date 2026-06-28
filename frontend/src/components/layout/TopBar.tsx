import { Bot, RefreshCw, RotateCcw } from 'lucide-react'
import { useChatStore } from '../../stores/chatStore'
import { useStatusStore } from '../../stores/statusStore'
import { IconButton } from '../common/IconButton'
import { StatusChip } from '../common/StatusChip'

function AgentAvatar() {
  return (
    <div className="relative grid h-14 w-14 shrink-0 place-items-center rounded-2xl border border-ai-primary/50 bg-gradient-to-br from-ai-primary/25 via-ai-secondary/25 to-brand-soft shadow-[0_0_30px_rgba(34,211,238,0.24)]" aria-hidden="true">
      <div className="absolute -inset-1 rounded-[1.25rem] bg-gradient-to-br from-ai-primary/30 via-transparent to-brand/30 blur-md" />
      <div className="relative grid h-10 w-10 place-items-center rounded-full border border-white/20 bg-[#111936]">
        <Bot size={23} className="text-ai-primary" />
        <span className="absolute -right-0.5 top-1 h-2.5 w-2.5 rounded-full bg-accent shadow-[0_0_12px_rgba(244,114,182,0.9)]" />
      </div>
    </div>
  )
}

export function TopBar() {
  const resetConversation = useChatStore(state => state.resetConversation)
  const ready = useStatusStore(state => state.ready)
  const loadingStatus = useStatusStore(state => state.loading)
  const refreshStatus = useStatusStore(state => state.refresh)

  return (
    <header className="sticky top-0 z-20 border-b border-ai-primary/25 bg-[#070a1a]/82 px-4 py-3 shadow-[0_1px_24px_rgba(34,211,238,0.08)] backdrop-blur-xl md:px-6">
      <div className="mx-auto flex w-full max-w-[1760px] items-center justify-between gap-4">
        <div className="flex min-w-0 items-center gap-3">
          <AgentAvatar />
          <div className="min-w-0">
            <h1 className="truncate font-heading text-lg font-bold tracking-wide text-foreground md:text-2xl">小米智能导购 Agent</h1>
            <p className="hidden text-sm text-muted md:block">Mi Cyber Guide · 商品咨询 · 智能推荐 · 加购下单 · 物流查询</p>
          </div>
        </div>
        <div className="flex shrink-0 items-center gap-2">
          <StatusChip value={ready?.status} label="状态" />
          <IconButton label="刷新后端状态" onClick={() => void refreshStatus()} disabled={loadingStatus}>
            <RefreshCw size={18} className={loadingStatus ? 'animate-spin' : ''} aria-hidden="true" />
          </IconButton>
          <IconButton label="重置会话" onClick={resetConversation}>
            <RotateCcw size={18} aria-hidden="true" />
          </IconButton>
        </div>
      </div>
    </header>
  )
}
