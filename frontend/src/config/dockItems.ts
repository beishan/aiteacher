import type { Component } from 'vue'
import {
  Box,
  Calendar,
  ChatDotRound,
  DataAnalysis,
  DataLine,
  FolderOpened,
  HomeFilled,
  Notebook,
  School,
  Setting,
  User,
  Wallet,
} from '@element-plus/icons-vue'

export type DockIconName =
  | 'dashboard' | 'students' | 'schedule' | 'homework' | 'classrooms' | 'grades'
  | 'finance' | 'materials' | 'ai-chat' | 'statistics' | 'backup' | 'settings'

export interface DockItemDefinition {
  key: DockIconName
  path: string
  label: string
  icon: Component
  tone: string
  adminOnly?: boolean
}

export const dockItems: DockItemDefinition[] = [
  { key: 'dashboard', path: '/dashboard', label: '首页', icon: HomeFilled, tone: 'blue' },
  { key: 'students', path: '/students', label: '学生', icon: User, tone: 'indigo' },
  { key: 'schedule', path: '/schedule', label: '排课', icon: Calendar, tone: 'orange' },
  { key: 'homework', path: '/homework', label: '作业', icon: Notebook, tone: 'violet' },
  { key: 'classrooms', path: '/classrooms', label: '班级', icon: School, tone: 'cyan' },
  { key: 'grades', path: '/grades', label: '成绩', icon: DataAnalysis, tone: 'green' },
  { key: 'finance', path: '/finance', label: '收入', icon: Wallet, tone: 'mint' },
  { key: 'materials', path: '/materials', label: '资料', icon: FolderOpened, tone: 'yellow' },
  { key: 'ai-chat', path: '/ai-chat', label: 'AI 助手', icon: ChatDotRound, tone: 'pink' },
  { key: 'statistics', path: '/statistics', label: '统计', icon: DataLine, tone: 'teal' },
  { key: 'backup', path: '/backup', label: '备份', icon: Box, tone: 'slate', adminOnly: true },
  { key: 'settings', path: '/settings', label: '设置', icon: Setting, tone: 'gray' },
]
