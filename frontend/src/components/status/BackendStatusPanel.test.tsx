import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useStatusStore } from '../../stores/statusStore'
import { useUiStore } from '../../stores/uiStore'
import { BackendStatusPanel } from './BackendStatusPanel'

const refresh = vi.fn()

beforeEach(() => {
  refresh.mockReset()
  useUiStore.setState({ rightPanelExpanded: false })
  useStatusStore.setState({
    health: { status: 'UP', project: 'xiaomi-agent', arch: 'three-node' },
    ready: {
      bootstrap: 'UP',
      orchestrator: 'UP',
      knowledgeGateway: 'FALLBACK',
      shoppingGateway: 'UP',
      postgres: 'UP',
      redis: 'UP',
      mcpserver: 'DOWN',
      chatModel: 'CONFIGURED',
      embeddingModel: 'MISSING_KEY',
      rerank: 'FALLBACK',
      status: 'DEGRADED',
    },
    loading: false,
    error: undefined,
    lastRefreshedAt: undefined,
    refresh,
  })
})

describe('BackendStatusPanel', () => {
  it('renders health and aggregate ready status without relying on colors only', () => {
    render(<BackendStatusPanel />)

    expect(screen.getByText('健康: 正常')).toBeInTheDocument()
    expect(screen.getByText('聚合: 降级')).toBeInTheDocument()
    expect(screen.getByText('部分能力降级运行')).toBeInTheDocument()
  })

  it('expands detailed backend groups on demand', async () => {
    render(<BackendStatusPanel />)

    expect(screen.queryByText('核心模块')).not.toBeInTheDocument()
    const toggle = screen.getByRole('button', { name: /查看服务状态/ })
    expect(toggle).toHaveAttribute('aria-expanded', 'false')
    await userEvent.click(toggle)

    expect(toggle).toHaveAttribute('aria-expanded', 'true')

    expect(screen.getByText('核心模块')).toBeInTheDocument()
    expect(screen.getByText('mcpserver: 异常')).toBeInTheDocument()
    expect(screen.getByText('embeddingModel: 缺配置')).toBeInTheDocument()
  })

  it('refreshes status manually', async () => {
    render(<BackendStatusPanel />)

    await userEvent.click(screen.getByRole('button', { name: /刷新后端状态/ }))

    expect(refresh).toHaveBeenCalledTimes(1)
  })
})
