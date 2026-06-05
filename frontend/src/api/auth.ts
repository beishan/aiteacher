import request from './request'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  userId: number
  username: string
  displayName: string
  role: string
  avatarUrl: string | null
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export function login(data: LoginParams): Promise<ApiResponse<LoginResult>> {
  return request.post('/auth/login', data)
}

export function getCurrentUser(): Promise<ApiResponse<LoginResult>> {
  return request.get('/auth/me')
}

export function changePassword(data: { oldPassword: string; newPassword: string }): Promise<ApiResponse<void>> {
  return request.put('/auth/password', data)
}
