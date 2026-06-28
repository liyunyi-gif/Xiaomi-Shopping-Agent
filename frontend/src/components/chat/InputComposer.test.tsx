import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { InputComposer } from './InputComposer'

describe('InputComposer', () => {
  it('does not send empty input', async () => {
    const onSend = vi.fn()
    render(<InputComposer draft="   " loading={false} onDraftChange={() => undefined} onSend={onSend} />)

    await userEvent.click(screen.getByRole('button', { name: /发送消息/ }))

    expect(onSend).not.toHaveBeenCalled()
  })

  it('sends with Enter and supports Shift Enter newline', async () => {
    const onSend = vi.fn()
    const onDraftChange = vi.fn()
    render(<InputComposer draft="小米14" loading={false} onDraftChange={onDraftChange} onSend={onSend} />)

    const input = screen.getByLabelText('输入导购问题')
    await userEvent.type(input, '{Shift>}{Enter}{/Shift}')
    expect(onSend).not.toHaveBeenCalled()

    await userEvent.keyboard('{Enter}')
    expect(onSend).toHaveBeenCalledTimes(1)
  })

  it('disables send button while loading', () => {
    render(<InputComposer draft="小米14" loading={true} onDraftChange={() => undefined} onSend={() => undefined} />)

    expect(screen.getByRole('button', { name: /发送消息/ })).toBeDisabled()
  })

  it('shows clarification placeholder when Agent needs more information', () => {
    render(<InputComposer draft="" loading={false} needClarify onDraftChange={() => undefined} onSend={() => undefined} />)

    expect(screen.getByPlaceholderText(/请补充上面 Agent 追问的信息/)).toBeInTheDocument()
  })
})
