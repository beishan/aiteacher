<template>
  <section class="dock-settings">
    <div class="dock-settings-heading">
      <div>
        <h3>Dock 设置</h3>
        <p>调整底部 Dock 的大小、透明度、放大效果和玻璃模糊强度，设置仅保存在当前浏览器。</p>
      </div>
      <el-button :icon="RefreshLeft" plain @click="dockStore.reset">恢复默认</el-button>
    </div>

    <div class="dock-preview-wrap">
      <div class="dock-preview" :style="previewStyle">
        <span v-for="item in previewItems" :key="item.tone" :class="`preview-icon tone-${item.tone}`">
          <el-icon><component :is="item.icon" /></el-icon>
        </span>
      </div>
    </div>

    <el-row :gutter="22" class="dock-controls">
      <el-col v-for="control in controls" :key="control.key" :xs="24" :md="12">
        <div class="dock-control">
          <div class="control-label">
            <span>{{ control.label }}</span>
            <el-tag size="small" round>{{ controlValue(control.key) }}{{ control.unit }}</el-tag>
          </div>
          <el-slider
            :model-value="controlValue(control.key)"
            :min="control.min"
            :max="control.max"
            :step="control.step"
            :show-tooltip="false"
            @input="value => dockStore.update(control.key, Number(value))"
          />
          <div class="control-range"><span>{{ control.min }}{{ control.unit }}</span><span>{{ control.max }}{{ control.unit }}</span></div>
        </div>
      </el-col>
    </el-row>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ChatDotRound, HomeFilled, RefreshLeft, Setting, User } from '@element-plus/icons-vue'
import { useDockStore } from '@/stores/dock'

type DockKey = 'size' | 'opacity' | 'magnification' | 'blur'
const dockStore = useDockStore()
const previewItems = [
  { icon: HomeFilled, tone: 'blue' },
  { icon: User, tone: 'indigo' },
  { icon: ChatDotRound, tone: 'pink' },
  { icon: Setting, tone: 'gray' },
]
const controls: Array<{ key: DockKey; label: string; min: number; max: number; step: number; unit: string }> = [
  { key: 'size', label: '图标大小', min: 44, max: 76, step: 1, unit: 'px' },
  { key: 'opacity', label: '背景透明度', min: 40, max: 96, step: 1, unit: '%' },
  { key: 'magnification', label: '悬停放大', min: 100, max: 150, step: 1, unit: '%' },
  { key: 'blur', label: '玻璃模糊', min: 8, max: 40, step: 1, unit: 'px' },
]

const controlValue = (key: DockKey) => dockStore[key]
const previewStyle = computed(() => ({
  '--preview-opacity': `${dockStore.opacity / 100}`,
  '--preview-blur': `${dockStore.blur}px`,
  '--preview-size': `${Math.round(dockStore.size * 0.76)}px`,
}))
</script>

<style scoped>
.dock-settings { margin-top: 28px; padding-top: 28px; border-top: 1px solid var(--color-border-light, #ebeef5); }
.dock-settings-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 18px; margin-bottom: 20px; }
.dock-settings-heading h3 { margin: 0 0 7px; font-size: 18px; color: var(--color-text-primary, #303133); }
.dock-settings-heading p { margin: 0; color: var(--color-text-secondary, #909399); line-height: 1.6; font-size: 14px; }
.dock-preview-wrap {
  min-height: 154px; display: grid; place-items: center; overflow: hidden; border-radius: 22px; margin-bottom: 24px;
  background: radial-gradient(circle at 18% 15%, rgba(83,199,255,.65), transparent 38%), radial-gradient(circle at 82% 75%, rgba(206,139,255,.55), transparent 42%), linear-gradient(145deg,#dceeff,#fff0df);
}
.dock-preview {
  display: flex; gap: 8px; align-items: center; padding: 8px 10px; border-radius: 20px;
  background: rgba(255,255,255,var(--preview-opacity)); border: 1px solid rgba(255,255,255,.82);
  backdrop-filter: blur(var(--preview-blur)) saturate(180%); box-shadow: 0 14px 34px rgba(45,63,89,.2), inset 0 1px 0 rgba(255,255,255,.8);
}
.preview-icon { width: var(--preview-size); height: var(--preview-size); display: grid; place-items: center; border-radius: 13px; color: #fff; font-size: calc(var(--preview-size) * .48); box-shadow: 0 5px 12px rgba(37,52,76,.2), inset 0 1px 0 rgba(255,255,255,.5); }
.tone-blue { background: linear-gradient(145deg,#43a5ff,#0068ed); }
.tone-indigo { background: linear-gradient(145deg,#7a8cff,#4b43d8); }
.tone-pink { background: linear-gradient(145deg,#ff85bc,#d93f91); }
.tone-gray { background: linear-gradient(145deg,#aeb8c5,#697789); }
.dock-control { padding: 16px 18px; margin-bottom: 18px; border: 1px solid var(--color-border-light, #ebeef5); border-radius: 16px; background: rgba(255,255,255,.34); }
.control-label, .control-range { display: flex; justify-content: space-between; align-items: center; }
.control-label { font-weight: 600; color: var(--color-text-primary, #303133); margin-bottom: 8px; }
.control-range { color: var(--color-text-tertiary, #909399); font-size: 11px; margin-top: -7px; }
@media (max-width: 640px) { .dock-settings-heading { flex-direction: column; } }
</style>
