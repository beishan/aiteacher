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
  remark: string | null
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

export function updateProfile(data: { displayName: string; remark: string }): Promise<ApiResponse<LoginResult>> {
  return request.put('/auth/profile', data)
}

export function uploadAvatar(file: File): Promise<ApiResponse<LoginResult>> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/auth/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function removeAvatar(): Promise<ApiResponse<LoginResult>> {
  return request.delete('/auth/avatar')
}
