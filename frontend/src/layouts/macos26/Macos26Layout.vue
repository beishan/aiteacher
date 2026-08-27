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
        <el-button
          v-for="(item, index) in dockItems"
          :key="item.path"
          text
          class="dock-item"
          :class="{ active: isActive(item.path) }"
          :style="dockItemStyle(index)"
          :aria-label="item.label"
          :aria-current="isActive(item.path) ? 'page' : undefined"
          @click="router.push(item.path)"
        >
          <span
            class="dock-icon-tile"
            :class="[`dock-tile-${item.tone}`, { custom: dockStore.iconStyle === 'custom' }]"
          >
            <img
              v-if="dockStore.iconStyle === 'custom' && dockStore.iconUrls[item.key]"
              class="dock-custom-icon"
              :src="dockStore.iconUrls[item.key]"
              :alt="`${item.label}自定义图标`"
            />
            <MacosDockIcon v-else class="dock-icon" :icon="item.icon" :tone="item.tone" />
          </span>
          <span class="dock-active-dot" aria-hidden="true" />
          <span class="dock-tooltip">{{ item.label }}</span>
        </el-button>

        <span class="dock-divider" aria-hidden="true" />

        <el-dropdown placement="top-end" trigger="click" @command="handleCommand">
          <el-button
            text
            class="dock-item dock-user-item"
            :style="dockItemStyle(dockItems.length)"
            aria-label="用户菜单"
          >
            <span class="dock-user-avatar">
              <img v-if="userStore.userInfo?.avatarUrl" :src="userStore.userInfo.avatarUrl" alt="用户头像" />
              <span v-else>{{ userInitial }}</span>
            </span>
            <span class="dock-tooltip">{{ userStore.displayName || '管理员' }}</span>
          </el-button>
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
import { Setting, SwitchButton } from '@element-plus/icons-vue'
import MacosDockIcon from '@/components/MacosDockIcon.vue'
import { useDockStore } from '@/stores/dock'
import { useUserStore } from '@/stores/user'
import { dockItems } from '@/config/dockItems'

const router = useRouter()
const route = useRoute()
const dockStore = useDockStore()
const userStore = useUserStore()
const dockContainerRef = ref<HTMLElement | null>(null)
const dockScales = ref<number[]>([])

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
  position: absolute; inset: auto auto max(18px, env(safe-area-inset-bottom)) 50%; z-index: 2000;
  padding: 7px 10px 9px; border: 1px solid rgba(255,255,255,.78); border-radius: calc(var(--dock-icon-size) * .44);
  background: linear-gradient(180deg,rgba(255,255,255,.44),transparent 44%), rgba(244,249,255,var(--dock-opacity));
  box-shadow: 0 22px 52px rgba(33,53,82,.2), 0 5px 14px rgba(33,53,82,.12), inset 0 1px 0 rgba(255,255,255,.96);
  transform: translateX(-50%); backdrop-filter: blur(var(--dock-blur)) saturate(190%) contrast(102%); -webkit-backdrop-filter: blur(var(--dock-blur)) saturate(190%) contrast(102%); isolation: isolate;
}
.dock-nav::before { position: absolute; inset: 1px 8% auto; z-index: -1; height: 45%; border-radius: inherit; background: linear-gradient(180deg,rgba(255,255,255,.58),transparent); pointer-events: none; content: ''; }
.dock-container { display: flex; align-items: flex-end; gap: 6px; }
.dock-item.el-button {
  position: relative; display: flex; width: var(--dock-icon-size); height: var(--dock-icon-size); flex: 0 0 var(--dock-icon-size); align-items: center; justify-content: center; padding: 0; border: 0; background: transparent; color: var(--color-text-secondary); cursor: pointer;
  min-height: 0; margin: 0; transform: translateY(calc(-1 * var(--dock-item-lift, 0px))) scale(var(--dock-item-scale,1)); transform-origin: bottom center; transition: transform 90ms cubic-bezier(.2,.8,.2,1); will-change: transform;
}
.dock-item.el-button:hover,
.dock-item.el-button:focus,
.dock-item.el-button:focus-visible,
.dock-item.el-button:active {
  border-color: transparent !important; background: transparent !important; box-shadow: none !important; outline: none !important;
}
.dock-item :deep(> span) { display: contents; }
.dock-item:focus-visible .dock-icon { filter: saturate(116%) brightness(1.04) drop-shadow(0 0 8px rgba(0,122,255,.38)); }
.dock-item:focus-visible .dock-custom-icon { filter: drop-shadow(0 6px 8px rgba(29,46,70,.2)) drop-shadow(0 0 8px rgba(0,122,255,.34)); }
.dock-icon-tile {
  position: relative; display: grid; width: calc(var(--dock-icon-size) - 4px); height: calc(var(--dock-icon-size) - 4px); place-items: center; overflow: visible;
}
.dock-icon-tile.custom { overflow: visible; border: 0; border-radius: 0; background: transparent; box-shadow: none; }
.dock-custom-icon { width: 100%; height: 100%; object-fit: contain; filter: drop-shadow(0 6px 8px rgba(29,46,70,.2)); }
.dock-icon { position: relative; z-index: 1; width: 100%; height: 100%; transition: filter 180ms ease; }
.dock-item:hover .dock-icon,.dock-item.active .dock-icon { filter: saturate(112%) brightness(1.025) drop-shadow(0 7px 10px rgba(31,69,112,.18)); }
.dock-active-dot { position: absolute; bottom: -6px; width: 4px; height: 4px; border-radius: 50%; background: transparent; }
.dock-item.active .dock-active-dot { background: #255f99; box-shadow: 0 0 5px rgba(0,122,255,.35); }
.dock-tooltip { position: absolute; bottom: calc(100% + 12px); left: 50%; padding: 6px 12px; border: 1px solid rgba(255,255,255,.68); border-radius: 9px; background: rgba(28,44,65,.84); box-shadow: 0 8px 22px rgba(20,38,61,.2); color: #fff; font-size: 12px; font-weight: 500; opacity: 0; visibility: hidden; white-space: nowrap; pointer-events: none; transform: translateX(-50%); transition: all .2s ease; backdrop-filter: blur(12px); }
.dock-tooltip::after { position: absolute; top: 100%; left: 50%; border: 6px solid transparent; border-top-color: rgba(28,44,65,.84); content: ''; transform: translateX(-50%); }
.dock-item:hover .dock-tooltip { opacity: 1; visibility: visible; transform: translateX(-50%) translateY(-4px); }
.dock-divider { width: 1px; height: calc(var(--dock-icon-size) * .72); margin: 0 7px; flex: 0 0 1px; background: rgba(70,91,119,.22); box-shadow: 1px 0 rgba(255,255,255,.58); }
.dock-user-avatar { display: grid; width: calc(var(--dock-icon-size) - 8px); height: calc(var(--dock-icon-size) - 8px); place-items: center; overflow: hidden; border: 2px solid rgba(255,255,255,.94); border-radius: 50%; background: linear-gradient(145deg,#56adff,#7558dc); box-shadow: 0 7px 16px rgba(31,69,112,.22), inset 0 1px 0 rgba(255,255,255,.72); color: #fff; font-size: calc(var(--dock-icon-size) * .34); font-weight: 700; }
.dock-user-avatar img { width: 100%; height: 100%; object-fit: cover; }
.dock-user-item:hover .dock-user-avatar { box-shadow: 0 9px 22px rgba(31,69,112,.28),0 0 0 3px rgba(0,122,255,.14); }
.macos-page-enter-active,.macos-page-leave-active { transition: opacity .18s ease,transform .18s ease; }
.macos-page-enter-from { opacity: 0; transform: translateY(7px); }
.macos-page-leave-to { opacity: 0; transform: translateY(-4px); }
@media (max-width: 900px) {
  .dock-item { width: min(var(--dock-icon-size),48px); height: min(var(--dock-icon-size),48px); flex-basis: min(var(--dock-icon-size),48px); }
  .dock-icon-tile { width: calc(min(var(--dock-icon-size),48px) - 4px); height: calc(min(var(--dock-icon-size),48px) - 4px); }
  .dock-icon { width: 100%; height: 100%; }
  .dock-user-avatar { width: calc(min(var(--dock-icon-size),48px) - 8px); height: calc(min(var(--dock-icon-size),48px) - 8px); }
}
@media (max-width: 620px) {
  .macos26-content { padding: 14px 10px 88px; }
  .dock-nav { inset: auto 8px max(10px,env(safe-area-inset-bottom)); padding: 6px 8px 8px; transform: none; }
  .dock-container { overflow-x: auto; overflow-y: hidden; scrollbar-width: none; }
  .dock-container::-webkit-scrollbar { display: none; }
  .dock-item { width: min(var(--dock-icon-size),42px); height: min(var(--dock-icon-size),42px); flex-basis: min(var(--dock-icon-size),42px); transform: none; }
  .dock-icon-tile { width: calc(min(var(--dock-icon-size),42px) - 4px); height: calc(min(var(--dock-icon-size),42px) - 4px); }
  .dock-icon { width: 100%; height: 100%; }
  .dock-user-avatar { width: calc(min(var(--dock-icon-size),42px) - 8px); height: calc(min(var(--dock-icon-size),42px) - 8px); }
  .dock-divider { height: 30px; margin: 0 4px; }
}
@media (hover:none) { .dock-tooltip { display: none; } }
@media (prefers-reduced-motion:reduce) { .dock-item,.macos-page-enter-active,.macos-page-leave-active { transition: none; } }
</style>
