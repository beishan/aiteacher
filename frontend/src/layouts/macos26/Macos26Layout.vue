<template>
  <div class="macos26-layout" :style="dockStore.cssVars">
    <div class="wallpaper-orb orb-blue" />
    <div class="wallpaper-orb orb-violet" />

    <main class="macos26-content">
      <router-view v-slot="{ Component }">
        <transition name="macos-page" mode="out-in">
          <component :is="Component" :key="$route.fullPath" />
        </transition>
      </router-view>
    </main>

    <nav class="dock-nav" aria-label="主要导航">
      <div
        ref="dockContainerRef"
        class="dock-container"
        @pointermove="handleDockPointerMove"
        @pointerleave="resetDockMagnification"
      >
        <button
          v-for="(item, index) in dockItems"
          :key="item.path"
          type="button"
          class="dock-item"
          :class="{ active: isActive(item.path) }"
          :style="dockItemStyle(index)"
          :aria-label="item.label"
          :aria-current="isActive(item.path) ? 'page' : undefined"
          @click="router.push(item.path)"
        >
          <span class="dock-icon-tile" :class="`dock-tile-${item.tone}`">
            <MacosDockIcon class="dock-icon" :icon="item.icon" :tone="item.tone" />
          </span>
          <span class="dock-active-dot" aria-hidden="true" />
          <span class="dock-tooltip">{{ item.label }}</span>
        </button>

        <span class="dock-divider" aria-hidden="true" />

        <el-dropdown placement="top-end" trigger="click" @command="handleCommand">
          <button
            type="button"
            class="dock-item dock-user-item"
            :style="dockItemStyle(dockItems.length)"
            aria-label="用户菜单"
          >
            <span class="dock-user-avatar">{{ userInitial }}</span>
            <span class="dock-tooltip">{{ userStore.displayName || '管理员' }}</span>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="settings" :icon="Setting">系统设置</el-dropdown-item>
              <el-dropdown-item divided command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
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
  SwitchButton,
  User,
  Wallet,
} from '@element-plus/icons-vue'
import MacosDockIcon from '@/components/MacosDockIcon.vue'
import { useDockStore } from '@/stores/dock'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const dockStore = useDockStore()
const userStore = useUserStore()
const dockContainerRef = ref<HTMLElement | null>(null)
const dockScales = ref<number[]>([])

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

const userInitial = computed(() => (userStore.displayName || '管').charAt(0).toUpperCase())

function isActive(path: string) {
  return route.path === path || route.path.startsWith(`${path}/`)
}

function resetDockMagnification() {
  dockScales.value = Array.from({ length: dockItems.length + 1 }, () => 1)
}

function dockItemStyle(index: number) {
  const scale = dockScales.value[index] || 1
  const lift = (scale - 1) * dockStore.size * 0.72
  return {
    '--dock-item-scale': String(scale),
    '--dock-item-lift': `${lift}px`,
  }
}

function handleDockPointerMove(event: PointerEvent) {
  if (event.pointerType === 'touch' || !dockContainerRef.value) return
  const maxScale = dockStore.magnification / 100
  const influenceRadius = dockStore.size * 1.75
  const items = dockContainerRef.value.querySelectorAll<HTMLElement>('.dock-item')
  dockScales.value = Array.from(items, item => {
    const rect = item.getBoundingClientRect()
    const distance = Math.abs(event.clientX - (rect.left + rect.width / 2))
    const proximity = Math.max(0, 1 - distance / influenceRadius)
    return 1 + (maxScale - 1) * proximity * proximity
  })
}

async function handleCommand(command: string) {
  if (command === 'settings') {
    await router.push('/settings')
    return
  }
  if (command !== 'logout') return
  const confirmed = await ElMessageBox.confirm('确定要退出当前账号吗？', '退出登录', {
    confirmButtonText: '退出', cancelButtonText: '取消', type: 'warning',
  }).then(() => true).catch(() => false)
  if (confirmed) userStore.logout()
}

onMounted(resetDockMagnification)
</script>

<style scoped>
.macos26-layout { position: relative; min-height: 100vh; overflow: hidden; background: var(--macos-wallpaper); color: var(--color-text-primary); }
.wallpaper-orb { position: fixed; border-radius: 50%; pointer-events: none; opacity: .58; }
.orb-blue { width: 38vw; height: 38vw; left: -16vw; top: 18vh; background: rgba(89,190,255,.28); }
.orb-violet { width: 30vw; height: 30vw; right: -10vw; top: 28vh; background: rgba(183,120,255,.23); }
.macos26-content { position: relative; z-index: 2; height: 100vh; padding: 24px clamp(14px,2vw,30px) calc(var(--dock-icon-size) + 68px); overflow: auto; }

.dock-nav {
  position: fixed; bottom: max(18px, env(safe-area-inset-bottom)); left: 50%; z-index: 2000;
  padding: 7px 10px 9px; border: 1px solid rgba(255,255,255,.78); border-radius: calc(var(--dock-icon-size) * .44);
  background: linear-gradient(180deg,rgba(255,255,255,.44),transparent 44%), rgba(244,249,255,var(--dock-opacity));
  box-shadow: 0 22px 52px rgba(33,53,82,.2), 0 5px 14px rgba(33,53,82,.12), inset 0 1px 0 rgba(255,255,255,.96);
  transform: translateX(-50%); backdrop-filter: blur(var(--dock-blur)) saturate(190%) contrast(102%); -webkit-backdrop-filter: blur(var(--dock-blur)) saturate(190%) contrast(102%); isolation: isolate;
}
.dock-nav::before { position: absolute; inset: 1px 8% auto; z-index: -1; height: 45%; border-radius: inherit; background: linear-gradient(180deg,rgba(255,255,255,.58),transparent); pointer-events: none; content: ''; }
.dock-container { display: flex; align-items: flex-end; gap: 6px; }
.dock-item {
  position: relative; display: flex; width: var(--dock-icon-size); height: var(--dock-icon-size); flex: 0 0 var(--dock-icon-size); align-items: center; justify-content: center; padding: 0; border: 0; background: transparent; color: var(--color-text-secondary); cursor: pointer;
  transform: translateY(calc(-1 * var(--dock-item-lift, 0px))) scale(var(--dock-item-scale,1)); transform-origin: bottom center; transition: transform 90ms cubic-bezier(.2,.8,.2,1); will-change: transform;
}
.dock-item:focus-visible { outline: 3px solid rgba(0,122,255,.34); outline-offset: 3px; border-radius: 14px; }
.dock-icon-tile {
  position: relative; display: grid; width: calc(var(--dock-icon-size) - 4px); height: calc(var(--dock-icon-size) - 4px); place-items: center; overflow: hidden;
  border: 1px solid rgba(255,255,255,.84); border-radius: 26%;
  background: radial-gradient(circle at 24% 14%,#fff,transparent 38%), linear-gradient(145deg,rgba(229,244,255,.95),rgba(147,201,239,.78));
  box-shadow: 0 7px 16px rgba(31,69,112,.17), inset 0 1px 0 rgba(255,255,255,.96), inset 0 -1px 2px rgba(40,104,162,.12); transition: filter 180ms ease, box-shadow 180ms ease;
}
.dock-icon-tile::after { position: absolute; inset: 0; border-radius: inherit; background: linear-gradient(120deg,rgba(255,255,255,.3),transparent 42%); pointer-events: none; content: ''; }
.dock-tile-indigo { background: radial-gradient(circle at 24% 14%,#fff,transparent 38%), linear-gradient(145deg,rgba(239,238,255,.96),rgba(177,180,236,.8)); }
.dock-tile-orange { background: radial-gradient(circle at 24% 14%,#fff,transparent 38%), linear-gradient(145deg,rgba(255,244,218,.96),rgba(239,196,129,.8)); }
.dock-tile-violet { background: radial-gradient(circle at 24% 14%,#fff,transparent 38%), linear-gradient(145deg,rgba(247,236,255,.96),rgba(205,167,234,.8)); }
.dock-tile-cyan { background: radial-gradient(circle at 24% 14%,#fff,transparent 38%), linear-gradient(145deg,rgba(225,249,252,.96),rgba(133,210,221,.8)); }
.dock-tile-green,.dock-tile-mint { background: radial-gradient(circle at 24% 14%,#fff,transparent 38%), linear-gradient(145deg,rgba(228,250,239,.96),rgba(144,214,177,.8)); }
.dock-tile-yellow { background: radial-gradient(circle at 24% 14%,#fff,transparent 38%), linear-gradient(145deg,rgba(255,250,223,.96),rgba(237,213,138,.8)); }
.dock-tile-pink { background: radial-gradient(circle at 24% 14%,#fff,transparent 38%), linear-gradient(145deg,rgba(255,236,247,.96),rgba(235,164,204,.8)); }
.dock-tile-teal { background: radial-gradient(circle at 24% 14%,#fff,transparent 38%), linear-gradient(145deg,rgba(225,248,246,.96),rgba(133,207,204,.8)); }
.dock-tile-slate,.dock-tile-gray { background: radial-gradient(circle at 24% 14%,#fff,transparent 38%), linear-gradient(145deg,rgba(240,244,248,.96),rgba(174,188,203,.8)); }
.dock-icon { position: relative; z-index: 1; width: calc(var(--dock-icon-size) * .66); height: calc(var(--dock-icon-size) * .66); }
.dock-item:hover .dock-icon-tile,.dock-item.active .dock-icon-tile { filter: saturate(112%) brightness(1.025); box-shadow: 0 9px 22px rgba(31,69,112,.23), inset 0 1px 0 #fff, inset 0 -1px 2px rgba(40,104,162,.12); }
.dock-active-dot { position: absolute; bottom: -6px; width: 4px; height: 4px; border-radius: 50%; background: transparent; }
.dock-item.active .dock-active-dot { background: #255f99; box-shadow: 0 0 5px rgba(0,122,255,.35); }
.dock-tooltip { position: absolute; bottom: calc(100% + 12px); left: 50%; padding: 6px 12px; border: 1px solid rgba(255,255,255,.68); border-radius: 9px; background: rgba(28,44,65,.84); box-shadow: 0 8px 22px rgba(20,38,61,.2); color: #fff; font-size: 12px; font-weight: 500; opacity: 0; visibility: hidden; white-space: nowrap; pointer-events: none; transform: translateX(-50%); transition: all .2s ease; backdrop-filter: blur(12px); }
.dock-tooltip::after { position: absolute; top: 100%; left: 50%; border: 6px solid transparent; border-top-color: rgba(28,44,65,.84); content: ''; transform: translateX(-50%); }
.dock-item:hover .dock-tooltip { opacity: 1; visibility: visible; transform: translateX(-50%) translateY(-4px); }
.dock-divider { width: 1px; height: calc(var(--dock-icon-size) * .72); margin: 0 7px; flex: 0 0 1px; background: rgba(70,91,119,.22); box-shadow: 1px 0 rgba(255,255,255,.58); }
.dock-user-avatar { display: grid; width: calc(var(--dock-icon-size) - 8px); height: calc(var(--dock-icon-size) - 8px); place-items: center; overflow: hidden; border: 2px solid rgba(255,255,255,.94); border-radius: 50%; background: linear-gradient(145deg,#56adff,#7558dc); box-shadow: 0 7px 16px rgba(31,69,112,.22), inset 0 1px 0 rgba(255,255,255,.72); color: #fff; font-size: calc(var(--dock-icon-size) * .34); font-weight: 700; }
.dock-user-item:hover .dock-user-avatar { box-shadow: 0 9px 22px rgba(31,69,112,.28),0 0 0 3px rgba(0,122,255,.14); }
.macos-page-enter-active,.macos-page-leave-active { transition: opacity .18s ease,transform .18s ease; }
.macos-page-enter-from { opacity: 0; transform: translateY(7px); }
.macos-page-leave-to { opacity: 0; transform: translateY(-4px); }
@media (max-width: 900px) {
  .dock-item { width: min(var(--dock-icon-size),48px); height: min(var(--dock-icon-size),48px); flex-basis: min(var(--dock-icon-size),48px); }
  .dock-icon-tile { width: calc(min(var(--dock-icon-size),48px) - 4px); height: calc(min(var(--dock-icon-size),48px) - 4px); }
  .dock-icon { width: calc(min(var(--dock-icon-size),48px) * .66); height: calc(min(var(--dock-icon-size),48px) * .66); }
  .dock-user-avatar { width: calc(min(var(--dock-icon-size),48px) - 8px); height: calc(min(var(--dock-icon-size),48px) - 8px); }
}
@media (max-width: 620px) {
  .macos26-content { padding: 14px 10px 88px; }
  .dock-nav { right: 8px; bottom: max(10px,env(safe-area-inset-bottom)); left: 8px; padding: 6px 8px 8px; transform: none; }
  .dock-container { overflow-x: auto; overflow-y: hidden; scrollbar-width: none; }
  .dock-container::-webkit-scrollbar { display: none; }
  .dock-item { width: min(var(--dock-icon-size),42px); height: min(var(--dock-icon-size),42px); flex-basis: min(var(--dock-icon-size),42px); transform: none; }
  .dock-icon-tile { width: calc(min(var(--dock-icon-size),42px) - 4px); height: calc(min(var(--dock-icon-size),42px) - 4px); }
  .dock-icon { width: calc(min(var(--dock-icon-size),42px) * .66); height: calc(min(var(--dock-icon-size),42px) * .66); }
  .dock-user-avatar { width: calc(min(var(--dock-icon-size),42px) - 8px); height: calc(min(var(--dock-icon-size),42px) - 8px); }
  .dock-divider { height: 30px; margin: 0 4px; }
}
@media (hover:none) { .dock-tooltip { display: none; } }
@media (prefers-reduced-motion:reduce) { .dock-item,.macos-page-enter-active,.macos-page-leave-active { transition: none; } }
</style>
