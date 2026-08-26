import request from './request'
import type { ApiResponse } from './auth'

export interface Subject {
  id: number
  name: string
  sortOrder: number
  courseCount: number
  usageCount: number
  canDelete: boolean
}

export interface SubjectRequest {
  name: string
  sortOrder?: number
}

export function getSubjects(): Promise<ApiResponse<Subject[]>> {
  return request.get('/subjects')
}

export function createSubject(data: SubjectRequest): Promise<ApiResponse<Subject>> {
  return request.post('/subjects', data)
}

export function updateSubject(id: number, data: SubjectRequest): Promise<ApiResponse<Subject>> {
  return request.put(`/subjects/${id}`, data)
}

export function deleteSubject(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/subjects/${id}`)
}
