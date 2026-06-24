import { httpClient } from './httpClient'
import type { ChatReply, ChatRequest, HealthResponse, ReadyResponse } from './types'

export const agentApi = {
  chat: (body: ChatRequest) =>
    httpClient.post<ChatReply>('/chat', body).then(response => response.data),

  health: () =>
    httpClient.get<HealthResponse>('/health').then(response => response.data),

  ready: () =>
    httpClient.get<ReadyResponse>('/ready').then(response => response.data),
}
