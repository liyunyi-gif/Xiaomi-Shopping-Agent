export interface ChatRequest {
  userId?: string
  conversationId?: string
  message: string
}

export type AgentIntent = 'KNOWLEDGE' | 'SHOPPING' | 'SYSTEM' | string

export type ChatReplyData = Record<string, unknown>

export interface ChatReply {
  answer: string
  intent?: AgentIntent | null
  needClarify?: boolean
  clarifyQuestion?: string | null
  qualityLevel?: string | null
  retryCount?: number | null
  childCalls?: number | null
  data?: ChatReplyData | null
  traceId?: string | null
}

export interface HealthResponse {
  status: string
  project: string
  arch: string
}

export interface ReadyResponse {
  bootstrap: string
  orchestrator: string
  knowledgeGateway: string
  shoppingGateway: string
  postgres: string
  redis: string
  mcpserver: string
  chatModel: string
  embeddingModel: string
  rerank: string
  status: 'UP' | 'DEGRADED' | 'DOWN' | string
}

export interface ApiError {
  message: string
  status?: number
  code?: string
  raw?: unknown
}
