import request from './request'
import type { ApiResponse } from './auth'

export interface ServiceStatus {
  key: string
  name: string
  status: 'UP' | 'DOWN'
  detail: string
}

export interface SystemInfo {
  applicationName: string
  version: string
  overallStatus: 'UP' | 'DEGRADED'
  environment: string
  startedAt: string
  serverTime: string
  uptimeSeconds: number
  javaVersion: string
  operatingSystem: string
  processors: number
  memoryUsedMb: number
  memoryMaxMb: number
  services: ServiceStatus[]
}

export function getSystemInfo(): Promise<ApiResponse<SystemInfo>> {
  return request.get('/system/info')
}
