import type { ReadyResponse } from '../api/types'

export type Tone = 'success' | 'warning' | 'danger' | 'neutral'

export function statusTone(value?: string): Tone {
  if (!value) return 'neutral'
  if (value === 'UP' || value === 'CONFIGURED' || value === 'SUCCESS') return 'success'
  if (value === 'DEGRADED' || value === 'FALLBACK' || value === 'NEED_CLARIFY') return 'warning'
  if (value === 'DOWN' || value === 'MISSING_KEY' || value === 'FAILED') return 'danger'
  return 'neutral'
}

export function statusLabel(value?: string): string {
  const labels: Record<string, string> = {
    UP: '正常',
    CONFIGURED: '已配置',
    SUCCESS: '成功',
    DEGRADED: '降级',
    FALLBACK: '降级兜底',
    NEED_CLARIFY: '需补充',
    DOWN: '异常',
    MISSING_KEY: '缺配置',
    FAILED: '失败',
  }
  return value ? labels[value] || value : '未知'
}

export function readySummary(ready?: ReadyResponse): string {
  if (!ready) return '未检查'
  if (ready.status === 'UP') return '全部服务正常'
  if (ready.status === 'DEGRADED') return '部分能力降级运行'
  if (ready.status === 'DOWN') return '核心服务异常'
  return ready.status
}
