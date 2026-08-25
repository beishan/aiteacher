<template>
  <section class="user-management">
    <div class="user-toolbar">
      <div>
        <h3>账号列表</h3>
        <p>停用账号后，该用户已有的登录状态会立即失效。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadUsers">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="users" class="user-table" empty-text="暂无系统用户">
      <el-table-column label="用户" min-width="180">
        <template #default="{ row }">
          <div class="user-cell">
            <el-avatar :size="38">{{ avatarText(row) }}</el-avatar>
            <div><strong>{{ row.displayName || row.username }}</strong><span>@{{ row.username }}</span></div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="角色" width="110">
        <template #default="{ row }"><el-tag effect="light" round>{{ roleName(row.role) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="账号状态" width="130">
        <template #default="{ row }">
          <el-switch
            :model-value="row.enabled"
            :loading="statusLoadingId === row.id"
            :disabled="row.id === currentUserId"
            inline-prompt
            active-text="启用"
            inactive-text="禁用"
            @change="value => handleStatusChange(row, Boolean(value))"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Key" @click="openResetDialog(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-alert
      title="当前登录账号不能被禁用，系统也会始终保留至少一个可用管理员。"
      type="info"
      :closable="false"
      show-icon
    />

    <el-dialog
      v-model="resetDialogVisible"
      title="重置用户密码"
      width="min(460px, 92vw)"
      append-to-body
      teleported
      align-center
      @closed="clearPasswordForm"
    >
      <p class="dialog-description">正在为 <strong>{{ selectedUser?.displayName || selectedUser?.username }}</strong> 设置新密码。</p>
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password autocomplete="new-password" placeholder="请输入6至72位新密码" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password autocomplete="new-password" placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetting" @click="submitPasswordReset">确认重置</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Key, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getSystemUsers, resetUserPassword, updateUserEnabled } from '@/api/users'
import type { SystemUser } from '@/api/users'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const currentUserId = computed(() => userStore.userInfo?.userId)
const users = ref<SystemUser[]>([])
const loading = ref(false)
const statusLoadingId = ref<number | null>(null)
const resetDialogVisible = ref(false)
const resetting = ref(false)
const selectedUser = ref<SystemUser | null>(null)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({ newPassword: '', confirmPassword: '' })
const passwordRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 72, message: '密码长度须为6到72位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: (_rule, value, callback) => value === passwordForm.newPassword ? callback() : callback(new Error('两次输入的密码不一致')), trigger: 'blur' },
  ],
}

async function loadUsers() {
  loading.value = true
  try {
    const response = await getSystemUsers()
    users.value = response.data
  } finally {
    loading.value = false
  }
}

async function handleStatusChange(user: SystemUser, enabled: boolean) {
  if (!enabled) {
    try {
      await ElMessageBox.confirm(`禁用后，${user.displayName || user.username} 将无法继续使用系统。`, '确认禁用用户', {
        confirmButtonText: '确认禁用',
        cancelButtonText: '取消',
        type: 'warning',
      })
    } catch {
      return
    }
  }
  statusLoadingId.value = user.id
  try {
    await updateUserEnabled(user.id, enabled)
    user.enabled = enabled
    ElMessage.success(enabled ? '用户已启用' : '用户已禁用')
  } finally {
    statusLoadingId.value = null
  }
}

function openResetDialog(user: SystemUser) {
  selectedUser.value = user
  resetDialogVisible.value = true
}

async function submitPasswordReset() {
  if (!selectedUser.value || !passwordFormRef.value) return
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  resetting.value = true
  try {
    await resetUserPassword(selectedUser.value.id, passwordForm.newPassword)
    ElMessage.success('密码已重置')
    resetDialogVisible.value = false
  } finally {
    resetting.value = false
  }
}

function clearPasswordForm() {
  selectedUser.value = null
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

function avatarText(user: SystemUser) {
  return (user.displayName || user.username).slice(0, 1).toUpperCase()
}

function roleName(role: string) {
  return role === 'ADMIN' ? '管理员' : role
}

function formatDate(value: string) {
  if (!value) return '-'
  return new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

onMounted(loadUsers)
</script>

<style scoped>
.user-management { display: grid; gap: 20px; }
.user-toolbar { display: flex; align-items: flex-start; justify-content: space-between; gap: 18px; }
.user-toolbar h3 { margin: 0 0 6px; color: var(--color-text-primary, #303133); font-size: 16px; }
.user-toolbar p, .dialog-description { margin: 0; color: var(--color-text-secondary, #909399); font-size: 13px; line-height: 1.6; }
.user-table { width: 100%; }
.user-cell { display: flex; align-items: center; gap: 11px; }
.user-cell .el-avatar { background: linear-gradient(145deg, var(--color-accent-light, #66b1ff), var(--color-accent, #409eff)); color: #fff; font-weight: 700; }
.user-cell strong, .user-cell span { display: block; }
.user-cell strong { color: var(--color-text-primary, #303133); }
.user-cell span { margin-top: 3px; color: var(--color-text-tertiary, #a8abb2); font-size: 12px; }
.dialog-description { margin-bottom: 18px; }
@media (max-width: 680px) {
  .user-toolbar { align-items: stretch; flex-direction: column; }
  .user-toolbar .el-button { align-self: flex-start; }
}
</style>
