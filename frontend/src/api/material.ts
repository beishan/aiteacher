import request from './request'
import type { ApiResponse } from './auth'

export interface Material {
  id: number
  title: string
  filePath: string
  fileType: string | null
  fileSize: number | null
  subject: string | null
  grade: string | null
  tags: string[]
  parentId: number | null
  isFolder: boolean
  isFavorite: boolean
  ownerId: number | null
  shareToken: string | null
  shareExpiresAt: string | null
  versionCount: number
  childCount: number
  createdAt: string
  updatedAt: string
}

export interface MaterialQuery {
  parentId?: number
  subject?: string
  grade?: string
  keyword?: string
  isFolder?: boolean
  isFavorite?: boolean
  page?: number
  size?: number
}

export interface MaterialRequest {
  title: string
  filePath?: string
  fileType?: string
  fileSize?: number
  subject?: string
  grade?: string
  tags?: string[]
  parentId?: number
  isFolder?: boolean
}

export interface MaterialVersion {
  id: number
  materialId: number
  versionNum: number
  filePath: string
  fileSize: number | null
  createdBy: number | null
  createdAt: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// 资料 CRUD
export function getMaterials(params: MaterialQuery): Promise<ApiResponse<PageResult<Material>>> {
  return request.get('/materials', { params })
}

// 上传文件
export function uploadMaterial(file: File, data: { title: string; subject?: string; grade?: string; tags?: string; parentId?: number }): Promise<ApiResponse<Material>> {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('title', data.title)
  if (data.subject) formData.append('subject', data.subject)
  if (data.grade) formData.append('grade', data.grade)
  if (data.tags) formData.append('tags', data.tags)
  if (data.parentId) formData.append('parentId', String(data.parentId))
  return request.post('/materials/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 获取预览 URL
export function getPreviewUrl(id: number): Promise<ApiResponse<string>> {
  return request.get(`/materials/${id}/preview-url`)
}

// 获取 OnlyOffice 编辑器配置
export function getOnlyOfficeConfig(id: number): Promise<ApiResponse<any>> {
  return request.get(`/onlyoffice/config/${id}`)
}

export function getMaterial(id: number): Promise<ApiResponse<Material>> {
  return request.get(`/materials/${id}`)
}

export function createMaterial(data: MaterialRequest): Promise<ApiResponse<Material>> {
  return request.post('/materials', data)
}

export function updateMaterial(id: number, data: MaterialRequest): Promise<ApiResponse<Material>> {
  return request.put(`/materials/${id}`, data)
}

export function deleteMaterial(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/materials/${id}`)
}

// 收藏
export function toggleFavorite(id: number): Promise<ApiResponse<void>> {
  return request.post(`/materials/${id}/favorite`)
}

// 版本
export function getMaterialVersions(id: number): Promise<ApiResponse<MaterialVersion[]>> {
  return request.get(`/materials/${id}/versions`)
}

// 分配给学生
export function assignToStudent(materialId: number, studentId: number): Promise<ApiResponse<void>> {
  return request.post(`/materials/${materialId}/assign`, null, { params: { studentId } })
}

// 学生专属资料
export function getStudentMaterials(studentId: number): Promise<ApiResponse<Material[]>> {
  return request.get(`/materials/student/${studentId}`)
}
