import { CartStatusCard } from '../cart/CartStatusCard'
import { BackendStatusPanel } from '../status/BackendStatusPanel'

export function RightPanel() {
  return (
    <aside className="hidden min-h-0 flex-col gap-4 overflow-y-auto lg:flex">
      <CartStatusCard />
      <BackendStatusPanel />
    </aside>
  )
}
