import request from './request'
import type { ApiResponse } from './auth'

export interface DashboardStats {
  totalStudents: number
  activeStudents: number
  todayCourses: number
  pendingHomeworks: number
  monthRevenue: number
  recentCourses: RecentCourse[]
  pendingTasks: PendingTask[]
}

export interface RecentCourse {
  id: number
  studentName: string | null
  subject: string
  startTime: string
  status: string
}

export interface PendingTask {
  type: string
  title: string
  description: string
  time: string
}

export interface RevenueStats {
  totalRevenue: number
  monthRevenue: number
  lastMonthRevenue: number
  growthRate: number
  monthlyData: MonthlyRevenue[]
}

export interface MonthlyRevenue {
  month: string
  amount: number
}

export interface StudentStats {
  totalStudents: number
  activeStudents: number
  pausedStudents: number
  graduatedStudents: number
  lostStudents: number
  gradeDistribution: GradeDistribution[]
  subjectDistribution: SubjectDistribution[]
  sourceDistribution: SourceDistribution[]
}

export interface GradeDistribution {
  grade: string
  count: number
}

export interface SubjectDistribution {
  subject: string
  count: number
}

export interface SourceDistribution {
  source: string
  count: number
}

export interface CourseStats {
  totalCourses: number
  completedCourses: number
  cancelledCourses: number
  thisMonthCourses: number
  weeklyData: WeeklyCourse[]
}

export interface WeeklyCourse {
  week: string
  count: number
}

export interface HomeworkStats {
  totalHomeworks: number
  pendingHomeworks: number
  submittedHomeworks: number
  gradedHomeworks: number
  completionRate: number
  subjectData: SubjectHomework[]
}

export interface SubjectHomework {
  subject: string
  total: number
  completed: number
}

// 仪表盘数据
export function getDashboardStats(): Promise<ApiResponse<DashboardStats>> {
  return request.get('/statistics/dashboard')
}

// 收入统计
export function getRevenueStats(): Promise<ApiResponse<RevenueStats>> {
  return request.get('/statistics/revenue')
}

// 学生分布
export function getStudentStats(): Promise<ApiResponse<StudentStats>> {
  return request.get('/statistics/students')
}

// 课时统计
export function getCourseStats(): Promise<ApiResponse<CourseStats>> {
  return request.get('/statistics/courses')
}

// 作业统计
export function getHomeworkStats(): Promise<ApiResponse<HomeworkStats>> {
  return request.get('/statistics/homework')
}
