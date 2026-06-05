<template>
  <div class="minimal-layout">
    <!-- 顶部导航栏 -->
    <header class="minimal-topbar">
      <div class="topbar-left">
        <router-link to="/" class="logo">
          <span class="logo-icon">📚</span>
          <span class="logo-text">家教助手</span>
        </router-link>
        <nav class="main-nav">
          <router-link
            v-for="item in menuItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >
            <span class="nav-icon">{{ item.icon }}</span>
            <span class="nav-label">{{ item.label }}</span>
          </router-link>
        </nav>
      </div>
      <div class="topbar-right">
        <div class="user-info" @click="toggleUserMenu">
          <div class="user-avatar">{{ userInitial }}</div>
          <span class="user-name">{{ userName }}</span>
          <span class="dropdown-arrow">▾</span>
        </div>
        <div v-if="showUserMenu" class="user-dropdown">
          <router-link to="/settings" class="dropdown-item" @click="showUserMenu = false">
            <span>⚙️</span> 设置
          </router-link>
          <div class="dropdown-divider"></div>
          <div class="dropdown-item" @click="handleLogout">
            <span>🚪</span> 退出登录
          </div>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="minimal-main">
      <div class="minimal-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const showUserMenu = ref(false)

const userName = computed(() => userStore.userInfo?.username || '用户')
const userInitial = computed(() => userName.value.charAt(0).toUpperCase())

const menuItems = [
  { path: '/', icon: '📊', label: '首页' },
  { path: '/students', icon: '👥', label: '学生' },
  { path: '/schedule', icon: '📅', label: '课程' },
  { path: '/homework', icon: '📝', label: '作业' },
  { path: '/grades', icon: '📈', label: '成绩' },
  { path: '/materials', icon: '📁', label: '资料' },
  { path: '/ai', icon: '🤖', label: 'AI' },
  { path: '/statistics', icon: '📉', label: '统计' },
]

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value
}

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

// 点击外部关闭菜单
document.addEventListener('click', (e) => {
  const target = e.target as HTMLElement
  if (!target.closest('.topbar-right')) {
    showUserMenu.value = false
  }
})
</script>

<style scoped>
.minimal-layout {
  min-height: 100vh;
  background: var(--color-bg-secondary);
  font-family: var(--font-family);
}

/* ===== 顶部导航栏 ===== */
.minimal-topbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--topbar-height);
  background: var(--color-bg-primary);
  border-bottom: 1px solid var(--color-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  z-index: 1000;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--color-text-primary);
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  letter-spacing: -0.5px;
}

/* ===== 主导航 ===== */
.main-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: var(--radius-full);
  text-decoration: none;
  color: var(--color-text-secondary);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.nav-item:hover {
  color: var(--color-text-primary);
  background: var(--color-bg-hover);
}

.nav-item.active {
  color: var(--color-accent);
  background: var(--color-accent-bg);
}

.nav-icon {
  font-size: 16px;
}

/* ===== 用户区域 ===== */
.topbar-right {
  position: relative;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.user-info:hover {
  background: var(--color-bg-hover);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-accent);
  color: var(--color-text-inverse);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.user-name {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.dropdown-arrow {
  color: var(--color-text-tertiary);
  font-size: 12px;
}

/* ===== 用户下拉菜单 ===== */
.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 160px;
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  padding: 4px;
  z-index: 1001;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  color: var(--color-text-primary);
  text-decoration: none;
  font-size: var(--font-size-base);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.dropdown-item:hover {
  background: var(--color-bg-hover);
}

.dropdown-divider {
  height: 1px;
  background: var(--color-border-light);
  margin: 4px 0;
}

/* ===== 主内容区 ===== */
.minimal-main {
  padding-top: var(--topbar-height);
  min-height: 100vh;
}

.minimal-content {
  padding: var(--content-padding);
  max-width: 1400px;
  margin: 0 auto;
}
</style>
