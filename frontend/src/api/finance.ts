import request from './request'
import type { ApiResponse } from './auth'

export interface FinanceRecord {
  id: number
  studentId: number
  studentName: string
  feeId: number | null
  amount: number
  paymentType: string
  paymentDate: string
  paymentMethod: string | null
  note: string | null
  createdBy: number | null
  createdAt: string
}

export interface FinanceRecordQuery {
  studentId?: number
  paymentType?: string
  paymentMethod?: string
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}

export interface FinanceSummary {
  totalIncome: number
  monthIncome: number
  lastMonthIncome: number
  growthRate: number
  totalRefund: number
  recordCount: number
}

export interface MonthlyTrend {
  month: string
  income: number
  refund: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// 费用记录列表
export function getFinanceRecords(params: FinanceRecordQuery): Promise<ApiResponse<PageResult<FinanceRecord>>> {
  return request.get('/finance/records', { params })
}

// 收入摘要统计
export function getFinanceSummary(): Promise<ApiResponse<FinanceSummary>> {
  return request.get('/finance/summary')
}

// 月度收入趋势
export function getFinanceTrend(months = 12): Promise<ApiResponse<MonthlyTrend[]>> {
  return request.get('/finance/trend', { params: { months } })
}
