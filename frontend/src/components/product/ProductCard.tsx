import type { MockProduct } from '../../data/mockProducts'
import { useChatStore } from '../../stores/chatStore'
import { Button } from '../common/Button'
import { Card } from '../common/Card'

interface ProductCardProps {
  product: MockProduct
}

export function ProductCard({ product }: ProductCardProps) {
  const setDraft = useChatStore(state => state.setDraft)

  return (
    <Card className="group p-4 transition duration-300 hover:-translate-y-0.5 hover:border-ai-primary/65 hover:shadow-[0_0_34px_rgba(34,211,238,0.16),0_0_22px_rgba(255,105,0,0.08)] focus-within:border-ai-primary/70">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h3 className="font-heading text-lg font-bold text-foreground">{product.name}</h3>
          <p className="mt-1 text-sm text-muted">{product.spec}</p>
        </div>
        <span className="rounded-full border border-brand/35 bg-brand-soft px-2.5 py-1 text-xs font-semibold text-brand">推荐</span>
      </div>
      <div className="mt-3 flex flex-wrap gap-2">
        {product.tags.map(tag => (
          <span key={tag} className="rounded-full border border-ai-secondary/30 bg-ai-secondary/12 px-2 py-1 text-xs font-medium text-ai-primary">
            {tag}
          </span>
        ))}
      </div>
      <p className="mt-3 text-sm leading-6 text-muted">{product.reason}</p>
      <div className="mt-4 grid grid-cols-2 gap-2">
        <Button variant="ghost" size="sm" onClick={() => setDraft(`查一下 ${product.skuId} 有没有库存`)}>
          查库存
        </Button>
        <Button size="sm" onClick={() => setDraft(`帮我加购一台${product.name} ${product.spec}`)}>
          加购
        </Button>
      </div>
    </Card>
  )
}
