import request from './request'
import type { ApiResponse } from './auth'

export interface Notification {
  id: number
  type: string
  title: string
  content: string | null
  recipientType: string
  recipientId: number
  channel: string
  status: string
  sendTime: string | null
  createdAt: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// 通知列表
export function getNotifications(params?: {
  status?: string
  page?: number
  size?: number
}): Promise<ApiResponse<PageResult<Notification>>> {
  return request.get('/notifications', { params })
}

// 未读数量
export function getUnreadCount(): Promise<ApiResponse<number>> {
  return request.get('/notifications/unread-count')
}

// 标记已读
export function markAsRead(id: number): Promise<ApiResponse<void>> {
  return request.put(`/notifications/${id}/read`)
}

// 全部已读
export function markAllAsRead(): Promise<ApiResponse<void>> {
  return request.post('/notifications/read-all')
}
