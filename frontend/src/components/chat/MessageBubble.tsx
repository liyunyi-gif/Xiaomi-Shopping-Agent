import type { ChatMessage } from '../../stores/chatStore'

interface MessageBubbleProps {
  message: ChatMessage
}

const roleClasses = {
  user: 'ml-auto bg-gradient-to-br from-brand to-[#ff8a1f] text-white shadow-[0_0_22px_rgba(255,105,0,0.2)]',
  assistant: 'mr-auto border border-ai-primary/28 bg-surface-strong text-foreground',
  clarify: 'mr-auto border border-warning/45 bg-warning/12 text-foreground',
  system: 'mx-auto border border-ai-secondary/35 bg-ai-secondary/12 text-ai-primary',
  error: 'mr-auto border border-danger/45 bg-danger/12 text-danger',
}

export function MessageBubble({ message }: MessageBubbleProps) {
  const isAssistant = message.role === 'assistant'
  const bubbleClass = message.meta?.needClarify ? roleClasses.clarify : roleClasses[message.role]
  return (
    <article className={`max-w-[82%] rounded-lg px-4 py-3 shadow-sm ${bubbleClass}`}>
      {message.meta?.needClarify && <div className="mb-2 h-1 rounded-full bg-warning" aria-hidden="true" />}
      <p className="whitespace-pre-wrap text-sm leading-6 md:text-base">{message.content}</p>
      {isAssistant && message.meta && (
        <div className="mt-3 flex flex-wrap gap-2 text-xs text-muted">
          {message.meta.intent && <span>意图：{message.meta.intent}</span>}
          {message.meta.qualityLevel && <span>质量：{message.meta.qualityLevel}</span>}
          {typeof message.meta.retryCount === 'number' && <span>重检：{message.meta.retryCount}</span>}
          {typeof message.meta.childCalls === 'number' && <span>调用：{message.meta.childCalls}</span>}
          {message.meta.needClarify && <span className="font-semibold text-warning">需要补充信息</span>}
        </div>
      )}
    </article>
  )
}
