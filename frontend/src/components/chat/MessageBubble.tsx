import type { ChatMessage } from '../../stores/chatStore'

interface MessageBubbleProps {
  message: ChatMessage
}

const roleClasses = {
  user: 'ml-auto bg-brand text-white',
  assistant: 'mr-auto border border-border bg-white text-foreground',
  system: 'mx-auto border border-ai-primary/20 bg-purple-50 text-ai-primary',
  error: 'mr-auto border border-red-200 bg-red-50 text-red-700',
}

export function MessageBubble({ message }: MessageBubbleProps) {
  const isAssistant = message.role === 'assistant'
  return (
    <article className={`max-w-[82%] rounded-lg px-4 py-3 shadow-sm ${roleClasses[message.role]}`}>
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
