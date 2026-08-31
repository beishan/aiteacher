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

export function getUiPreferences(): Promise<ApiResponse<SystemSetting[]>> {
  return request.get('/settings/ui-preferences')
}

// 获取单个设置
export function getSetting(key: string): Promise<ApiResponse<SystemSetting>> {
  return request.get(`/settings/${key}`)
}

// 更新设置
export function updateSettings(settings: Record<string, string>): Promise<ApiResponse<void>> {
  return request.put('/settings', settings)
}

export function uploadSiteIcon(file: File): Promise<ApiResponse<string>> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/settings/site-icon', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function getDockIcons(): Promise<ApiResponse<Record<string, string>>> {
  return request.get('/settings/dock-icons')
}

export function uploadDockIcon(name: string, file: File): Promise<ApiResponse<string>> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/settings/dock-icons/${name}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function removeDockIcon(name: string): Promise<ApiResponse<void>> {
  return request.delete(`/settings/dock-icons/${name}`)
}
