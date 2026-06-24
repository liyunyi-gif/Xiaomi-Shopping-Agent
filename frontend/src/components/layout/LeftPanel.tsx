import { mockProducts } from '../../data/mockProducts'
import { useChatStore } from '../../stores/chatStore'
import { PromptChips } from '../chat/PromptChips'
import { Card } from '../common/Card'
import { ProductCard } from '../product/ProductCard'

export function LeftPanel() {
  const setDraft = useChatStore(state => state.setDraft)

  return (
    <aside className="hidden min-h-0 flex-col gap-4 lg:flex">
      <Card className="p-4">
        <h2 className="font-heading text-lg font-bold">快捷演示</h2>
        <p className="mt-1 text-sm text-muted">一键填入常用导购场景，方便联调展示。</p>
        <div className="mt-4">
          <PromptChips onPick={setDraft} />
        </div>
      </Card>
      <div className="space-y-3 overflow-y-auto pb-2">
        {mockProducts.map(product => <ProductCard key={product.id} product={product} />)}
      </div>
    </aside>
  )
}
