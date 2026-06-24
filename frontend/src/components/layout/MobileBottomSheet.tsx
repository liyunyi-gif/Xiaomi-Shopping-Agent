import { Activity, PackageSearch, ShoppingCart, X } from 'lucide-react'
import { mockProducts } from '../../data/mockProducts'
import { useUiStore } from '../../stores/uiStore'
import { CartStatusCard } from '../cart/CartStatusCard'
import { IconButton } from '../common/IconButton'
import { ProductCard } from '../product/ProductCard'
import { BackendStatusPanel } from '../status/BackendStatusPanel'

export function MobileBottomSheet() {
  const panel = useUiStore(state => state.mobilePanel)
  const setPanel = useUiStore(state => state.setMobilePanel)

  return (
    <>
      <nav className="fixed inset-x-0 bottom-0 z-30 grid grid-cols-3 border-t border-border bg-white p-2 lg:hidden" aria-label="移动端快捷面板">
        <button className="min-h-11 rounded-md text-sm" onClick={() => setPanel('prompts')}>
          <PackageSearch className="mx-auto" size={18} aria-hidden="true" />推荐
        </button>
        <button className="min-h-11 rounded-md text-sm" onClick={() => setPanel('cart')}>
          <ShoppingCart className="mx-auto" size={18} aria-hidden="true" />购物
        </button>
        <button className="min-h-11 rounded-md text-sm" onClick={() => setPanel('status')}>
          <Activity className="mx-auto" size={18} aria-hidden="true" />状态
        </button>
      </nav>

      {panel && (
        <div className="fixed inset-0 z-40 bg-black/40 lg:hidden" role="dialog" aria-modal="true">
          <div className="absolute inset-x-0 bottom-0 max-h-[78dvh] overflow-y-auto rounded-t-xl bg-background p-4 pb-20 shadow-xl">
            <div className="mb-3 flex items-center justify-between">
              <h2 className="font-heading text-lg font-bold">{panel === 'prompts' ? '推荐商品' : panel === 'cart' ? '购物状态' : '后端状态'}</h2>
              <IconButton label="关闭面板" onClick={() => setPanel(null)}>
                <X size={18} aria-hidden="true" />
              </IconButton>
            </div>
            {panel === 'prompts' && <div className="space-y-3">{mockProducts.map(product => <ProductCard key={product.id} product={product} />)}</div>}
            {panel === 'cart' && <CartStatusCard />}
            {panel === 'status' && <BackendStatusPanel />}
          </div>
        </div>
      )}
    </>
  )
}
