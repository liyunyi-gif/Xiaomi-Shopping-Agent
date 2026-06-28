import { useEffect } from 'react'
import { useStatusStore } from '../../stores/statusStore'
import { ChatPanel } from '../chat/ChatPanel'
import { LeftPanel } from './LeftPanel'
import { MobileBottomSheet } from './MobileBottomSheet'
import { RightPanel } from './RightPanel'
import { TopBar } from './TopBar'

export function AppShell() {
  const refreshStatus = useStatusStore(state => state.refresh)

  useEffect(() => {
    void refreshStatus()
  }, [refreshStatus])

  return (
    <div className="relative flex min-h-dvh flex-col overflow-x-hidden pb-20 lg:h-dvh lg:overflow-hidden lg:pb-0">
      <TopBar />
      <main className="mx-auto grid w-full max-w-[1760px] flex-1 gap-4 p-4 md:p-6 lg:min-h-0 lg:grid-cols-[clamp(240px,19vw,320px)_minmax(520px,1fr)_clamp(260px,21vw,350px)] lg:items-stretch xl:gap-5">
        <LeftPanel />
        <ChatPanel />
        <RightPanel />
      </main>
      <MobileBottomSheet />
    </div>
  )
}
