import { create } from 'zustand'
import { agentApi } from '../api/agentApi'
import type { ApiError, ChatReply } from '../api/types'
import { createId, getOrCreateLocalId } from '../utils/ids'
import { parseShoppingState, type ParsedShoppingState } from '../utils/responseParser'

export interface ChatMessage {
  id: string
  role: 'user' | 'assistant' | 'system' | 'error'
  content: string
  createdAt: number
  meta?: {
    intent?: string | null
    needClarify?: boolean
    qualityLevel?: string | null
    retryCount?: number | null
    childCalls?: number | null
  }
}

interface ChatState {
  userId: string
  conversationId: string
  messages: ChatMessage[]
  draft: string
  loading: boolean
  lastReply?: ChatReply
  shoppingState?: ParsedShoppingState
  setDraft: (draft: string) => void
  sendMessage: (message?: string) => Promise<void>
  resetConversation: () => void
}

const welcome: ChatMessage = {
  id: 'welcome',
  role: 'assistant',
  content: '你好，我是小米智能导购。你可以问我商品参数、让我推荐手机，或帮你加购。',
  createdAt: Date.now(),
}

export const useChatStore = create<ChatState>((set, get) => ({
  userId: getOrCreateLocalId('xiaomi-agent-user-id', 'user'),
  conversationId: getOrCreateLocalId('xiaomi-agent-conversation-id', 'conv'),
  messages: [welcome],
  draft: '',
  loading: false,

  setDraft: draft => set({ draft }),

  sendMessage: async input => {
    const text = (input ?? get().draft).trim()
    if (!text || get().loading) return

    const userMessage: ChatMessage = {
      id: createId('msg'),
      role: 'user',
      content: text,
      createdAt: Date.now(),
    }

    set(state => ({
      messages: [...state.messages, userMessage],
      draft: input ? state.draft : '',
      loading: true,
    }))

    try {
      const reply = await agentApi.chat({
        userId: get().userId,
        conversationId: get().conversationId,
        message: text,
      })

      const assistantMessage: ChatMessage = {
        id: createId('msg'),
        role: reply.intent === 'SYSTEM' ? 'system' : 'assistant',
        content: reply.answer,
        createdAt: Date.now(),
        meta: {
          intent: reply.intent,
          needClarify: reply.needClarify,
          qualityLevel: reply.qualityLevel,
          retryCount: reply.retryCount,
          childCalls: reply.childCalls,
        },
      }

      set(state => ({
        messages: [...state.messages, assistantMessage],
        loading: false,
        lastReply: reply,
        shoppingState: parseShoppingState(reply.answer, reply.needClarify) ?? state.shoppingState,
      }))
    } catch (error) {
      const apiError = error as ApiError
      const errorMessage: ChatMessage = {
        id: createId('msg'),
        role: 'error',
        content: apiError.message || '导购服务暂时不可用，请稍后重试。',
        createdAt: Date.now(),
      }
      set(state => ({ messages: [...state.messages, errorMessage], loading: false }))
    }
  },

  resetConversation: () => {
    const next = createId('conv')
    localStorage.setItem('xiaomi-agent-conversation-id', next)
    set({ conversationId: next, messages: [welcome], lastReply: undefined, shoppingState: undefined })
  },
}))
