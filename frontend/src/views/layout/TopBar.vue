<template>
  <div class="top-bar">
    <div class="left">
      <el-icon
        class="collapse-btn"
        :size="20"
        @click="$emit('toggle-collapse')"
      >
        <Fold v-if="!isCollapse" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentTitle">
          {{ currentTitle }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="right">
      <!-- 通知铃铛 -->
      <el-popover placement="bottom" :width="350" trigger="click">
        <template #reference>
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notification-badge">
            <el-icon :size="20" class="notification-bell"><Bell /></el-icon>
          </el-badge>
        </template>

        <div class="notification-panel">
          <div class="notification-header">
            <span>通知</span>
            <el-button type="primary" link @click="handleReadAll" v-if="unreadCount > 0">
              全部已读
            </el-button>
          </div>

          <div class="notification-list" v-if="notifications.length > 0">
            <div
              v-for="item in notifications"
              :key="item.id"
              class="notification-item"
              :class="{ unread: item.status === 'UNREAD' }"
              @click="handleRead(item)"
            >
              <div class="notification-title">{{ item.title }}</div>
              <div class="notification-content">{{ item.content }}</div>
              <div class="notification-time">{{ formatTime(item.createdAt) }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无通知" :image-size="60" />
        </div>
      </el-popover>

      <!-- 用户下拉菜单 -->
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" :icon="UserFilled" />
          <span class="username">{{ userStore.displayName }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>个人信息
            </el-dropdown-item>
            <el-dropdown-item command="password">
              <el-icon><Lock /></el-icon>修改密码
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { computed } from 'vue'
import { Fold, Expand, UserFilled, User, Lock, SwitchButton, ArrowDown, Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification'
import type { Notification } from '@/api/notification'

defineProps<{
  isCollapse: boolean
}>()

defineEmits<{
  'toggle-collapse': []
}>()

const route = useRoute()
const userStore = useUserStore()
const unreadCount = ref(0)
const notifications = ref<Notification[]>([])

const currentTitle = computed(() => {
  return (route.meta?.title as string) || ''
})

function formatTime(dateStr: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return date.toLocaleDateString('zh-CN')
}

async function fetchNotifications() {
  try {
    const [countRes, listRes] = await Promise.all([
      getUnreadCount(),
      getNotifications({ size: 10 }),
    ])
    unreadCount.value = countRes.data
    notifications.value = listRes.data.records
  } catch (error) {
    // handled
  }
}

async function handleRead(notification: Notification) {
  if (notification.status === 'UNREAD') {
    try {
      await markAsRead(notification.id)
      notification.status = 'READ'
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      // handled
    }
  }
}

async function handleReadAll() {
  try {
    await markAllAsRead()
    notifications.value.forEach(n => n.status = 'READ')
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    // handled
  }
}

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
  }
}

onMounted(() => {
  fetchNotifications()
  // 每分钟刷新一次通知
  setInterval(fetchNotifications, 60000)
})
</script>

<style scoped>
.top-bar {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  color: #606266;
}

.collapse-btn:hover {
  color: #409EFF;
}

.right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notification-bell {
  cursor: pointer;
  color: #606266;
}

.notification-bell:hover {
  color: #409EFF;
}

.notification-badge :deep(.el-badge__content) {
  font-size: 10px;
}

.notification-panel {
  max-height: 400px;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 12px;
  font-weight: 600;
}

.notification-list {
  max-height: 300px;
  overflow-y: auto;
}

.notification-item {
  padding: 10px;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 8px;
}

.notification-item:hover {
  background: #f5f7fa;
}

.notification-item.unread {
  background: #ecf5ff;
}

.notification-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.notification-content {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-time {
  font-size: 12px;
  color: #c0c4cc;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #606266;
}

.username {
  font-size: 14px;
}
</style>
