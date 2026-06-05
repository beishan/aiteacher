import request from './request'
import type { ApiResponse } from './auth'

export interface SystemSetting {
  id: number
  key: string
  value: string | null
  description: string | null
  updatedAt: string
}

// 获取所有设置
export function getSettings(): Promise<ApiResponse<SystemSetting[]>> {
  return request.get('/settings')
}

// 获取单个设置
export function getSetting(key: string): Promise<ApiResponse<SystemSetting>> {
  return request.get(`/settings/${key}`)
}

// 更新设置
export function updateSettings(settings: Record<string, string>): Promise<ApiResponse<void>> {
  return request.put('/settings', settings)
}
