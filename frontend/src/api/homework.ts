import request from './request'
import type { ApiResponse } from './auth'

export interface Homework {
  id: number
  title: string
  studentId: number | null
  studentName: string | null
  classId: number | null
  className: string | null
  subject: string | null
  content: string | null
  dueDate: string | null
  status: string
  scoreType: string | null
  score: string | null
  comment: string | null
  aiReport: string | null
  submissionCount: number
  createdAt: string
}

export interface HomeworkQuery {
  studentId?: number
  classId?: number
  subject?: string
  status?: string
  page?: number
  size?: number
}

export interface HomeworkRequest {
  title: string
  studentId?: number
  classId?: number
  subject?: string
  content?: string
  dueDate?: string
  scoreType?: string
}

export interface Submission {
  id: number
  homeworkId: number
  studentId: number
  studentName: string
  submittedAt: string
  files: string[] | null
  content: string | null
  status: string
}

export interface SubmissionRequest {
  studentId?: number
  files?: string[]
  content?: string
}

export interface GradeRequest {
  score: string
  comment?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// 作业 CRUD
export function getHomeworks(params: HomeworkQuery): Promise<ApiResponse<PageResult<Homework>>> {
  return request.get('/homeworks', { params })
}

export function getHomework(id: number): Promise<ApiResponse<Homework>> {
  return request.get(`/homeworks/${id}`)
}

export function createHomework(data: HomeworkRequest): Promise<ApiResponse<Homework>> {
  return request.post('/homeworks', data)
}

export function updateHomework(id: number, data: HomeworkRequest): Promise<ApiResponse<Homework>> {
  return request.put(`/homeworks/${id}`, data)
}

export function deleteHomework(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/homeworks/${id}`)
}

// 作业提交
export function submitHomework(id: number, data: SubmissionRequest): Promise<ApiResponse<Submission>> {
  return request.post(`/homeworks/${id}/submit`, data)
}

export function getSubmissions(homeworkId: number): Promise<ApiResponse<Submission[]>> {
  return request.get(`/homeworks/${homeworkId}/submissions`)
}

// 作业批改
export function gradeHomework(id: number, data: GradeRequest): Promise<ApiResponse<Homework>> {
  return request.put(`/homeworks/${id}/grade`, data)
}
