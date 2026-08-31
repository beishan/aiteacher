<template>
  <div v-if="userStore.role === 'VIEWER'" class="access-mode-badge" role="status" aria-label="当前为只读模式">
    <el-icon><Lock /></el-icon>
    <span><strong>只读模式</strong><small>可查看数据，不可修改业务内容</small></span>
  </div>
</template>

<script setup lang="ts">
import { Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>

<style scoped>
.access-mode-badge { position: fixed; top: 14px; right: 18px; z-index: 1200; display: flex; align-items: center; gap: 9px; padding: 8px 12px; border: 1px solid color-mix(in srgb, var(--color-accent, #409eff) 24%, transparent); border-radius: 12px; background: color-mix(in srgb, var(--color-bg-card, #fff) 88%, transparent); box-shadow: 0 9px 26px rgba(31,45,61,.14); color: var(--color-text-primary, #303133); backdrop-filter: blur(18px) saturate(150%); }
.access-mode-badge > .el-icon { color: var(--color-accent, #409eff); font-size: 17px; }
.access-mode-badge span, .access-mode-badge strong, .access-mode-badge small { display: block; }
.access-mode-badge strong { font-size: 12px; line-height: 1.2; }
.access-mode-badge small { margin-top: 2px; color: var(--color-text-secondary, #909399); font-size: 10px; line-height: 1.2; }
@media (max-width: 680px) { .access-mode-badge { top: auto; right: 12px; bottom: 92px; } .access-mode-badge small { display: none; } }
</style>
