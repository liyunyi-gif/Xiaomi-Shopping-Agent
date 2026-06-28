import { mockProducts } from '../../data/mockProducts'
import { useChatStore } from '../../stores/chatStore'
import { PromptChips } from '../chat/PromptChips'
import { Card } from '../common/Card'
import { ProductCard } from '../product/ProductCard'

export function LeftPanel() {
  const setDraft = useChatStore(state => state.setDraft)

  return (
    <aside className="hidden min-h-0 flex-col gap-4 overflow-y-auto pr-1 lg:flex">
      <Card className="p-4 transition duration-300 hover:border-ai-primary/55 hover:shadow-[0_0_34px_rgba(34,211,238,0.16)]">
        <p className="text-xs font-semibold uppercase tracking-[0.22em] text-ai-primary">Demo Rail</p>
        <h2 className="mt-1 font-heading text-lg font-bold text-foreground">快捷演示</h2>
        <p className="mt-1 text-sm text-muted">一键填入常用导购场景，方便联调展示。</p>
        <div className="mt-4">
          <PromptChips onPick={setDraft} />
        </div>
      </Card>
      <div className="space-y-3 pb-2">
        {mockProducts.map(product => <ProductCard key={product.id} product={product} />)}
      </div>
    </aside>
  )
}
