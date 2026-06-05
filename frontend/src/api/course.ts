import request from './request'
import type { ApiResponse } from './auth'

export interface Course {
  id: number
  studentId: number | null
  studentName: string | null
  classId: number | null
  className: string | null
  subject: string
  courseType: string
  title: string | null
  startTime: string
  endTime: string
  location: string | null
  meetingLink: string | null
  recurrenceRule: string | null
  recurrenceEndDate: string | null
  parentCourseId: number | null
  status: string
  remark: string | null
  color: string | null
  createdAt: string
}

export interface CourseQuery {
  studentId?: number
  classId?: number
  subject?: string
  courseType?: string
  status?: string
  startTimeFrom?: string
  startTimeTo?: string
  page?: number
  size?: number
}

export interface CourseRequest {
  studentId?: number
  classId?: number
  subject: string
  courseType: string
  title?: string
  startTime: string
  endTime: string
  location?: string
  meetingLink?: string
  repeatType?: string
  repeatInterval?: number
  repeatEndDate?: string
  remark?: string
  color?: string
}

export interface CourseRecord {
  id: number
  courseId: number
  actualStartTime: string | null
  actualEndTime: string | null
  attendanceStatus: string
  contentSummary: string | null
  homeworkAssigned: string | null
  remark: string | null
  createdAt: string
}

export interface CourseRecordRequest {
  actualStartTime?: string
  actualEndTime?: string
  attendanceStatus: string
  contentSummary?: string
  homeworkAssigned?: string
  remark?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// 课程 CRUD
export function getCourses(params: CourseQuery): Promise<ApiResponse<PageResult<Course>>> {
  return request.get('/courses', { params })
}

export function getCourse(id: number): Promise<ApiResponse<Course>> {
  return request.get(`/courses/${id}`)
}

export function createCourse(data: CourseRequest): Promise<ApiResponse<Course[]>> {
  return request.post('/courses', data)
}

export function updateCourse(id: number, data: CourseRequest): Promise<ApiResponse<Course>> {
  return request.put(`/courses/${id}`, data)
}

export function deleteCourse(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/courses/${id}`)
}

export function updateCourseStatus(id: number, status: string): Promise<ApiResponse<void>> {
  return request.put(`/courses/${id}/status`, null, { params: { status } })
}

// 上课记录
export function completeCourse(id: number, data: CourseRecordRequest): Promise<ApiResponse<CourseRecord>> {
  return request.post(`/courses/${id}/complete`, data)
}

export function getCourseRecords(courseId: number): Promise<ApiResponse<CourseRecord[]>> {
  return request.get(`/courses/${courseId}/records`)
}

// 日历数据
export function getCalendarCourses(params: {
  start: string
  end: string
  studentId?: number
  subject?: string
  courseType?: string
}): Promise<ApiResponse<Course[]>> {
  return request.get('/courses/calendar', { params })
}

// 冲突检测
export function detectConflicts(params: {
  start: string
  end: string
  excludeId?: number
}): Promise<ApiResponse<Course[]>> {
  return request.get('/courses/conflicts', { params })
}
