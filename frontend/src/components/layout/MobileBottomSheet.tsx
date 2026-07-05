import { Activity, PackageSearch, ShoppingCart, X } from 'lucide-react'
import { mockProducts } from '../../data/mockProducts'
import { useChatStore } from '../../stores/chatStore'
import { useUiStore } from '../../stores/uiStore'
import { CartStatusCard } from '../cart/CartStatusCard'
import { PromptChips } from '../chat/PromptChips'
import { IconButton } from '../common/IconButton'
import { ProductCard } from '../product/ProductCard'
import { BackendStatusPanel } from '../status/BackendStatusPanel'

export function MobileBottomSheet() {
  const panel = useUiStore(state => state.mobilePanel)
  const setPanel = useUiStore(state => state.setMobilePanel)
  const setDraft = useChatStore(state => state.setDraft)

  function pickPrompt(prompt: string) {
    setDraft(prompt)
    setPanel(null)
  }

  return (
    <>
      <nav className="fixed inset-x-0 bottom-0 z-30 grid grid-cols-3 border-t border-ai-primary/25 bg-[#070a1a]/88 p-2 backdrop-blur-xl lg:hidden" aria-label="移动端快捷面板">
        <button className="min-h-11 rounded-md text-sm text-foreground transition hover:bg-ai-primary/10" onClick={() => setPanel('prompts')} aria-expanded={panel === 'prompts'}>
          <PackageSearch className="mx-auto" size={18} aria-hidden="true" />推荐
        </button>
        <button className="min-h-11 rounded-md text-sm text-foreground transition hover:bg-ai-primary/10" onClick={() => setPanel('cart')} aria-expanded={panel === 'cart'}>
          <ShoppingCart className="mx-auto" size={18} aria-hidden="true" />购物
        </button>
        <button className="min-h-11 rounded-md text-sm text-foreground transition hover:bg-ai-primary/10" onClick={() => setPanel('status')} aria-expanded={panel === 'status'}>
          <Activity className="mx-auto" size={18} aria-hidden="true" />状态
        </button>
      </nav>

      {panel && (
        <div className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm lg:hidden" role="dialog" aria-modal="true" aria-labelledby="mobile-panel-title">
          <div className="absolute inset-x-0 bottom-0 max-h-[78dvh] overflow-y-auto rounded-t-xl border border-ai-primary/25 bg-[#070a1a] p-4 pb-20 shadow-[0_0_40px_rgba(34,211,238,0.18)]">
            <div className="mb-3 flex items-center justify-between">
              <h2 id="mobile-panel-title" className="font-heading text-lg font-bold text-foreground">{panel === 'prompts' ? '推荐商品' : panel === 'cart' ? '购物状态' : '后端状态'}</h2>
              <IconButton label="关闭面板" onClick={() => setPanel(null)}>
                <X size={18} aria-hidden="true" />
              </IconButton>
            </div>
            {panel === 'prompts' && (
              <div className="space-y-4">
                <div>
                  <p className="mb-2 text-sm font-medium text-muted">快捷操作</p>
                  <PromptChips onPick={pickPrompt} />
                </div>
                <div className="space-y-3">
                  {mockProducts.map(product => <ProductCard key={product.id} product={product} />)}
                </div>
              </div>
            )}
            {panel === 'cart' && <CartStatusCard />}
            {panel === 'status' && <BackendStatusPanel />}
          </div>
        </div>
      )}
    </>
  )
}
