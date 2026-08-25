<template>
  <section class="dock-settings" aria-labelledby="dock-settings-title">
    <header class="dock-settings-header">
      <div>
        <div id="dock-settings-title" class="dock-settings-title">
          <span class="dock-settings-symbol" aria-hidden="true" />
          <span>Dock 设置</span>
          <el-tag size="small" round>MACOS</el-tag>
        </div>
        <p>调整液态玻璃 Dock 的尺寸、通透感与悬浮反馈，效果会实时应用。</p>
      </div>
      <el-button :icon="RefreshLeft" text @click="resetDock">恢复默认</el-button>
    </header>

    <div class="dock-settings-body">
      <div class="dock-preview-stage">
        <div class="preview-wallpaper" aria-hidden="true"><i /><i /></div>
        <div class="dock-preview" :style="previewStyle">
          <span
            v-for="(item, index) in previewItems"
            :key="item.key"
            class="dock-preview-item"
            :class="{ active: index === 1, magnified: index === 2 }"
          >
            <span class="preview-icon-tile" :class="[`preview-tile-${item.tone}`, { custom: dockStore.iconStyle === 'custom' }]">
              <img v-if="customIconUrl(item.key)" class="preview-custom-icon" :src="customIconUrl(item.key)" alt="" />
              <MacosDockIcon v-else class="preview-icon" :icon="item.icon" :tone="item.tone" />
            </span>
            <i class="preview-active-dot" />
          </span>
        </div>
        <span class="preview-caption">实时预览</span>
      </div>

      <div class="dock-controls">
        <section class="dock-icon-config" aria-labelledby="dock-icon-style-title">
          <div class="dock-icon-config-copy">
            <div id="dock-icon-style-title" class="control-label"><span>图标风格</span></div>
            <p>使用内置 macOS 26 图标，或为每个 Dock 入口上传自己的图片。</p>
          </div>
          <div class="dock-icon-options">
            <button
              v-for="option in iconStyleOptions"
              :key="option.value"
              type="button"
              class="dock-icon-option"
              :class="{ active: dockStore.iconStyle === option.value }"
              :aria-pressed="dockStore.iconStyle === option.value"
              @click="changeIconStyle(option.value)"
            >
              <span class="dock-icon-option-preview">
                <span v-for="item in previewItems.slice(0, 3)" :key="item.key" class="icon-style-sample">
                  <img v-if="option.value === 'custom' && dockStore.iconUrls[item.key]" :src="dockStore.iconUrls[item.key]" alt="" />
                  <MacosDockIcon v-else :icon="item.icon" :tone="item.tone" />
                </span>
              </span>
              <span class="dock-icon-option-copy"><strong>{{ option.label }}</strong><small>{{ option.description }}</small></span>
              <el-icon v-if="dockStore.iconStyle === option.value" class="dock-icon-option-check"><Check /></el-icon>
            </button>
          </div>

          <div v-if="dockStore.iconStyle === 'custom'" class="custom-icon-settings">
            <div class="custom-icon-heading">
              <strong>自定义图标</strong>
              <small>支持 PNG、JPG，单张不超过 5MB；建议使用透明背景的正方形图片。</small>
            </div>
            <div class="custom-icon-grid">
              <article v-for="item in dockItems" :key="item.key" class="custom-icon-card">
                <div class="custom-icon-image" :class="{ empty: !dockStore.iconUrls[item.key] }">
                  <img v-if="dockStore.iconUrls[item.key]" :src="dockStore.iconUrls[item.key]" :alt="`${item.label}自定义图标`" />
                  <MacosDockIcon v-else :icon="item.icon" :tone="item.tone" />
                </div>
                <strong>{{ item.label }}</strong>
                <div class="custom-icon-actions">
                  <el-upload
                    accept="image/png,image/jpeg"
                    :auto-upload="false"
                    :show-file-list="false"
                    :disabled="dockStore.uploading[item.key]"
                    :on-change="file => handleIconUpload(item.key, item.label, file)"
                  >
                    <el-button size="small" :loading="dockStore.uploading[item.key]">上传</el-button>
                  </el-upload>
                  <el-button size="small" text type="danger" :disabled="dockStore.uploading[item.key] || !dockStore.iconUrls[item.key]" @click="removeIcon(item.key, item.label)">移除</el-button>
                </div>
              </article>
            </div>
          </div>
        </section>

        <div v-for="control in controls" :key="control.key" class="dock-control">
          <div class="dock-control-copy">
            <div class="control-label">
              <span>{{ control.label }}</span>
              <output>{{ controlValue(control.key) }}{{ control.unit }}</output>
            </div>
            <p>{{ control.description }}</p>
          </div>
          <el-slider
            :model-value="controlValue(control.key)"
            :min="control.min"
            :max="control.max"
            :step="control.step"
            :show-tooltip="false"
            :aria-label="control.label"
            @input="value => dockStore.update(control.key, Number(value))"
            @change="dockStore.persist"
          />
        </div>
        <el-alert title="设置会保存到服务器，并在下次登录后继续应用到页面底部 Dock。" type="info" :closable="false" show-icon />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Check, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadFile } from 'element-plus'
import MacosDockIcon from '@/components/MacosDockIcon.vue'
import { useDockStore } from '@/stores/dock'
import type { DockIconStyle } from '@/stores/dock'
import { dockItems } from '@/config/dockItems'
import type { DockIconName } from '@/config/dockItems'

type DockKey = 'size' | 'opacity' | 'magnification' | 'blur'
const dockStore = useDockStore()
const previewItems = dockItems.slice(0, 5)
const iconStyleOptions: Array<{ value: DockIconStyle; label: string; description: string }> = [
  { value: 'macos26', label: 'macOS 26', description: '液态玻璃彩色图标' },
  { value: 'custom', label: '自定义', description: '完整显示上传图片' },
]
const controls: Array<{ key: DockKey; label: string; description: string; min: number; max: number; step: number; unit: string }> = [
  { key: 'size', label: 'Dock 大小', description: '调整图标和玻璃托盘的整体尺寸。', min: 44, max: 76, step: 1, unit: ' px' },
  { key: 'opacity', label: '透明度', description: '控制玻璃底色的浓淡，数值越低越通透。', min: 40, max: 96, step: 1, unit: '%' },
  { key: 'magnification', label: '悬浮放大', description: '设置指针靠近图标时的最大放大比例。', min: 100, max: 150, step: 1, unit: '%' },
  { key: 'blur', label: '玻璃模糊', description: '调整背景折射的柔和程度。', min: 8, max: 40, step: 1, unit: ' px' },
]

const controlValue = (key: DockKey) => dockStore[key]
const previewStyle = computed(() => ({
  '--preview-opacity': String(dockStore.opacity / 100),
  '--preview-blur': `${dockStore.blur}px`,
  '--preview-size': `${Math.round(dockStore.size * .72)}px`,
  '--preview-scale': String(dockStore.magnification / 100),
}))
const customIconUrl = (name: DockIconName) => dockStore.iconStyle === 'custom' ? dockStore.iconUrls[name] : ''

async function changeIconStyle(style: DockIconStyle) {
  if (dockStore.iconStyle === style) return
  try {
    await dockStore.persistIconStyle(style)
    ElMessage.success(style === 'custom' ? '已启用自定义 Dock 图标' : '已恢复 macOS 26 Dock 图标')
  } catch {
    ElMessage.warning('图标风格已切换，但服务器保存失败')
  }
}

async function handleIconUpload(name: DockIconName, label: string, uploadFile: UploadFile) {
  const file = uploadFile.raw
  if (!file) return
  try {
    await dockStore.uploadIcon(name, file)
    ElMessage.success(`${label}图标已更新`)
  } catch (error) {
    if (error instanceof Error && !('response' in error)) ElMessage.error(error.message)
  }
}

async function removeIcon(name: DockIconName, label: string) {
  const confirmed = await ElMessageBox.confirm(`确定移除“${label}”的自定义图标吗？`, '移除图标', {
    confirmButtonText: '移除', cancelButtonText: '取消', type: 'warning',
  }).then(() => true).catch(() => false)
  if (!confirmed) return
  try {
    await dockStore.removeIcon(name)
    ElMessage.success(`${label}图标已移除`)
  } catch {
    // request interceptor handles errors
  }
}

async function resetDock() {
  dockStore.reset()
  try {
    await dockStore.persist()
    ElMessage.success('Dock 外观已恢复默认设置并保存')
  } catch {
    ElMessage.warning('Dock 已恢复默认，但服务器保存失败')
  }
}
</script>

<style scoped>
.dock-settings { margin-top: 28px; overflow: hidden; border: 1px solid var(--color-border-light,#e4e7ed); border-radius: 20px; background: rgba(255,255,255,.28); }
.dock-settings-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 18px; padding: 20px 22px; border-bottom: 1px solid var(--color-border-light,#ebeef5); }
.dock-settings-title { display: flex; align-items: center; gap: 10px; color: var(--color-text-primary,#303133); font-size: 17px; font-weight: 700; }
.dock-settings-symbol { width: 25px; height: 8px; border: 1px solid rgba(255,255,255,.84); border-radius: 999px; background: linear-gradient(110deg,rgba(255,255,255,.94),rgba(152,205,255,.58)); box-shadow: 0 4px 12px rgba(41,91,145,.18),inset 0 1px 2px #fff; }
.dock-settings-header p { margin: 7px 0 0; color: var(--color-text-secondary,#909399); font-size: 13px; line-height: 1.5; }
.dock-settings-body { display: grid; gap: 24px; padding: 22px; }
.dock-preview-stage { position: relative; display: flex; min-height: 280px; align-items: flex-end; justify-content: center; padding: 36px 24px 48px; overflow: hidden; isolation: isolate; border: 1px solid rgba(255,255,255,.74); border-radius: 24px; background: linear-gradient(155deg,#d8edff 0%,#eee8ff 52%,#ffe8d5 100%); box-shadow: inset 0 1px 0 rgba(255,255,255,.92),0 18px 45px rgba(45,72,112,.14); }
.preview-wallpaper { position: absolute; inset: 0; z-index: -1; overflow: hidden; }
.preview-wallpaper::before { position: absolute; inset: 0; background: radial-gradient(circle at 72% 18%,rgba(255,255,255,.82),transparent 34%),linear-gradient(120deg,transparent 35%,rgba(255,255,255,.3) 35.5%,transparent 36%); content: ''; }
.preview-wallpaper i { position: absolute; width: 210px; height: 210px; border-radius: 42% 58% 63% 37% / 46% 38% 62% 54%; background: rgba(98,191,255,.33); }
.preview-wallpaper i:first-child { top: -40px; right: -38px; transform: rotate(22deg); }
.preview-wallpaper i:last-child { bottom: -92px; left: -42px; background: rgba(188,125,245,.25); transform: rotate(-20deg); }
.dock-preview { position: relative; display: flex; align-items: flex-end; gap: 6px; padding: 7px 10px 9px; border: 1px solid rgba(255,255,255,.78); border-radius: calc(var(--preview-size) * .44); background: linear-gradient(180deg,rgba(255,255,255,.44),transparent 44%),rgba(244,249,255,var(--preview-opacity)); box-shadow: 0 18px 44px rgba(36,58,89,.2),0 4px 12px rgba(36,58,89,.12),inset 0 1px 0 rgba(255,255,255,.96); backdrop-filter: blur(var(--preview-blur)) saturate(190%); }
.dock-preview::before { position: absolute; inset: 1px 8% auto; height: 45%; border-radius: inherit; background: linear-gradient(180deg,rgba(255,255,255,.58),transparent); content: ''; }
.dock-preview-item { position: relative; display: grid; width: var(--preview-size); height: var(--preview-size); place-items: center; transform-origin: bottom center; }
.dock-preview-item.magnified { z-index: 2; transform: translateY(calc((1 - var(--preview-scale)) * var(--preview-size) * .72)) scale(var(--preview-scale)); }
.preview-icon-tile { position: relative; display: grid; width: calc(var(--preview-size) - 4px); height: calc(var(--preview-size) - 4px); place-items: center; overflow: hidden; border: 1px solid rgba(255,255,255,.84); border-radius: 26%; background: radial-gradient(circle at 24% 14%,#fff,transparent 38%),linear-gradient(145deg,rgba(229,244,255,.95),rgba(147,201,239,.78)); box-shadow: 0 6px 14px rgba(31,69,112,.18),inset 0 1px 0 #fff; }
.preview-icon-tile.custom { overflow: visible; border: 0; border-radius: 0; background: transparent; box-shadow: none; }
.preview-custom-icon { width: 100%; height: 100%; object-fit: contain; filter: drop-shadow(0 5px 7px rgba(29,46,70,.18)); }
.preview-tile-indigo { background: radial-gradient(circle at 24% 14%,#fff,transparent 38%),linear-gradient(145deg,rgba(239,238,255,.96),rgba(177,180,236,.8)); }
.preview-tile-pink { background: radial-gradient(circle at 24% 14%,#fff,transparent 38%),linear-gradient(145deg,rgba(255,236,247,.96),rgba(235,164,204,.8)); }
.preview-tile-green { background: radial-gradient(circle at 24% 14%,#fff,transparent 38%),linear-gradient(145deg,rgba(228,250,239,.96),rgba(144,214,177,.8)); }
.preview-tile-gray { background: radial-gradient(circle at 24% 14%,#fff,transparent 38%),linear-gradient(145deg,rgba(240,244,248,.96),rgba(174,188,203,.8)); }
.preview-icon { width: calc(var(--preview-size) * .66); height: calc(var(--preview-size) * .66); }
.preview-active-dot { position: absolute; bottom: -5px; width: 4px; height: 4px; border-radius: 50%; }
.dock-preview-item.active .preview-active-dot { background: #255f99; box-shadow: 0 0 5px rgba(0,122,255,.35); }
.preview-caption { position: absolute; right: 16px; bottom: 14px; padding: 5px 9px; border-radius: 999px; background: rgba(255,255,255,.5); color: rgba(49,67,92,.7); font-size: 11px; backdrop-filter: blur(8px); }
.dock-controls { display: grid; gap: 0; }
.dock-icon-config { padding: 2px 4px 22px; border-bottom: 1px solid var(--color-border-light,#ebeef5); }
.dock-icon-config-copy p { margin: 5px 0 0; color: var(--color-text-secondary,#909399); font-size: 12px; }
.dock-icon-options { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 12px; margin-top: 16px; }
.dock-icon-option { position: relative; display: flex; min-width: 0; align-items: center; gap: 13px; padding: 13px; border: 1px solid var(--color-border-light,#dcdfe6); border-radius: 14px; background: var(--color-bg-card,#fff); color: inherit; cursor: pointer; font: inherit; text-align: left; transition: border-color .18s ease,box-shadow .18s ease,transform .18s ease; }
.dock-icon-option:hover { transform: translateY(-1px); border-color: color-mix(in srgb,var(--color-accent,#409eff) 48%,transparent); }
.dock-icon-option.active { border-color: var(--color-accent,#409eff); box-shadow: 0 0 0 2px color-mix(in srgb,var(--color-accent,#409eff) 13%,transparent); }
.dock-icon-option-preview { display: flex; flex: 0 0 auto; gap: 3px; }
.icon-style-sample { display: grid; width: 28px; height: 28px; place-items: center; }
.icon-style-sample img { width: 100%; height: 100%; object-fit: contain; }
.icon-style-sample :deep(.macos-dock-glyph) { width: 100%; height: 100%; }
.dock-icon-option-copy { display: grid; min-width: 0; flex: 1; gap: 3px; }
.dock-icon-option-copy strong { color: var(--color-text-primary,#303133); font-size: 13px; }
.dock-icon-option-copy small { color: var(--color-text-secondary,#909399); font-size: 11px; }
.dock-icon-option-check { color: var(--color-accent,#409eff); }
.custom-icon-settings { margin-top: 22px; }
.custom-icon-heading { display: flex; align-items: baseline; justify-content: space-between; gap: 14px; margin-bottom: 12px; }
.custom-icon-heading strong { color: var(--color-text-primary,#303133); font-size: 14px; }
.custom-icon-heading small { color: var(--color-text-secondary,#909399); font-size: 11px; }
.custom-icon-grid { display: grid; grid-template-columns: repeat(auto-fit,minmax(190px,1fr)); gap: 10px; }
.custom-icon-card { display: grid; grid-template-columns: 48px minmax(0,1fr); align-items: center; gap: 5px 10px; padding: 10px; border: 1px solid var(--color-border-light,#e4e7ed); border-radius: 13px; background: var(--color-bg-card,#fff); }
.custom-icon-card > strong { overflow: hidden; color: var(--color-text-primary,#303133); font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.custom-icon-image { display: grid; width: 48px; height: 48px; grid-row: span 2; place-items: center; overflow: hidden; border-radius: 12px; background: var(--color-bg-secondary,#f5f7fa); }
.custom-icon-image img { width: 100%; height: 100%; object-fit: contain; }
.custom-icon-image :deep(.macos-dock-glyph) { width: 36px; height: 36px; }
.custom-icon-actions { display: flex; align-items: center; gap: 2px; }
.dock-control { display: grid; grid-template-columns: minmax(220px,1fr) minmax(240px,1.1fr); align-items: center; gap: 24px; padding: 17px 4px; }
.dock-control + .dock-control { border-top: 1px solid var(--color-border-light,#ebeef5); }
.control-label { display: flex; align-items: center; justify-content: space-between; gap: 12px; color: var(--color-text-primary,#303133); font-weight: 650; }
.control-label output { color: var(--color-accent,#409eff); font-size: 13px; font-variant-numeric: tabular-nums; }
.dock-control-copy p { margin: 5px 0 0; color: var(--color-text-secondary,#909399); font-size: 12px; }
@media (max-width:720px) { .dock-settings-header { flex-direction: column; } .dock-control { grid-template-columns: 1fr; gap: 8px; } .dock-preview-stage { min-height: 230px; padding-right: 10px; padding-left: 10px; } .dock-icon-options { grid-template-columns: 1fr; } .custom-icon-heading { align-items: flex-start; flex-direction: column; gap: 4px; } }
</style>
