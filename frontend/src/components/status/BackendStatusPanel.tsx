import { RefreshCw } from 'lucide-react'
import { useEffect } from 'react'
import { useStatusStore } from '../../stores/statusStore'
import { readySummary } from '../../utils/statusMapping'
import { Button } from '../common/Button'
import { Card } from '../common/Card'
import { StatusChip } from '../common/StatusChip'

const groups = [
  ['核心模块', ['bootstrap', 'orchestrator', 'knowledgeGateway', 'shoppingGateway']],
  ['基础设施', ['postgres', 'redis', 'mcpserver']],
  ['模型能力', ['chatModel', 'embeddingModel', 'rerank']],
] as const

export function BackendStatusPanel() {
  const ready = useStatusStore(state => state.ready)
  const loading = useStatusStore(state => state.loading)
  const error = useStatusStore(state => state.error)
  const lastRefreshedAt = useStatusStore(state => state.lastRefreshedAt)
  const refresh = useStatusStore(state => state.refresh)

  useEffect(() => {
    void refresh()
  }, [refresh])

  return (
    <Card className="p-4">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h3 className="font-heading font-bold">后端状态</h3>
          <p className="text-sm text-muted">{error ? error.message : readySummary(ready)}</p>
        </div>
        <Button variant="ghost" size="sm" onClick={() => void refresh()} disabled={loading} aria-label="刷新后端状态">
          <RefreshCw size={16} className={loading ? 'animate-spin' : ''} aria-hidden="true" />
          刷新
        </Button>
      </div>

      <div className="mt-3 flex flex-wrap gap-2">
        <StatusChip value={ready?.status} label="聚合" />
        {lastRefreshedAt && <span className="text-xs text-muted">刷新于 {new Date(lastRefreshedAt).toLocaleTimeString()}</span>}
      </div>

      <div className="mt-4 space-y-4">
        {groups.map(([title, keys]) => (
          <div key={title}>
            <h4 className="mb-2 text-xs font-semibold text-muted">{title}</h4>
            <div className="flex flex-wrap gap-2">
              {keys.map(key => <StatusChip key={key} value={ready?.[key]} label={key} />)}
            </div>
          </div>
        ))}
      </div>
    </Card>
  )
}
