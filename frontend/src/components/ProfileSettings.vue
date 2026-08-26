<template>
  <div class="profile-settings">
    <section class="avatar-section">
      <div class="avatar-preview">
        <img v-if="userStore.userInfo?.avatarUrl" :src="userStore.userInfo.avatarUrl" alt="当前头像" />
        <span v-else>{{ avatarText }}</span>
      </div>
      <div class="avatar-content">
        <h3>个人头像</h3>
        <p>选择图片后可以拖动和缩放完成裁剪，上传后会显示在页面导航和用户菜单中。</p>
        <div class="avatar-actions">
          <el-upload
            accept="image/png,image/jpeg"
            :auto-upload="false"
            :show-file-list="false"
            :disabled="uploading"
            :on-change="handleAvatarSelect"
          >
            <el-button type="primary" :loading="uploading">选择头像</el-button>
          </el-upload>
          <el-button v-if="userStore.userInfo?.avatarUrl" :disabled="uploading" @click="handleRemoveAvatar">移除头像</el-button>
        </div>
        <small>支持 PNG、JPG，图片不超过 5MB，最小尺寸 32×32。</small>
      </div>
    </section>

    <el-dialog
      v-model="cropDialogVisible"
      title="裁剪个人头像"
      width="min(520px, 94vw)"
      append-to-body
      teleported
      align-center
      destroy-on-close
      :close-on-click-modal="false"
      @closed="releaseCropPreview"
    >
      <div class="crop-dialog-content">
        <div
          class="crop-viewport avatar-crop-viewport"
          :style="{ width: `${cropViewportSize}px`, height: `${cropViewportSize}px` }"
          @pointerdown="startCropDrag"
          @pointermove="moveCropImage"
          @pointerup="stopCropDrag"
          @pointercancel="stopCropDrag"
        >
          <img
            v-if="cropPreviewUrl"
            :src="cropPreviewUrl"
            :style="cropImageStyle"
            alt="待裁剪头像"
            draggable="false"
          />
          <span class="crop-grid" aria-hidden="true" />
        </div>
        <div class="zoom-control">
          <el-icon><ZoomOut /></el-icon>
          <el-slider v-model="cropZoom" :min="1" :max="3" :step="0.01" :show-tooltip="false" @input="clampCropPosition" />
          <el-icon><ZoomIn /></el-icon>
        </div>
        <el-alert title="拖动图片调整位置，使用滑块缩放；圆形区域为头像最终显示范围。" type="info" :closable="false" show-icon />
      </div>
      <template #footer>
        <el-button @click="cropDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="cropAndUploadAvatar">裁剪并上传</el-button>
      </template>
    </el-dialog>

    <el-divider />

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="profile-form">
      <div class="profile-grid">
        <el-form-item label="登录账号">
          <el-input :model-value="userStore.userInfo?.username" disabled />
          <span class="field-hint">登录账号由管理员维护，不能在此修改。</span>
        </el-form-item>
        <el-form-item label="账号角色">
          <el-input :model-value="roleLabel" disabled />
        </el-form-item>
      </div>
      <el-form-item label="昵称" prop="displayName">
        <el-input v-model="form.displayName" maxlength="50" show-word-limit placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="个人备注" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          resize="vertical"
          placeholder="可以填写个人简介、工作习惯或其他备注"
        />
      </el-form-item>
      <div class="form-actions">
        <el-button type="primary" :loading="saving" @click="handleSave">保存个人信息</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { UploadFile } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ZoomIn, ZoomOut } from '@element-plus/icons-vue'
import { removeAvatar, updateProfile, uploadAvatar } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const saving = ref(false)
const uploading = ref(false)
const cropDialogVisible = ref(false)
const cropPreviewUrl = ref('')
const cropImageWidth = ref(1)
const cropImageHeight = ref(1)
const cropZoom = ref(1)
const cropPanX = ref(0)
const cropPanY = ref(0)
const cropViewportSize = ref(Math.min(320, Math.max(220, window.innerWidth - 72)))
const AVATAR_OUTPUT_SIZE = 512
let cropSourceImage: HTMLImageElement | null = null
let cropDragging = false
let lastPointerX = 0
let lastPointerY = 0
const form = reactive({ displayName: '', remark: '' })
const rules: FormRules = {
  displayName: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 50, message: '昵称不能超过50个字符', trigger: 'blur' },
  ],
  remark: [{ max: 500, message: '个人备注不能超过500个字符', trigger: 'blur' }],
}

const avatarText = computed(() => (userStore.displayName || '用').slice(0, 1).toUpperCase())
const roleLabel = computed(() => userStore.userInfo?.role === 'ADMIN' ? '管理员' : '普通用户')
const cropBaseScale = computed(() => Math.max(cropViewportSize.value / cropImageWidth.value, cropViewportSize.value / cropImageHeight.value))
const cropDisplayScale = computed(() => cropBaseScale.value * cropZoom.value)
const cropRenderedWidth = computed(() => cropImageWidth.value * cropDisplayScale.value)
const cropRenderedHeight = computed(() => cropImageHeight.value * cropDisplayScale.value)
const cropImageStyle = computed(() => ({
  width: `${cropRenderedWidth.value}px`,
  height: `${cropRenderedHeight.value}px`,
  transform: `translate(calc(-50% + ${cropPanX.value}px), calc(-50% + ${cropPanY.value}px))`,
}))

watch(() => userStore.userInfo, info => {
  form.displayName = info?.displayName || info?.username || ''
  form.remark = info?.remark || ''
}, { immediate: true, deep: true })

async function handleSave() {
  if (!formRef.value || !await formRef.value.validate().catch(() => false)) return
  saving.value = true
  try {
    const response = await updateProfile({
      displayName: form.displayName.trim(),
      remark: form.remark.trim(),
    })
    userStore.updateUserInfo(response.data)
    ElMessage.success('个人信息已保存')
  } finally {
    saving.value = false
  }
}

async function handleAvatarSelect(uploadFile: UploadFile) {
  const file = uploadFile.raw
  if (!file) return
  if (!['image/png', 'image/jpeg'].includes(file.type)) {
    ElMessage.warning('请选择 PNG 或 JPG 图片')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('头像大小不能超过 5MB')
    return
  }

  releaseCropPreview()
  const url = URL.createObjectURL(file)
  const image = new Image()
  image.onload = () => {
    if (image.naturalWidth < 32 || image.naturalHeight < 32) {
      URL.revokeObjectURL(url)
      ElMessage.warning('图片尺寸不能小于 32×32')
      return
    }
    cropSourceImage = image
    cropPreviewUrl.value = url
    cropImageWidth.value = image.naturalWidth
    cropImageHeight.value = image.naturalHeight
    cropZoom.value = 1
    cropPanX.value = 0
    cropPanY.value = 0
    cropDialogVisible.value = true
  }
  image.onerror = () => {
    URL.revokeObjectURL(url)
    ElMessage.error('图片读取失败，请重新选择')
  }
  image.src = url
}

function clampCropPosition() {
  const maxX = Math.max(0, (cropRenderedWidth.value - cropViewportSize.value) / 2)
  const maxY = Math.max(0, (cropRenderedHeight.value - cropViewportSize.value) / 2)
  cropPanX.value = Math.min(maxX, Math.max(-maxX, cropPanX.value))
  cropPanY.value = Math.min(maxY, Math.max(-maxY, cropPanY.value))
}

function startCropDrag(event: PointerEvent) {
  cropDragging = true
  lastPointerX = event.clientX
  lastPointerY = event.clientY
  ;(event.currentTarget as HTMLElement).setPointerCapture(event.pointerId)
}

function moveCropImage(event: PointerEvent) {
  if (!cropDragging) return
  cropPanX.value += event.clientX - lastPointerX
  cropPanY.value += event.clientY - lastPointerY
  lastPointerX = event.clientX
  lastPointerY = event.clientY
  clampCropPosition()
}

function stopCropDrag() {
  cropDragging = false
}

async function cropAndUploadAvatar() {
  if (!cropSourceImage) return
  uploading.value = true
  try {
    const scale = cropDisplayScale.value
    const sourceSize = cropViewportSize.value / scale
    const sourceX = (cropRenderedWidth.value / 2 - cropViewportSize.value / 2 - cropPanX.value) / scale
    const sourceY = (cropRenderedHeight.value / 2 - cropViewportSize.value / 2 - cropPanY.value) / scale
    const canvas = document.createElement('canvas')
    canvas.width = AVATAR_OUTPUT_SIZE
    canvas.height = AVATAR_OUTPUT_SIZE
    const context = canvas.getContext('2d')
    if (!context) throw new Error('浏览器不支持图片裁剪')
    context.imageSmoothingEnabled = true
    context.imageSmoothingQuality = 'high'
    context.drawImage(cropSourceImage, sourceX, sourceY, sourceSize, sourceSize, 0, 0, AVATAR_OUTPUT_SIZE, AVATAR_OUTPUT_SIZE)
    const blob = await new Promise<Blob>((resolve, reject) => {
      canvas.toBlob(value => value ? resolve(value) : reject(new Error('头像生成失败')), 'image/png')
    })
    const response = await uploadAvatar(new File([blob], 'avatar.png', { type: 'image/png' }))
    userStore.updateUserInfo(response.data)
    cropDialogVisible.value = false
    ElMessage.success('头像已裁剪并更新')
  } catch (error) {
    if (error instanceof Error && !('response' in error)) ElMessage.error(error.message)
  } finally {
    uploading.value = false
  }
}

function releaseCropPreview() {
  if (cropPreviewUrl.value) URL.revokeObjectURL(cropPreviewUrl.value)
  cropPreviewUrl.value = ''
  cropSourceImage = null
  cropDragging = false
}

function updateCropViewportSize() {
  cropViewportSize.value = Math.min(320, Math.max(220, window.innerWidth - 72))
  clampCropPosition()
}

async function handleRemoveAvatar() {
  const confirmed = await ElMessageBox.confirm('移除后将恢复显示昵称首字，确定继续吗？', '移除头像', {
    confirmButtonText: '移除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => true).catch(() => false)
  if (!confirmed) return
  uploading.value = true
  try {
    const response = await removeAvatar()
    userStore.updateUserInfo(response.data)
    ElMessage.success('头像已移除')
  } finally {
    uploading.value = false
  }
}

onMounted(() => window.addEventListener('resize', updateCropViewportSize))
onBeforeUnmount(() => {
  window.removeEventListener('resize', updateCropViewportSize)
  releaseCropPreview()
})
</script>

<style scoped>
.profile-settings { max-width: 820px; }
.avatar-section { display: flex; align-items: center; gap: 24px; }
.avatar-preview { display: grid; width: 112px; height: 112px; flex: 0 0 112px; place-items: center; overflow: hidden; border: 4px solid rgba(255,255,255,.92); border-radius: 50%; background: linear-gradient(145deg, var(--color-accent-light, #66b1ff), var(--color-accent, #409eff)); box-shadow: 0 10px 28px color-mix(in srgb, var(--color-accent, #409eff) 24%, transparent); color: #fff; font-size: 38px; font-weight: 700; }
.avatar-preview img { width: 100%; height: 100%; object-fit: cover; }
.avatar-content h3 { margin: 0 0 7px; color: var(--color-text-primary, #303133); font-size: 17px; }
.avatar-content p { margin: 0 0 14px; color: var(--color-text-secondary, #909399); font-size: 13px; line-height: 1.6; }
.avatar-actions { display: flex; flex-wrap: wrap; gap: 8px; }
.avatar-content small, .field-hint { display: block; margin-top: 8px; color: var(--color-text-tertiary, #a8abb2); font-size: 12px; }
.crop-dialog-content { display: flex; flex-direction: column; align-items: center; gap: 20px; }
.crop-viewport { position: relative; flex: 0 0 auto; overflow: hidden; background: #e9edf3; box-shadow: inset 0 0 0 1px rgba(0,0,0,.1), 0 12px 32px rgba(27,45,75,.14); cursor: grab; touch-action: none; user-select: none; }
.crop-viewport:active { cursor: grabbing; }
.crop-viewport img { position: absolute; top: 50%; left: 50%; max-width: none; pointer-events: none; }
.avatar-crop-viewport { border-radius: 50%; }
.crop-grid { position: absolute; inset: 0; border: 2px solid rgba(255,255,255,.94); border-radius: inherit; background: linear-gradient(90deg, transparent 33.1%, rgba(255,255,255,.52) 33.3%, rgba(255,255,255,.52) 33.6%, transparent 33.8%, transparent 66.1%, rgba(255,255,255,.52) 66.3%, rgba(255,255,255,.52) 66.6%, transparent 66.8%), linear-gradient(0deg, transparent 33.1%, rgba(255,255,255,.52) 33.3%, rgba(255,255,255,.52) 33.6%, transparent 33.8%, transparent 66.1%, rgba(255,255,255,.52) 66.3%, rgba(255,255,255,.52) 66.6%, transparent 66.8%); pointer-events: none; }
.zoom-control { display: flex; width: min(360px, 100%); align-items: center; gap: 14px; color: var(--color-text-secondary, #606266); }
.zoom-control .el-slider { flex: 1; }
.profile-form { padding-top: 4px; }
.profile-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18px; }
.form-actions { display: flex; justify-content: flex-end; padding-top: 4px; }
@media (max-width: 640px) {
  .avatar-section { align-items: flex-start; flex-direction: column; }
  .avatar-preview { width: 92px; height: 92px; flex-basis: 92px; font-size: 32px; }
  .profile-grid { grid-template-columns: 1fr; gap: 0; }
  .form-actions .el-button { width: 100%; }
}
</style>
