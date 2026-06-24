import { describe, expect, it } from 'vitest'
import { statusLabel, statusTone } from './statusMapping'

describe('statusMapping', () => {
  it('maps normal statuses', () => {
    expect(statusTone('UP')).toBe('success')
    expect(statusLabel('UP')).toBe('正常')
  })

  it('maps degraded statuses', () => {
    expect(statusTone('DEGRADED')).toBe('warning')
    expect(statusLabel('FALLBACK')).toBe('降级兜底')
  })

  it('maps down statuses', () => {
    expect(statusTone('DOWN')).toBe('danger')
    expect(statusLabel('MISSING_KEY')).toBe('缺配置')
  })
})
