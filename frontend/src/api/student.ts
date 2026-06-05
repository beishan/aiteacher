import request from './request'
import type { ApiResponse } from './auth'

export interface Student {
  id: number
  name: string
  gender: string | null
  birthDate: string | null
  age: number | null
  grade: string
  school: string | null
  subjects: string[]
  source: string | null
  address: string | null
  phone: string | null
  parentName: string | null
  parentPhone: string
  parentRelation: string | null
  remark: string | null
  enrollmentDate: string
  status: string
  avatarUrl: string | null
  tags: string[]
  createdAt: string
  updatedAt: string
}

export interface StudentQuery {
  name?: string
  grade?: string
  status?: string
  subject?: string
  page?: number
  size?: number
}

export interface StudentRequest {
  name: string
  gender?: string
  birthDate?: string
  grade: string
  school?: string
  subjects?: string[]
  source?: string
  address?: string
  phone?: string
  parentName?: string
  parentPhone: string
  parentRelation?: string
  remark?: string
  enrollmentDate: string
  tags?: string[]
}

export interface PriceTier {
  hours: number
  price: number
}

export interface StudentFee {
  id: number
  studentId: number
  feeType: string
  unitPrice: number | null
  prepaidCount: number | null
  remainingCount: number | null
  periodStart: string | null
  periodEnd: string | null
  subject: string | null
  status: string
  remark: string | null
  createdAt: string
  priceTiers: PriceTier[] | null
}

export interface StudentFeeRequest {
  feeType: string
  unitPrice?: number
  prepaidCount?: number
  periodStart?: string
  periodEnd?: string
  subject?: string
  remark?: string
  priceTiers?: PriceTier[]
}

export interface FeeRecord {
  id: number
  studentId: number
  feeId: number | null
  amount: number
  paymentType: string
  paymentDate: string
  paymentMethod: string | null
  note: string | null
  createdBy: number | null
  createdAt: string
}

export interface FeeRecordRequest {
  feeId?: number
  amount: number
  paymentType: string
  paymentDate: string
  paymentMethod?: string
  note?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// 学生 CRUD
export function getStudents(params: StudentQuery): Promise<ApiResponse<PageResult<Student>>> {
  return request.get('/students', { params })
}

export function getStudent(id: number): Promise<ApiResponse<Student>> {
  return request.get(`/students/${id}`)
}

export function createStudent(data: StudentRequest): Promise<ApiResponse<Student>> {
  return request.post('/students', data)
}

export function updateStudent(id: number, data: StudentRequest): Promise<ApiResponse<Student>> {
  return request.put(`/students/${id}`, data)
}

export function deleteStudent(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/students/${id}`)
}

export function updateStudentStatus(id: number, status: string): Promise<ApiResponse<void>> {
  return request.put(`/students/${id}/status`, null, { params: { status } })
}

// 课时费管理
export function getStudentFees(studentId: number): Promise<ApiResponse<StudentFee[]>> {
  return request.get(`/students/${studentId}/fees`)
}

export function createStudentFee(studentId: number, data: StudentFeeRequest): Promise<ApiResponse<StudentFee>> {
  return request.post(`/students/${studentId}/fees`, data)
}

export function updateStudentFee(studentId: number, feeId: number, data: StudentFeeRequest): Promise<ApiResponse<StudentFee>> {
  return request.put(`/students/${studentId}/fees/${feeId}`, data)
}

// 费用记录
export function getFeeRecords(studentId: number, page = 1, size = 20): Promise<ApiResponse<PageResult<FeeRecord>>> {
  return request.get(`/students/${studentId}/fee-records`, { params: { page, size } })
}

export function createFeeRecord(studentId: number, data: FeeRecordRequest): Promise<ApiResponse<FeeRecord>> {
  return request.post(`/students/${studentId}/fee-records`, data)
}
