<template>
  <div class="cyber-layout">
    <!-- 背景特效 -->
    <div class="cyber-bg">
      <div class="grid-lines"></div>
      <div class="glow-orb orb-1"></div>
      <div class="glow-orb orb-2"></div>
    </div>

    <!-- 侧边栏 -->
    <aside class="cyber-sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-header">
        <div class="logo-section">
          <span class="logo-icon">⚡</span>
          <span v-if="!isCollapsed" class="logo-text">TUTOR.SYS</span>
        </div>
        <button class="collapse-btn" @click="toggleSidebar">
          <span class="btn-icon">{{ isCollapsed ? '▶' : '◀' }}</span>
        </button>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
          :title="item.label"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span v-if="!isCollapsed" class="nav-label">{{ item.label }}</span>
          <span v-if="isActive(item.path)" class="active-indicator"></span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="system-status">
          <span class="status-dot"></span>
          <span v-if="!isCollapsed" class="status-text">SYSTEM ONLINE</span>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="cyber-main" :class="{ 'sidebar-collapsed': isCollapsed }">
      <!-- 顶部栏 -->
      <header class="cyber-topbar">
        <div class="topbar-left">
          <div class="breadcrumb">
            <span class="path-segment">SYS</span>
            <span class="path-divider">/</span>
            <span class="path-current">{{ currentPageTitle }}</span>
          </div>
        </div>
        <div class="topbar-right">
          <div class="status-bar">
            <span class="status-item">
              <span class="pulse-dot"></span>
              <span>{{ currentTime }}</span>
            </span>
          </div>
          <div class="user-section" @click="toggleUserMenu">
            <div class="user-avatar">
              <span class="avatar-text">{{ userInitial }}</span>
              <span class="avatar-ring"></span>
            </div>
            <div v-if="!isCollapsed" class="user-info">
              <span class="user-name">{{ userName }}</span>
              <span class="user-role">ADMIN</span>
            </div>
          </div>
          <div v-if="showUserMenu" class="user-dropdown">
            <router-link to="/settings" class="dropdown-item" @click="showUserMenu = false">
              <span class="item-icon">⚙</span>
              <span>系统配置</span>
            </router-link>
            <div class="dropdown-divider"></div>
            <div class="dropdown-item" @click="handleLogout">
              <span class="item-icon">⏻</span>
              <span>安全退出</span>
            </div>
          </div>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="cyber-content">
        <div class="cyber-page">
          <router-view />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapsed = ref(false)
const showUserMenu = ref(false)
const currentTime = ref('')

const userName = computed(() => userStore.userInfo?.username || 'USER')
const userInitial = computed(() => userName.value.charAt(0).toUpperCase())

const currentPageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/': 'DASHBOARD',
    '/students': 'STUDENTS',
    '/schedule': 'SCHEDULE',
    '/homework': 'HOMEWORK',
    '/grades': 'GRADES',
    '/materials': 'MATERIALS',
    '/ai': 'AI_ASSISTANT',
    '/statistics': 'STATISTICS',
    '/settings': 'SETTINGS',
  }
  return titles[route.path] || 'UNKNOWN'
})

const menuItems = [
  { path: '/', icon: '◈', label: '仪表盘' },
  { path: '/students', icon: '◉', label: '学生管理' },
  { path: '/schedule', icon: '◎', label: '课程排期' },
  { path: '/homework', icon: '◈', label: '作业系统' },
  { path: '/grades', icon: '◉', label: '成绩分析' },
  { path: '/materials', icon: '◎', label: '资料库' },
  { path: '/ai', icon: '◈', label: 'AI 助手' },
  { path: '/statistics', icon: '◉', label: '数据统计' },
]

let timeInterval: ReturnType<typeof setInterval>

onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  clearInterval(timeInterval)
})

function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
}

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

function toggleSidebar() {
  isCollapsed.value = !isCollapsed.value
}

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value
}

function handleLogout() {
  userStore.logout()
  ElMessage.success('已安全退出系统')
  router.push('/login')
}

document.addEventListener('click', (e) => {
  const target = e.target as HTMLElement
  if (!target.closest('.topbar-right')) {
    showUserMenu.value = false
  }
})
</script>

<style scoped>
.cyber-layout {
  min-height: 100vh;
  background: var(--color-bg-primary);
  font-family: var(--font-family);
  position: relative;
  overflow: hidden;
}

/* ===== 背景特效 ===== */
.cyber-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
}

.grid-lines {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(rgba(59, 130, 246, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(59, 130, 246, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
}

.glow-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.3;
}

.orb-1 {
  width: 300px;
  height: 300px;
  background: var(--color-neon-blue);
  top: -100px;
  right: -100px;
  animation: float 8s ease-in-out infinite;
}

.orb-2 {
  width: 250px;
  height: 250px;
  background: var(--color-neon-purple);
  bottom: -80px;
  left: -80px;
  animation: float 10s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(20px, -20px); }
}

/* ===== 侧边栏 ===== */
.cyber-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: var(--sidebar-width);
  background: rgba(17, 24, 39, 0.95);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  z-index: 100;
  transition: width var(--transition-normal);
  backdrop-filter: blur(20px);
}

.cyber-sidebar.collapsed {
  width: var(--sidebar-collapsed-width);
}

.sidebar-header {
  padding: 20px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--color-border);
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 24px;
  filter: drop-shadow(0 0 8px var(--color-neon-blue));
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-neon-blue);
  letter-spacing: 2px;
  text-shadow: 0 0 10px var(--color-neon-blue);
}

.collapse-btn {
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text-secondary);
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.collapse-btn:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
  background: var(--color-accent-bg);
}

.btn-icon {
  font-size: 10px;
}

/* ===== 导航菜单 ===== */
.sidebar-nav {
  flex: 1;
  padding: 12px 8px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 4px;
  border-radius: var(--radius-md);
  text-decoration: none;
  color: var(--color-text-secondary);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-fast);
  position: relative;
  overflow: hidden;
}

.nav-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--color-accent);
  transform: scaleY(0);
  transition: transform var(--transition-fast);
}

.nav-item:hover {
  color: var(--color-text-primary);
  background: var(--color-bg-hover);
}

.nav-item:hover::before {
  transform: scaleY(1);
}

.nav-item.active {
  color: var(--color-neon-blue);
  background: var(--color-accent-bg);
  box-shadow: var(--glow-blue);
}

.nav-item.active::before {
  transform: scaleY(1);
  background: var(--color-neon-blue);
  box-shadow: 0 0 10px var(--color-neon-blue);
}

.nav-icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
}

.active-indicator {
  position: absolute;
  right: 12px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-neon-blue);
  box-shadow: 0 0 8px var(--color-neon-blue);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* ===== 侧边栏底部 ===== */
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--color-border);
}

.system-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-neon-green);
  box-shadow: 0 0 8px var(--color-neon-green);
  animation: pulse 2s infinite;
}

.status-text {
  font-size: 11px;
  color: var(--color-text-tertiary);
  letter-spacing: 1px;
}

/* ===== 主内容区 ===== */
.cyber-main {
  margin-left: var(--sidebar-width);
  min-height: 100vh;
  position: relative;
  z-index: 1;
  transition: margin-left var(--transition-normal);
}

.cyber-main.sidebar-collapsed {
  margin-left: var(--sidebar-collapsed-width);
}

/* ===== 顶部栏 ===== */
.cyber-topbar {
  height: var(--topbar-height);
  background: rgba(17, 24, 39, 0.8);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  backdrop-filter: blur(20px);
  position: sticky;
  top: 0;
  z-index: 50;
}

.topbar-left {
  display: flex;
  align-items: center;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--font-size-sm);
}

.path-segment {
  color: var(--color-text-tertiary);
}

.path-divider {
  color: var(--color-text-tertiary);
}

.path-current {
  color: var(--color-neon-blue);
  font-weight: var(--font-weight-semibold);
  text-shadow: 0 0 8px var(--color-neon-blue);
}

/* ===== 顶部栏右侧 ===== */
.topbar-right {
  display: flex;
  align-items: center;
  gap: 24px;
  position: relative;
}

.status-bar {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  font-family: 'SF Mono', monospace;
}

.pulse-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-neon-green);
  animation: pulse 2s infinite;
}

/* ===== 用户区域 ===== */
.user-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.user-section:hover {
  background: var(--color-bg-hover);
}

.user-avatar {
  position: relative;
  width: 36px;
  height: 36px;
}

.avatar-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-neon-blue);
}

.avatar-ring {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border: 2px solid var(--color-neon-blue);
  border-radius: 50%;
  animation: rotate 3s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.user-role {
  font-size: 10px;
  color: var(--color-neon-purple);
  letter-spacing: 1px;
}

/* ===== 用户下拉菜单 ===== */
.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 180px;
  background: rgba(17, 24, 39, 0.95);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg), var(--glow-blue);
  padding: 4px;
  z-index: 1001;
  backdrop-filter: blur(20px);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: var(--radius-sm);
  color: var(--color-text-primary);
  text-decoration: none;
  font-size: var(--font-size-base);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.dropdown-item:hover {
  background: var(--color-accent-bg);
  color: var(--color-neon-blue);
}

.item-icon {
  font-size: 14px;
  width: 20px;
  text-align: center;
}

.dropdown-divider {
  height: 1px;
  background: var(--color-border);
  margin: 4px 0;
}

/* ===== 页面内容 ===== */
.cyber-content {
  min-height: calc(100vh - var(--topbar-height));
}

.cyber-page {
  padding: var(--content-padding);
  position: relative;
}
</style>
