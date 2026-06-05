import request from './request'
import type { ApiResponse } from './auth'

export interface ExamRecord {
  id: number
  studentId: number
  studentName: string | null
  examName: string
  examType: string | null
  examDate: string
  subject: string
  score: number
  fullScore: number
  percentage: number | null
  classRank: number | null
  gradeRank: number | null
  paperUrl: string | null
  analysis: string | null
  createdAt: string
}

export interface ExamQuery {
  studentId?: number
  subject?: string
  examType?: string
  page?: number
  size?: number
}

export interface ExamRequest {
  studentId: number
  examName: string
  examType?: string
  examDate: string
  subject: string
  score: number
  fullScore: number
  classRank?: number
  gradeRank?: number
  paperUrl?: string
  analysis?: string
}

export interface TrendVO {
  subject: string
  points: TrendPoint[]
}

export interface TrendPoint {
  examDate: string
  examName: string
  score: number
  fullScore: number
  percentage: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// 成绩 CRUD
export function getExams(params: ExamQuery): Promise<ApiResponse<PageResult<ExamRecord>>> {
  return request.get('/exams', { params })
}

export function getExam(id: number): Promise<ApiResponse<ExamRecord>> {
  return request.get(`/exams/${id}`)
}

export function createExam(data: ExamRequest): Promise<ApiResponse<ExamRecord>> {
  return request.post('/exams', data)
}

export function updateExam(id: number, data: ExamRequest): Promise<ApiResponse<ExamRecord>> {
  return request.put(`/exams/${id}`, data)
}

export function deleteExam(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/exams/${id}`)
}

// 学生成绩
export function getStudentExams(studentId: number): Promise<ApiResponse<ExamRecord[]>> {
  return request.get(`/exams/student/${studentId}`)
}

export function getStudentTrend(studentId: number): Promise<ApiResponse<TrendVO[]>> {
  return request.get(`/exams/student/${studentId}/trend`)
}
