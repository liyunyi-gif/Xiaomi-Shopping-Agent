import { ChatPanel } from '../chat/ChatPanel'
import { LeftPanel } from './LeftPanel'
import { MobileBottomSheet } from './MobileBottomSheet'
import { RightPanel } from './RightPanel'
import { TopBar } from './TopBar'

export function AppShell() {
  return (
    <div className="min-h-dvh bg-[radial-gradient(circle_at_top_left,#fff3ea,transparent_28%),radial-gradient(circle_at_top_right,#ede9fe,transparent_30%),var(--color-background)] pb-20 lg:pb-0">
      <TopBar />
      <main className="mx-auto grid max-w-[1440px] gap-4 p-4 md:p-6 lg:grid-cols-[minmax(220px,0.9fr)_minmax(420px,1.7fr)_minmax(260px,1fr)]">
        <LeftPanel />
        <ChatPanel />
        <RightPanel />
      </main>
      <MobileBottomSheet />
    </div>
  )
}
