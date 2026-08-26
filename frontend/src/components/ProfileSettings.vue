<template>
  <div class="profile-settings">
    <section class="avatar-section">
      <div class="avatar-preview">
        <img v-if="userStore.userInfo?.avatarUrl" :src="userStore.userInfo.avatarUrl" alt="当前头像" />
        <span v-else>{{ avatarText }}</span>
      </div>
      <div class="avatar-content">
        <h3>个人头像</h3>
        <p>上传后会显示在页面导航和用户菜单中，图片将自动居中裁剪为正方形。</p>
        <div class="avatar-actions">
          <input ref="fileInputRef" class="visually-hidden" type="file" accept="image/png,image/jpeg" @change="handleAvatarSelect" />
          <el-button type="primary" :loading="uploading" @click="fileInputRef?.click()">选择头像</el-button>
          <el-button v-if="userStore.userInfo?.avatarUrl" :disabled="uploading" @click="handleRemoveAvatar">移除头像</el-button>
        </div>
        <small>支持 PNG、JPG，图片不超过 5MB，最小尺寸 32×32。</small>
      </div>
    </section>

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
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { removeAvatar, updateProfile, uploadAvatar } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const fileInputRef = ref<HTMLInputElement>()
const saving = ref(false)
const uploading = ref(false)
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

async function handleAvatarSelect(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  if (!['image/png', 'image/jpeg'].includes(file.type)) {
    ElMessage.warning('请选择 PNG 或 JPG 图片')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('头像大小不能超过 5MB')
    return
  }
  uploading.value = true
  try {
    const response = await uploadAvatar(file)
    userStore.updateUserInfo(response.data)
    ElMessage.success('头像已更新')
  } finally {
    uploading.value = false
  }
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
.visually-hidden { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0,0,0,0); white-space: nowrap; }
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
