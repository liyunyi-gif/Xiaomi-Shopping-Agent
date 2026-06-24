import { ShoppingCart } from 'lucide-react'
import { useChatStore } from '../../stores/chatStore'
import { Card } from '../common/Card'

export function CartStatusCard() {
  const shoppingState = useChatStore(state => state.shoppingState)

  if (!shoppingState) {
    return (
      <Card className="p-4">
        <div className="flex items-center gap-3">
          <div className="rounded-lg bg-brand-soft p-2 text-brand">
            <ShoppingCart size={20} aria-hidden="true" />
          </div>
          <div>
            <h3 className="font-heading font-bold">购物状态</h3>
            <p className="text-sm text-muted">还没有加购商品。试试让导购帮你推荐一款手机。</p>
          </div>
        </div>
      </Card>
    )
  }

  const toneClass = {
    empty: 'border-slate-200 bg-slate-50',
    added: 'border-green-200 bg-green-50',
    orderCreated: 'border-green-200 bg-green-50',
    logistics: 'border-blue-200 bg-blue-50',
    needClarify: 'border-yellow-200 bg-yellow-50',
    failed: 'border-red-200 bg-red-50',
  }[shoppingState.type]

  return (
    <Card className={`p-4 ${toneClass}`}>
      <h3 className="font-heading font-bold text-foreground">{shoppingState.title}</h3>
      <p className="mt-1 text-sm leading-6 text-muted">{shoppingState.description}</p>
      {shoppingState.fields && (
        <dl className="mt-3 grid gap-2 text-sm">
          {Object.entries(shoppingState.fields).map(([key, value]) => (
            <div key={key} className="flex justify-between gap-3 rounded-md bg-white/70 px-2 py-1">
              <dt className="text-muted">{key}</dt>
              <dd className="font-medium text-foreground">{value}</dd>
            </div>
          ))}
        </dl>
      )}
    </Card>
  )
}
