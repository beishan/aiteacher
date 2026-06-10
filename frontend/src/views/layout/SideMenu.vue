<template>
  <el-menu
    :default-active="activeMenu"
    :collapse="isCollapse"
    router
    class="side-menu"
    background-color="#001529"
    text-color="#ffffffb3"
    active-text-color="#409EFF"
  >
    <div class="logo">
      <el-icon :size="28" color="#409EFF"><Monitor /></el-icon>
      <span v-show="!isCollapse" class="logo-text">家教助手</span>
    </div>

    <el-menu-item
      v-for="item in menuItems"
      :key="item.path"
      :index="item.path"
    >
      <el-icon><component :is="item.icon" /></el-icon>
      <template #title>{{ item.title }}</template>
    </el-menu-item>
  </el-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Monitor } from '@element-plus/icons-vue'

defineProps<{
  isCollapse: boolean
}>()

const route = useRoute()

const activeMenu = computed(() => route.path)

const menuItems = [
  { path: '/dashboard', title: '首页', icon: 'HomeFilled' },
  { path: '/students', title: '学生管理', icon: 'User' },
  { path: '/schedule', title: '排课管理', icon: 'Calendar' },
  { path: '/homework', title: '作业管理', icon: 'Notebook' },
  { path: '/grades', title: '成绩管理', icon: 'DataAnalysis' },
  { path: '/finance', title: '收入管理', icon: 'Wallet' },
  { path: '/materials', title: '资料库', icon: 'FolderOpened' },
  { path: '/ai-chat', title: 'AI 助手', icon: 'ChatDotRound' },
  { path: '/settings', title: '系统设置', icon: 'Setting' },
]
</script>

<style scoped>
.side-menu {
  height: 100vh;
  border-right: none;
}

.side-menu:not(.el-menu--collapse) {
  width: 220px;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid #ffffff1a;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
}
</style>
