import request from './request'
import type { ApiResponse } from './auth'

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

export interface ChatRequest {
  message: string
  history?: ChatMessage[]
}

export interface ChatResponse {
  reply: string
  modelName: string
  inputTokens?: number
  outputTokens?: number
  durationMs?: number
}

export interface GradeReport {
  totalScore: number
  correctCount: number
  wrongCount: number
  questions: QuestionResult[]
  summary: string
  knowledgePoints: string[]
  suggestion: string
}

export interface QuestionResult {
  questionNumber: number
  questionContent: string
  studentAnswer: string
  correctAnswer: string
  isCorrect: boolean
  explanation: string
}

export interface TeachingPlan {
  title: string
  goal: string
  phases: PlanPhase[]
  materials: string[]
  summary: string
}

export interface PlanPhase {
  phase: string
  duration: string
  tasks: string[]
  expectedOutcome: string
}

// AI 问答
export function aiChat(data: ChatRequest): Promise<ApiResponse<ChatResponse>> {
  return request.post('/ai/chat', data)
}

// AI 批改作业
export function aiGradeHomework(params: {
  subject: string
  grade: string
  content: string
  answer: string
}): Promise<ApiResponse<GradeReport>> {
  return request.post('/ai/grade-homework', null, { params })
}

// AI 生成教学计划
export function aiTeachingPlan(params: {
  studentName: string
  grade: string
  subject: string
  level: string
  goal: string
}): Promise<ApiResponse<TeachingPlan>> {
  return request.post('/ai/teaching-plan', null, { params })
}
