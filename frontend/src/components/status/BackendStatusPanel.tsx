import { ChevronDown, RefreshCw } from 'lucide-react'
import { useUiStore } from '../../stores/uiStore'
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
  const health = useStatusStore(state => state.health)
  const ready = useStatusStore(state => state.ready)
  const loading = useStatusStore(state => state.loading)
  const error = useStatusStore(state => state.error)
  const lastRefreshedAt = useStatusStore(state => state.lastRefreshedAt)
  const refresh = useStatusStore(state => state.refresh)
  const expanded = useUiStore(state => state.rightPanelExpanded)
  const toggleExpanded = useUiStore(state => state.toggleRightPanel)

  return (
    <Card className="p-4">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-ai-primary">Backend Link</p>
          <h3 className="font-heading font-bold text-foreground">后端状态</h3>
          <p className="text-sm text-muted">{error ? error.message : readySummary(ready)}</p>
        </div>
        <div className="flex gap-2">
          <Button variant="ghost" size="sm" onClick={toggleExpanded} aria-expanded={expanded} aria-controls="backend-status-details">
            <ChevronDown size={16} className={`transition ${expanded ? 'rotate-180' : ''}`} aria-hidden="true" />
            {expanded ? '隐藏服务状态' : '查看服务状态'}
          </Button>
          <Button variant="ghost" size="sm" onClick={() => void refresh()} disabled={loading} aria-label="刷新后端状态">
            <RefreshCw size={16} className={loading ? 'animate-spin' : ''} aria-hidden="true" />
            刷新
          </Button>
        </div>
      </div>

      <div className="mt-3 flex flex-wrap gap-2">
        <StatusChip value={health?.status} label="健康" />
        <StatusChip value={ready?.status} label="聚合" />
        {lastRefreshedAt && <span className="text-xs text-muted">刷新于 {new Date(lastRefreshedAt).toLocaleTimeString()}</span>}
      </div>

      {expanded && (
        <div id="backend-status-details" className="mt-4 max-h-72 space-y-4 overflow-y-auto pr-1">
          {groups.map(([title, keys]) => (
            <div key={title}>
              <h4 className="mb-2 text-xs font-semibold text-muted">{title}</h4>
              <div className="flex flex-wrap gap-2">
                {keys.map(key => <StatusChip key={key} value={ready?.[key]} label={key} />)}
              </div>
            </div>
          ))}
        </div>
      )}
    </Card>
  )
}
