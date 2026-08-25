<template>
  <div class="macos26-layout" :style="dockStore.cssVars">
    <div class="macos26-orb orb-blue" />
    <div class="macos26-orb orb-violet" />
    <header class="macos26-menubar glass-surface">
      <div class="menubar-brand">
        <span class="traffic-lights" aria-hidden="true">
          <i class="traffic-red" />
          <i class="traffic-yellow" />
          <i class="traffic-green" />
        </span>
        <span class="brand-mark"><el-icon><Monitor /></el-icon></span>
        <strong>家教助手</strong>
      </div>

      <div class="menubar-title">
        <el-icon><component :is="currentIcon" /></el-icon>
        <span>{{ currentTitle }}</span>
      </div>

      <div class="menubar-actions">
        <span class="menubar-time">{{ currentTime }}</span>
        <el-dropdown trigger="click" @command="handleCommand">
          <button class="user-pill" type="button">
            <el-avatar :size="30" :icon="UserFilled" />
            <span>{{ userStore.displayName || '管理员' }}</span>
            <el-icon><ArrowDown /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="settings" :icon="Setting">系统设置</el-dropdown-item>
              <el-dropdown-item divided command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <main class="macos26-content">
      <router-view v-slot="{ Component }">
        <transition name="macos-page" mode="out-in">
          <component :is="Component" :key="$route.fullPath" />
        </transition>
      </router-view>
    </main>

    <nav
      class="macos26-dock glass-surface"
      aria-label="主要导航"
      @pointermove="handlePointerMove"
      @pointerleave="hoveredIndex = null"
    >
      <el-tooltip
        v-for="(item, index) in dockItems"
        :key="item.path"
        :content="item.label"
        placement="top"
        :show-after="300"
      >
        <button
          class="dock-item"
          :class="{ active: isActive(item.path) }"
          :style="dockItemStyle(index)"
          type="button"
          :aria-label="item.label"
          :aria-current="isActive(item.path) ? 'page' : undefined"
          @click="router.push(item.path)"
        >
          <span class="dock-icon" :class="`dock-tone-${item.tone}`">
            <el-icon><component :is="item.icon" /></el-icon>
          </span>
          <i class="active-dot" />
        </button>
      </el-tooltip>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  Box,
  Calendar,
  ChatDotRound,
  DataAnalysis,
  DataLine,
  FolderOpened,
  HomeFilled,
  Monitor,
  Notebook,
  School,
  Setting,
  SwitchButton,
  User,
  UserFilled,
  Wallet,
} from '@element-plus/icons-vue'
import { useDockStore } from '@/stores/dock'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const dockStore = useDockStore()
const userStore = useUserStore()
const hoveredIndex = ref<number | null>(null)
const currentTime = ref('')

const dockItems = [
  { path: '/dashboard', label: '首页', icon: HomeFilled, tone: 'blue' },
  { path: '/students', label: '学生', icon: User, tone: 'indigo' },
  { path: '/schedule', label: '排课', icon: Calendar, tone: 'orange' },
  { path: '/homework', label: '作业', icon: Notebook, tone: 'violet' },
  { path: '/classrooms', label: '班级', icon: School, tone: 'cyan' },
  { path: '/grades', label: '成绩', icon: DataAnalysis, tone: 'green' },
  { path: '/finance', label: '收入', icon: Wallet, tone: 'mint' },
  { path: '/materials', label: '资料', icon: FolderOpened, tone: 'yellow' },
  { path: '/ai-chat', label: 'AI 助手', icon: ChatDotRound, tone: 'pink' },
  { path: '/statistics', label: '统计', icon: DataLine, tone: 'teal' },
  { path: '/backup', label: '备份', icon: Box, tone: 'slate' },
  { path: '/settings', label: '设置', icon: Setting, tone: 'gray' },
]

const currentTitle = computed(() => String(route.meta.title || '家教助手'))
const currentIcon = computed(() => dockItems.find(item => isActive(item.path))?.icon || HomeFilled)

function isActive(path: string) {
  return route.path === path || route.path.startsWith(`${path}/`)
}

function handlePointerMove(event: PointerEvent) {
  const element = (event.target as HTMLElement).closest('.dock-item') as HTMLElement | null
  const dock = event.currentTarget as HTMLElement | null
  if (!element || !dock) return
  const items = Array.from(dock.querySelectorAll('.dock-item'))
  hoveredIndex.value = items.indexOf(element)
}

function dockItemStyle(index: number) {
  if (hoveredIndex.value === null) return undefined
  const distance = Math.abs(index - hoveredIndex.value)
  const maxScale = dockStore.magnification / 100
  const scale = distance === 0 ? maxScale : distance === 1 ? 1 + (maxScale - 1) * 0.38 : 1
  return { transform: `translateY(${distance === 0 ? -7 : distance === 1 ? -3 : 0}px) scale(${scale})` }
}

function updateTime() {
  currentTime.value = new Intl.DateTimeFormat('zh-CN', {
    month: 'numeric', day: 'numeric', weekday: 'short', hour: '2-digit', minute: '2-digit', hour12: false,
  }).format(new Date())
}
updateTime()
const timer = window.setInterval(updateTime, 30_000)
onBeforeUnmount(() => window.clearInterval(timer))

async function handleCommand(command: string) {
  if (command === 'settings') {
    router.push('/settings')
    return
  }
  if (command === 'logout') {
    await ElMessageBox.confirm('确定要退出当前账号吗？', '退出登录', {
      confirmButtonText: '退出', cancelButtonText: '取消', type: 'warning',
    }).catch(() => false).then(confirmed => {
      if (confirmed) userStore.logout()
    })
  }
}
</script>

<style scoped>
.macos26-layout {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background: var(--macos-wallpaper);
  color: var(--color-text-primary);
}

.macos26-orb {
  position: fixed;
  border-radius: 50%;
  filter: blur(2px);
  pointer-events: none;
  opacity: 0.62;
}

.orb-blue { width: 38vw; height: 38vw; left: -16vw; top: 18vh; background: rgba(89, 190, 255, 0.28); }
.orb-violet { width: 30vw; height: 30vw; right: -10vw; top: 28vh; background: rgba(183, 120, 255, 0.23); }

.glass-surface {
  background: rgba(255, 255, 255, 0.56);
  border: 1px solid rgba(255, 255, 255, 0.76);
  box-shadow: var(--shadow-md), inset 0 1px 0 rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(var(--glass-blur)) saturate(180%);
  -webkit-backdrop-filter: blur(var(--glass-blur)) saturate(180%);
}

.macos26-menubar {
  position: fixed;
  z-index: 30;
  top: 10px;
  left: 14px;
  right: 14px;
  height: 50px;
  border-radius: 18px;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  padding: 0 14px;
}

.menubar-brand, .menubar-title, .menubar-actions, .user-pill { display: flex; align-items: center; }
.menubar-brand { gap: 9px; font-size: 14px; }
.brand-mark { display: grid; place-items: center; color: #007aff; font-size: 18px; }
.traffic-lights { display: flex; gap: 6px; margin-right: 4px; }
.traffic-lights i { width: 10px; height: 10px; border-radius: 50%; box-shadow: inset 0 -1px 1px rgba(0,0,0,.12); }
.traffic-red { background: #ff5f57; }
.traffic-yellow { background: #febc2e; }
.traffic-green { background: #28c840; }
.menubar-title { gap: 7px; font-size: 14px; font-weight: 600; }
.menubar-title .el-icon { color: #007aff; }
.menubar-actions { justify-content: flex-end; gap: 12px; }
.menubar-time { font-size: 12px; color: var(--color-text-secondary); white-space: nowrap; }
.user-pill { gap: 7px; border: 0; padding: 3px 8px 3px 3px; border-radius: 999px; color: inherit; background: rgba(255,255,255,.46); cursor: pointer; font: inherit; }
.user-pill:hover { background: rgba(255,255,255,.76); }
.user-pill .el-avatar { --el-avatar-bg-color: linear-gradient(135deg, #007aff, #af52de); background: linear-gradient(135deg, #007aff, #af52de); }

.macos26-content {
  position: relative;
  z-index: 2;
  height: 100vh;
  overflow: auto;
  padding: 78px clamp(14px, 2vw, 30px) calc(var(--dock-icon-size) + 62px);
}

.macos26-dock {
  position: fixed;
  z-index: 40;
  bottom: max(14px, env(safe-area-inset-bottom));
  left: 50%;
  transform: translateX(-50%);
  max-width: calc(100vw - 24px);
  height: calc(var(--dock-icon-size) + 18px);
  padding: 8px 10px;
  border-radius: 24px;
  display: flex;
  align-items: flex-end;
  gap: clamp(3px, .45vw, 8px);
  overflow-x: auto;
  overflow-y: visible;
  background: rgba(255, 255, 255, var(--dock-opacity));
  backdrop-filter: blur(var(--dock-blur)) saturate(190%);
  -webkit-backdrop-filter: blur(var(--dock-blur)) saturate(190%);
  scrollbar-width: none;
}
.macos26-dock::-webkit-scrollbar { display: none; }

.dock-item {
  position: relative;
  width: var(--dock-icon-size);
  height: var(--dock-icon-size);
  flex: 0 0 var(--dock-icon-size);
  padding: 0;
  border: 0;
  border-radius: 16px;
  background: transparent;
  cursor: pointer;
  transform-origin: center bottom;
  transition: transform 90ms ease, filter 160ms ease;
}
.dock-item:focus-visible { outline: 3px solid rgba(0,122,255,.34); outline-offset: 3px; }
.dock-icon {
  width: 100%; height: 100%; display: grid; place-items: center;
  border-radius: clamp(12px, calc(var(--dock-icon-size) * .27), 19px);
  color: #fff; font-size: calc(var(--dock-icon-size) * .48);
  box-shadow: 0 7px 16px rgba(45, 61, 89, .2), inset 0 1px 0 rgba(255,255,255,.55);
  border: 1px solid rgba(255,255,255,.45);
}
.dock-item:hover .dock-icon { filter: brightness(1.06); }
.active-dot { position: absolute; left: 50%; bottom: -6px; width: 4px; height: 4px; border-radius: 50%; background: #273447; transform: translateX(-50%) scale(0); transition: transform .18s ease; }
.dock-item.active .active-dot { transform: translateX(-50%) scale(1); }
.dock-tone-blue { background: linear-gradient(145deg, #43a5ff, #0068ed); }
.dock-tone-indigo { background: linear-gradient(145deg, #7a8cff, #4b43d8); }
.dock-tone-orange { background: linear-gradient(145deg, #ffb23f, #ff6b2c); }
.dock-tone-violet { background: linear-gradient(145deg, #c278ff, #7f45db); }
.dock-tone-cyan { background: linear-gradient(145deg, #57d8f7, #078dbf); }
.dock-tone-green { background: linear-gradient(145deg, #6fe38d, #13a64c); }
.dock-tone-mint { background: linear-gradient(145deg, #4edbb7, #079883); }
.dock-tone-yellow { background: linear-gradient(145deg, #ffd55f, #ed9f16); }
.dock-tone-pink { background: linear-gradient(145deg, #ff85bc, #d93f91); }
.dock-tone-teal { background: linear-gradient(145deg, #57d7d1, #138d98); }
.dock-tone-slate { background: linear-gradient(145deg, #8c9bad, #485667); }
.dock-tone-gray { background: linear-gradient(145deg, #aeb8c5, #697789); }

.macos-page-enter-active, .macos-page-leave-active { transition: opacity .18s ease, transform .18s ease; }
.macos-page-enter-from { opacity: 0; transform: translateY(7px) scale(.997); }
.macos-page-leave-to { opacity: 0; transform: translateY(-4px); }

@media (max-width: 760px) {
  .macos26-menubar { left: 8px; right: 8px; top: 7px; grid-template-columns: 1fr auto; }
  .traffic-lights, .menubar-title, .menubar-time, .user-pill > span { display: none; }
  .macos26-content { padding: 70px 10px calc(var(--dock-icon-size) + 55px); }
  .macos26-dock { left: 8px; right: 8px; transform: none; max-width: none; border-radius: 21px; justify-content: flex-start; }
}

@media (hover: none) {
  .dock-item { transform: none !important; }
}

@media (prefers-reduced-motion: reduce) {
  .dock-item, .macos-page-enter-active, .macos-page-leave-active { transition: none; }
}
</style>
