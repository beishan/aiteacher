<template>
  <section class="site-icon-settings">
    <div class="section-heading">
      <div>
        <h3>网站图标</h3>
        <p>用于浏览器标签页和收藏夹，上传前可以移动和缩放图片完成裁剪。</p>
      </div>
      <el-tag round effect="plain">推荐 512×512</el-tag>
    </div>

    <div class="icon-setting-card">
      <div class="current-icon" :class="{ empty: !brandingStore.iconUrl }">
        <img v-if="brandingStore.iconUrl" :src="brandingStore.iconUrl" alt="当前网站图标" />
        <el-icon v-else><Picture /></el-icon>
      </div>
      <div class="icon-setting-copy">
        <strong>{{ brandingStore.iconUrl ? '当前网站图标' : '尚未设置自定义图标' }}</strong>
        <span>支持 PNG、JPG、WebP，图片不超过 5MB。</span>
      </div>
      <el-upload
        accept="image/png,image/jpeg,image/webp"
        :auto-upload="false"
        :show-file-list="false"
        :on-change="handleFileChange"
      >
        <el-button type="primary" plain :icon="Upload">选择图片</el-button>
      </el-upload>
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="裁剪网站图标"
      width="min(520px, 94vw)"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
      @closed="releasePreview"
    >
      <div class="crop-dialog-content">
        <div
          class="crop-viewport"
          :style="{ width: `${viewportSize}px`, height: `${viewportSize}px` }"
          @pointerdown="startDrag"
          @pointermove="moveImage"
          @pointerup="stopDrag"
          @pointercancel="stopDrag"
        >
          <img
            v-if="previewUrl"
            :src="previewUrl"
            :style="imageStyle"
            alt="待裁剪图片"
            draggable="false"
          />
          <span class="crop-grid" aria-hidden="true" />
        </div>
        <div class="zoom-control">
          <el-icon><ZoomOut /></el-icon>
          <el-slider v-model="zoom" :min="1" :max="3" :step="0.01" :show-tooltip="false" @input="clampPosition" />
          <el-icon><ZoomIn /></el-icon>
        </div>
        <el-alert title="拖动图片调整位置，使用滑块缩放；裁剪结果将保存为 512×512 PNG。" type="info" :closable="false" show-icon />
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="cropAndUpload">裁剪并上传</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadFile } from 'element-plus'
import { Picture, Upload, ZoomIn, ZoomOut } from '@element-plus/icons-vue'
import { uploadSiteIcon } from '@/api/settings'
import { useBrandingStore } from '@/stores/branding'

const OUTPUT_SIZE = 512
const brandingStore = useBrandingStore()
const dialogVisible = ref(false)
const uploading = ref(false)
const previewUrl = ref('')
const imageWidth = ref(1)
const imageHeight = ref(1)
const zoom = ref(1)
const panX = ref(0)
const panY = ref(0)
const viewportSize = ref(Math.min(320, Math.max(220, window.innerWidth - 72)))
let sourceImage: HTMLImageElement | null = null
let dragging = false
let lastPointerX = 0
let lastPointerY = 0

const baseScale = computed(() => Math.max(viewportSize.value / imageWidth.value, viewportSize.value / imageHeight.value))
const displayScale = computed(() => baseScale.value * zoom.value)
const renderedWidth = computed(() => imageWidth.value * displayScale.value)
const renderedHeight = computed(() => imageHeight.value * displayScale.value)
const imageStyle = computed(() => ({
  width: `${renderedWidth.value}px`,
  height: `${renderedHeight.value}px`,
  transform: `translate(calc(-50% + ${panX.value}px), calc(-50% + ${panY.value}px))`,
}))

function handleFileChange(uploadFile: UploadFile) {
  const file = uploadFile.raw
  if (!file) return
  if (!['image/png', 'image/jpeg', 'image/webp'].includes(file.type)) {
    ElMessage.warning('仅支持 PNG、JPG 和 WebP 图片')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return
  }

  releasePreview()
  const url = URL.createObjectURL(file)
  const image = new Image()
  image.onload = () => {
    if (image.naturalWidth < 32 || image.naturalHeight < 32) {
      URL.revokeObjectURL(url)
      ElMessage.warning('图片尺寸不能小于 32×32')
      return
    }
    sourceImage = image
    previewUrl.value = url
    imageWidth.value = image.naturalWidth
    imageHeight.value = image.naturalHeight
    zoom.value = 1
    panX.value = 0
    panY.value = 0
    dialogVisible.value = true
  }
  image.onerror = () => {
    URL.revokeObjectURL(url)
    ElMessage.error('图片读取失败，请重新选择')
  }
  image.src = url
}

function clampPosition() {
  const maxX = Math.max(0, (renderedWidth.value - viewportSize.value) / 2)
  const maxY = Math.max(0, (renderedHeight.value - viewportSize.value) / 2)
  panX.value = Math.min(maxX, Math.max(-maxX, panX.value))
  panY.value = Math.min(maxY, Math.max(-maxY, panY.value))
}

function startDrag(event: PointerEvent) {
  dragging = true
  lastPointerX = event.clientX
  lastPointerY = event.clientY
  ;(event.currentTarget as HTMLElement).setPointerCapture(event.pointerId)
}

function moveImage(event: PointerEvent) {
  if (!dragging) return
  panX.value += event.clientX - lastPointerX
  panY.value += event.clientY - lastPointerY
  lastPointerX = event.clientX
  lastPointerY = event.clientY
  clampPosition()
}

function stopDrag() {
  dragging = false
}

async function cropAndUpload() {
  if (!sourceImage) return
  uploading.value = true
  try {
    const scale = displayScale.value
    const sourceSize = viewportSize.value / scale
    const sourceX = (renderedWidth.value / 2 - viewportSize.value / 2 - panX.value) / scale
    const sourceY = (renderedHeight.value / 2 - viewportSize.value / 2 - panY.value) / scale
    const canvas = document.createElement('canvas')
    canvas.width = OUTPUT_SIZE
    canvas.height = OUTPUT_SIZE
    const context = canvas.getContext('2d')
    if (!context) throw new Error('浏览器不支持图片裁剪')
    context.imageSmoothingEnabled = true
    context.imageSmoothingQuality = 'high'
    context.drawImage(sourceImage, sourceX, sourceY, sourceSize, sourceSize, 0, 0, OUTPUT_SIZE, OUTPUT_SIZE)
    const blob = await new Promise<Blob>((resolve, reject) => {
      canvas.toBlob(value => value ? resolve(value) : reject(new Error('图片生成失败')), 'image/png')
    })
    const response = await uploadSiteIcon(new File([blob], 'site-icon.png', { type: 'image/png' }))
    brandingStore.setIcon(response.data)
    dialogVisible.value = false
    ElMessage.success('网站图标已更新')
  } catch (error) {
    if (error instanceof Error && !('response' in error)) ElMessage.error(error.message)
  } finally {
    uploading.value = false
  }
}

function releasePreview() {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = ''
  sourceImage = null
  dragging = false
}

function updateViewportSize() {
  viewportSize.value = Math.min(320, Math.max(220, window.innerWidth - 72))
  clampPosition()
}

onMounted(() => window.addEventListener('resize', updateViewportSize))
onBeforeUnmount(() => {
  window.removeEventListener('resize', updateViewportSize)
  releasePreview()
})
</script>

<style scoped>
.site-icon-settings { margin-bottom: 26px; padding-bottom: 26px; border-bottom: 1px solid var(--color-border-light, #ebeef5); }
.section-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
.section-heading h3 { margin: 0 0 6px; color: var(--color-text-primary, #303133); font-size: 16px; }
.section-heading p { margin: 0; color: var(--color-text-secondary, #909399); font-size: 13px; }
.icon-setting-card { display: flex; align-items: center; gap: 16px; padding: 18px; border: 1px solid var(--color-border-light, #e4e7ed); border-radius: 16px; background: color-mix(in srgb, var(--color-bg-card, #fff) 82%, var(--color-bg-secondary, #f5f7fa)); }
.current-icon { display: grid; width: 72px; height: 72px; flex: 0 0 72px; place-items: center; overflow: hidden; border: 1px solid rgba(0,0,0,.08); border-radius: 17px; background: #fff; box-shadow: 0 8px 20px rgba(31,45,61,.12); }
.current-icon img { width: 100%; height: 100%; object-fit: cover; }
.current-icon.empty { color: var(--color-text-tertiary, #909399); font-size: 29px; }
.icon-setting-copy { display: flex; min-width: 0; flex: 1; flex-direction: column; gap: 6px; }
.icon-setting-copy strong { color: var(--color-text-primary, #303133); font-size: 15px; }
.icon-setting-copy span { color: var(--color-text-secondary, #909399); font-size: 12px; line-height: 1.5; }
.crop-dialog-content { display: flex; flex-direction: column; align-items: center; gap: 20px; }
.crop-viewport { position: relative; flex: 0 0 auto; overflow: hidden; border-radius: 28px; background: #e9edf3; box-shadow: inset 0 0 0 1px rgba(0,0,0,.1), 0 12px 32px rgba(27,45,75,.14); cursor: grab; touch-action: none; user-select: none; }
.crop-viewport:active { cursor: grabbing; }
.crop-viewport img { position: absolute; top: 50%; left: 50%; max-width: none; pointer-events: none; }
.crop-grid { position: absolute; inset: 0; border: 2px solid rgba(255,255,255,.9); border-radius: inherit; background: linear-gradient(90deg, transparent 33.1%, rgba(255,255,255,.55) 33.3%, rgba(255,255,255,.55) 33.6%, transparent 33.8%, transparent 66.1%, rgba(255,255,255,.55) 66.3%, rgba(255,255,255,.55) 66.6%, transparent 66.8%), linear-gradient(0deg, transparent 33.1%, rgba(255,255,255,.55) 33.3%, rgba(255,255,255,.55) 33.6%, transparent 33.8%, transparent 66.1%, rgba(255,255,255,.55) 66.3%, rgba(255,255,255,.55) 66.6%, transparent 66.8%); pointer-events: none; }
.zoom-control { display: flex; width: min(360px, 100%); align-items: center; gap: 14px; color: var(--color-text-secondary, #606266); }
.zoom-control .el-slider { flex: 1; }
@media (max-width: 640px) {
  .icon-setting-card { align-items: flex-start; flex-wrap: wrap; }
  .icon-setting-copy { min-width: calc(100% - 92px); }
  .icon-setting-card :deep(.el-upload) { margin-left: 88px; }
}
</style>
