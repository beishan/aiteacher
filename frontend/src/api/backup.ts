import request from './request'

// 备份记录类型
export interface BackupRecord {
  id: number
  fileName: string
  filePath: string
  fileSize: number
  backupType: 'MANUAL' | 'AUTO'
  status: 'IN_PROGRESS' | 'COMPLETED' | 'FAILED'
  durationMs: number
  remark: string
  errorMessage: string
  createdAt: string
  updatedAt: string
}

// 自动备份配置
export interface AutoBackupConfig {
  enabled: boolean
  maxKeep: number
  backupDir: string
}

// 分页结果
interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// 创建手动备份
export function createBackup(remark?: string) {
  return request.post('/backups', { remark: remark || '手动备份' })
}

// 获取备份列表
export function listBackups(params: {
  backupType?: string
  status?: string
  page?: number
  size?: number
}) {
  return request.get('/backups', { params })
}

// 删除备份
export function deleteBackup(id: number) {
  return request.delete(`/backups/${id}`)
}

// 恢复备份
export function restoreBackup(id: number) {
  return request.post(`/backups/${id}/restore`)
}

// 获取自动备份配置
export function getAutoBackupConfig() {
  return request.get('/backups/config')
}

// 下载备份文件
export function getBackupDownloadUrl(id: number): string {
  return `/api/v1/backups/${id}/download`
}
