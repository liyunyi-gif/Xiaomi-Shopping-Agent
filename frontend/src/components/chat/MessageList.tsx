import { useEffect, useRef } from 'react'
import type { ChatMessage as ChatMessageType } from '../../stores/chatStore'
import { LoadingBubble } from './LoadingBubble'
import { MessageBubble } from './MessageBubble'

interface MessageListProps {
  messages: ChatMessageType[]
  loading: boolean
}

export function MessageList({ messages, loading }: MessageListProps) {
  const endRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' })
  }, [messages.length, loading])

  return (
    <div className="flex min-h-0 flex-1 flex-col gap-4 overflow-y-auto px-2 py-4" aria-live="polite">
      {messages.map(message => (
        <MessageBubble key={message.id} message={message} />
      ))}
      {loading && <LoadingBubble />}
      <div ref={endRef} />
    </div>
  )
}
