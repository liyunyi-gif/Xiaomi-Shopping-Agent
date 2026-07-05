import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useChatStore } from '../../stores/chatStore'
import { useStatusStore } from '../../stores/statusStore'
import { TopBar } from './TopBar'

const refresh = vi.fn()

beforeEach(() => {
  refresh.mockReset()
  useChatStore.setState({ conversationId: 'conv-abc123' })
  useStatusStore.setState({ ready: { status: 'UP' } as never, loading: false, refresh })
})

describe('TopBar', () => {
  it('does not render the conversation id by default', () => {
    render(<TopBar />)

    expect(screen.getByText('小米智能导购 Agent')).toBeInTheDocument()
    expect(screen.queryByText(/conv-abc123/)).not.toBeInTheDocument()
  })

  it('exposes status refresh action', () => {
    render(<TopBar />)

    expect(screen.getByRole('button', { name: /刷新后端状态/ })).toBeInTheDocument()
  })
})
