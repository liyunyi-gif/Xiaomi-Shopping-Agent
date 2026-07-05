import { create } from 'zustand'
import { agentApi } from '../api/agentApi'
import type { ApiError, HealthResponse, ReadyResponse } from '../api/types'

interface StatusState {
  health?: HealthResponse
  ready?: ReadyResponse
  loading: boolean
  error?: ApiError
  lastRefreshedAt?: number
  refresh: () => Promise<void>
}

export const useStatusStore = create<StatusState>((set, get) => ({
  loading: false,
  refresh: async () => {
    if (get().loading) return
    set({ loading: true, error: undefined })
    try {
      const [health, ready] = await Promise.all([agentApi.health(), agentApi.ready()])
      set({ health, ready, loading: false, lastRefreshedAt: Date.now() })
    } catch (error) {
      set({ error: error as ApiError, loading: false, lastRefreshedAt: Date.now() })
    }
  },
}))
