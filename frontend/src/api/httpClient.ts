import axios, { type AxiosInstance } from 'axios'
import type { ApiError } from './types'

export const httpClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15_000,
})

export function normalizeApiError(error: unknown): ApiError {
  if (axios.isAxiosError(error)) {
    return {
      message: error.response?.data?.message || error.message || '接口请求失败',
      status: error.response?.status,
      code: error.code,
      raw: error,
    }
  }

  if (error instanceof Error) {
    return { message: error.message, raw: error }
  }

  return { message: '未知错误', raw: error }
}

httpClient.interceptors.response.use(
  response => response,
  error => Promise.reject(normalizeApiError(error)),
)
