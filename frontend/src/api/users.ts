import request from './request'
import type { ApiResponse } from './auth'
import type { UserRole } from '@/config/permissions'

export interface SystemUser {
  id: number
  username: string
  displayName: string
  role: UserRole
  enabled: boolean
  createdAt: string
}

export interface CreateSystemUserPayload {
  username: string
  displayName: string
  password: string
  role: UserRole
}

export function getSystemUsers(): Promise<ApiResponse<SystemUser[]>> {
  return request.get('/users')
}

export function createSystemUser(payload: CreateSystemUserPayload): Promise<ApiResponse<SystemUser>> {
  return request.post('/users', payload)
}

export function resetUserPassword(userId: number, newPassword: string): Promise<ApiResponse<void>> {
  return request.put(`/users/${userId}/password`, { newPassword })
}

export function updateUserEnabled(userId: number, enabled: boolean): Promise<ApiResponse<void>> {
  return request.put(`/users/${userId}/enabled`, { enabled })
}

export function updateUserRole(userId: number, role: UserRole): Promise<ApiResponse<void>> {
  return request.put(`/users/${userId}/role`, { role })
}
