import { create } from 'zustand'

type MobilePanel = 'prompts' | 'cart' | 'status' | null

interface UiState {
  mobilePanel: MobilePanel
  rightPanelExpanded: boolean
  setMobilePanel: (panel: MobilePanel) => void
  toggleRightPanel: () => void
}

export const useUiStore = create<UiState>(set => ({
  mobilePanel: null,
  rightPanelExpanded: false,
  setMobilePanel: mobilePanel => set({ mobilePanel }),
  toggleRightPanel: () => set(state => ({ rightPanelExpanded: !state.rightPanelExpanded })),
}))
