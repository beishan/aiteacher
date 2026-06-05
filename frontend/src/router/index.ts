import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '首页', icon: 'HomeFilled' },
      },
      {
        path: 'students',
        name: 'Students',
        component: () => import('@/views/student/StudentListView.vue'),
        meta: { title: '学生管理', icon: 'User' },
      },
      {
        path: 'students/:id',
        name: 'StudentDetail',
        component: () => import('@/views/student/StudentDetailView.vue'),
        meta: { title: '学生详情', icon: 'User', hidden: true },
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: () => import('@/views/schedule/ScheduleView.vue'),
        meta: { title: '排课管理', icon: 'Calendar' },
      },
      {
        path: 'homework',
        name: 'Homework',
        component: () => import('@/views/homework/HomeworkListView.vue'),
        meta: { title: '作业管理', icon: 'Notebook' },
      },
      {
        path: 'homework/:id',
        name: 'HomeworkDetail',
        component: () => import('@/views/homework/HomeworkDetailView.vue'),
        meta: { title: '作业详情', icon: 'Notebook', hidden: true },
      },
      {
        path: 'classrooms',
        name: 'Classrooms',
        component: () => import('@/views/classroom/ClassroomListView.vue'),
        meta: { title: '虚拟班级', icon: 'School' },
      },
      {
        path: 'grades',
        name: 'Grades',
        component: () => import('@/views/grade/GradeListView.vue'),
        meta: { title: '成绩管理', icon: 'DataAnalysis' },
      },
      {
        path: 'materials',
        name: 'Materials',
        component: () => import('@/views/material/MaterialListView.vue'),
        meta: { title: '资料库', icon: 'FolderOpened' },
      },
      {
        path: 'ai-chat',
        name: 'AIChat',
        component: () => import('@/views/ai/AIChatView.vue'),
        meta: { title: 'AI 助手', icon: 'ChatDotRound' },
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/StatisticsView.vue'),
        meta: { title: '数据统计', icon: 'DataLine' },
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/SettingsView.vue'),
        meta: { title: '系统设置', icon: 'Setting' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
