<template>
  <section class="user-management">
    <div class="user-toolbar">
      <div>
        <h3>账号列表</h3>
        <p>停用账号后，该用户已有的登录状态会立即失效。</p>
      </div>
      <div class="toolbar-actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadUsers">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="createDialogVisible = true">新建用户</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="users" class="user-table" empty-text="暂无系统用户">
      <el-table-column label="用户" min-width="180">
        <template #default="{ row }">
          <div class="user-cell">
            <el-avatar :size="38">{{ avatarText(row as SystemUser) }}</el-avatar>
            <div><strong>{{ row.displayName || row.username }}</strong><span>@{{ row.username }}</span></div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="角色" width="145">
        <template #default="{ row }">
          <el-select
            :model-value="row.role"
            :loading="roleLoadingId === row.id"
            :disabled="row.id === currentUserId"
            aria-label="用户角色"
            @change="value => handleRoleChange(row as SystemUser, value as UserRole)"
          >
            <el-option v-for="role in roleOptions" :key="role.value" :label="role.label" :value="role.value" />
          </el-select>
        </template>
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
            @change="value => handleStatusChange(row as SystemUser, Boolean(value))"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Key" @click="openResetDialog(row as SystemUser)">重置密码</el-button>
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
      v-model="createDialogVisible"
      title="新建系统用户"
      width="min(500px, 92vw)"
      append-to-body
      teleported
      align-center
      @closed="clearCreateForm"
    >
      <div class="create-intro">
        <span class="create-intro__icon"><UserFilled /></span>
        <div><strong>创建系统账号</strong><p>选择适合的权限角色，新账号创建后会立即启用。</p></div>
      </div>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-position="top">
        <div class="form-grid">
          <el-form-item label="登录用户名" prop="username">
            <el-input v-model="createForm.username" maxlength="50" autocomplete="off" placeholder="例如：teacher.li" />
          </el-form-item>
          <el-form-item label="显示名称" prop="displayName">
            <el-input v-model="createForm.displayName" maxlength="50" autocomplete="off" placeholder="例如：李老师" />
          </el-form-item>
        </div>
        <el-form-item label="账号角色" prop="role">
          <div class="role-options">
            <button
              v-for="role in roleOptions"
              :key="role.value"
              type="button"
              class="role-option"
              :class="{ active: createForm.role === role.value }"
              @click="createForm.role = role.value"
            >
              <span><strong>{{ role.label }}</strong><el-icon v-if="createForm.role === role.value"><Check /></el-icon></span>
              <small>{{ role.description }}</small>
            </button>
          </div>
        </el-form-item>
        <el-form-item label="初始密码" prop="password">
          <el-input v-model="createForm.password" type="password" show-password autocomplete="new-password" placeholder="请输入6至72位初始密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="createForm.confirmPassword" type="password" show-password autocomplete="new-password" placeholder="请再次输入初始密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreateUser">创建用户</el-button>
      </template>
    </el-dialog>

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
import { Check, Key, Plus, Refresh, UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createSystemUser, getSystemUsers, resetUserPassword, updateUserEnabled, updateUserRole } from '@/api/users'
import type { SystemUser } from '@/api/users'
import { useUserStore } from '@/stores/user'
import { roleOptions } from '@/config/permissions'
import type { UserRole } from '@/config/permissions'

const userStore = useUserStore()
const currentUserId = computed(() => userStore.userInfo?.userId)
const users = ref<SystemUser[]>([])
const loading = ref(false)
const statusLoadingId = ref<number | null>(null)
const roleLoadingId = ref<number | null>(null)
const createDialogVisible = ref(false)
const creating = ref(false)
const resetDialogVisible = ref(false)
const resetting = ref(false)
const selectedUser = ref<SystemUser | null>(null)
const createFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const createForm = reactive<{ username: string; displayName: string; password: string; confirmPassword: string; role: UserRole }>({
  username: '', displayName: '', password: '', confirmPassword: '', role: 'TEACHER',
})
const passwordForm = reactive({ newPassword: '', confirmPassword: '' })
const createRules: FormRules = {
  username: [
    { required: true, message: '请输入登录用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度须为3到50位', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9._-]+$/, message: '只能包含字母、数字、点、下划线和连字符', trigger: 'blur' },
  ],
  displayName: [
    { required: true, message: '请输入显示名称', trigger: 'blur' },
    { max: 50, message: '显示名称不能超过50个字符', trigger: 'blur' },
  ],
  role: [{ required: true, message: '请选择账号角色', trigger: 'change' }],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, max: 72, message: '密码长度须为6到72位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入初始密码', trigger: 'blur' },
    { validator: (_rule, value, callback) => value === createForm.password ? callback() : callback(new Error('两次输入的密码不一致')), trigger: 'blur' },
  ],
}
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

async function submitCreateUser() {
  if (!createFormRef.value) return
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return
  creating.value = true
  try {
    const response = await createSystemUser({
      username: createForm.username.trim(),
      displayName: createForm.displayName.trim(),
      password: createForm.password,
      role: createForm.role,
    })
    users.value.push(response.data)
    ElMessage.success('用户创建成功')
    createDialogVisible.value = false
  } finally {
    creating.value = false
  }
}

async function handleRoleChange(user: SystemUser, role: UserRole) {
  if (role === user.role) return
  const previousRole = user.role
  roleLoadingId.value = user.id
  try {
    await updateUserRole(user.id, role)
    user.role = role
    ElMessage.success('用户角色已更新')
  } catch {
    user.role = previousRole
  } finally {
    roleLoadingId.value = null
  }
}

function clearCreateForm() {
  createForm.username = ''
  createForm.displayName = ''
  createForm.password = ''
  createForm.confirmPassword = ''
  createForm.role = 'TEACHER'
  createFormRef.value?.clearValidate()
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

function formatDate(value: string) {
  if (!value) return '-'
  return new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

onMounted(loadUsers)
</script>

<style scoped>
.user-management { display: grid; gap: 20px; }
.user-toolbar { display: flex; align-items: flex-start; justify-content: space-between; gap: 18px; }
.toolbar-actions { display: flex; align-items: center; gap: 10px; }
.user-toolbar h3 { margin: 0 0 6px; color: var(--color-text-primary, #303133); font-size: 16px; }
.user-toolbar p, .dialog-description { margin: 0; color: var(--color-text-secondary, #909399); font-size: 13px; line-height: 1.6; }
.user-table { width: 100%; }
.user-cell { display: flex; align-items: center; gap: 11px; }
.user-cell .el-avatar { background: linear-gradient(145deg, var(--color-accent-light, #66b1ff), var(--color-accent, #409eff)); color: #fff; font-weight: 700; }
.user-cell strong, .user-cell span { display: block; }
.user-cell strong { color: var(--color-text-primary, #303133); }
.user-cell span { margin-top: 3px; color: var(--color-text-tertiary, #a8abb2); font-size: 12px; }
.dialog-description { margin-bottom: 18px; }
.create-intro { display: flex; align-items: center; gap: 14px; margin: -4px 0 20px; padding: 14px 16px; border: 1px solid color-mix(in srgb, var(--color-accent, #409eff) 20%, transparent); border-radius: 12px; background: color-mix(in srgb, var(--color-accent, #409eff) 7%, transparent); }
.create-intro__icon { display: grid; flex: 0 0 auto; width: 38px; height: 38px; place-items: center; border-radius: 11px; background: linear-gradient(145deg, var(--color-accent-light, #66b1ff), var(--color-accent, #409eff)); box-shadow: 0 8px 18px color-mix(in srgb, var(--color-accent, #409eff) 25%, transparent); color: #fff; font-size: 19px; }
.create-intro strong { color: var(--color-text-primary, #303133); font-size: 14px; }
.create-intro p { margin: 3px 0 0; color: var(--color-text-secondary, #909399); font-size: 12px; line-height: 1.5; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.role-options { display: grid; width: 100%; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.role-option { min-width: 0; padding: 12px; border: 1px solid var(--color-border-light, #dcdfe6); border-radius: 12px; background: var(--color-bg-card, #fff); color: var(--color-text-primary, #303133); cursor: pointer; text-align: left; transition: border-color .18s ease, background .18s ease, box-shadow .18s ease, transform .18s ease; }
.role-option:hover { border-color: color-mix(in srgb, var(--color-accent, #409eff) 44%, transparent); transform: translateY(-1px); }
.role-option.active { border-color: var(--color-accent, #409eff); background: color-mix(in srgb, var(--color-accent, #409eff) 8%, var(--color-bg-card, #fff)); box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-accent, #409eff) 12%, transparent); }
.role-option span { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.role-option small { display: block; margin-top: 5px; color: var(--color-text-secondary, #909399); font-size: 11px; line-height: 1.45; }
@media (max-width: 680px) {
  .user-toolbar { align-items: stretch; flex-direction: column; }
  .toolbar-actions { justify-content: flex-end; }
  .form-grid { grid-template-columns: 1fr; gap: 0; }
  .role-options { grid-template-columns: 1fr; }
}
</style>
