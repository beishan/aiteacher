import request from './request'
import type { ApiResponse } from './auth'

export interface VirtualClass {
  id: number
  name: string
  subject: string | null
  description: string | null
  status: string
  memberCount: number
  members: ClassMember[]
  createdAt: string
}

export interface ClassMember {
  id: number
  classId: number
  studentId: number
  studentName: string
  grade: string | null
  joinedAt: string
}

export interface ClassroomRequest {
  name: string
  subject?: string
  description?: string
}

// 班级 CRUD
export function getClassrooms(): Promise<ApiResponse<VirtualClass[]>> {
  return request.get('/classrooms')
}

export function getClassroom(id: number): Promise<ApiResponse<VirtualClass>> {
  return request.get(`/classrooms/${id}`)
}

export function createClassroom(data: ClassroomRequest): Promise<ApiResponse<VirtualClass>> {
  return request.post('/classrooms', data)
}

export function updateClassroom(id: number, data: ClassroomRequest): Promise<ApiResponse<VirtualClass>> {
  return request.put(`/classrooms/${id}`, data)
}

export function deleteClassroom(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/classrooms/${id}`)
}

export function updateClassroomStatus(id: number, status: string): Promise<ApiResponse<void>> {
  return request.put(`/classrooms/${id}/status`, null, { params: { status } })
}

// 成员管理
export function getClassMembers(classId: number): Promise<ApiResponse<ClassMember[]>> {
  return request.get(`/classrooms/${classId}/members`)
}

export function addClassMember(classId: number, studentId: number): Promise<ApiResponse<void>> {
  return request.post(`/classrooms/${classId}/members`, null, { params: { studentId } })
}

export function removeClassMember(classId: number, studentId: number): Promise<ApiResponse<void>> {
  return request.delete(`/classrooms/${classId}/members/${studentId}`)
}
