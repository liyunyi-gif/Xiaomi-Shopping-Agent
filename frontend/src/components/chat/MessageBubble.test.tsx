import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import type { ChatMessage } from '../../stores/chatStore'
import { MessageBubble } from './MessageBubble'

describe('MessageBubble', () => {
  it('renders assistant answer and metadata', () => {
    const message: ChatMessage = {
      id: 'm1',
      role: 'assistant',
      content: '根据当前知识库资料：小米14影像表现优秀。',
      createdAt: 1,
      meta: {
        intent: 'KNOWLEDGE',
        qualityLevel: 'SUFFICIENT',
        retryCount: 0,
        childCalls: 1,
      },
    }

    render(<MessageBubble message={message} />)

    expect(screen.getByText(/小米14影像表现优秀/)).toBeInTheDocument()
    expect(screen.getByText(/意图：KNOWLEDGE/)).toBeInTheDocument()
    expect(screen.getByText(/质量：SUFFICIENT/)).toBeInTheDocument()
  })

  it('renders clarify marker when needClarify is true', () => {
    const message: ChatMessage = {
      id: 'm2',
      role: 'assistant',
      content: '还需要补充商品规格。',
      createdAt: 1,
      meta: { needClarify: true },
    }

    render(<MessageBubble message={message} />)

    expect(screen.getByText('需要补充信息')).toBeInTheDocument()
  })
})
