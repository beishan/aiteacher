export type UserRole = 'ADMIN' | 'TEACHER' | 'VIEWER'

export interface RoleOption {
  value: UserRole
  label: string
  description: string
}

export const roleOptions: RoleOption[] = [
  { value: 'ADMIN', label: '管理员', description: '全部权限，可管理用户、系统配置和数据备份' },
  { value: 'TEACHER', label: '教师', description: '可维护教学业务，不可管理系统与账号' },
  { value: 'VIEWER', label: '只读用户', description: '仅查看教学数据，可维护自己的账号资料' },
]

export function isUserRole(value: unknown): value is UserRole {
  return value === 'ADMIN' || value === 'TEACHER' || value === 'VIEWER'
}

export function roleName(role?: string): string {
  return roleOptions.find(item => item.value === role)?.label || '未知角色'
}

export function canAccessAdminModule(role: UserRole | undefined): boolean {
  return role === 'ADMIN'
}

export function canWriteBusiness(role: UserRole | undefined): boolean {
  return role === 'ADMIN' || role === 'TEACHER'
}
