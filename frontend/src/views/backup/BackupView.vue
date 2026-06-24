<template>
  <div class="backup-view">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <el-icon :size="32" color="#409eff"><FolderOpened /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalCount }}</div>
              <div class="stat-label">备份总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <el-icon :size="32" color="#67c23a"><CircleCheck /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.successCount }}</div>
              <div class="stat-label">成功备份</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <el-icon :size="32" color="#e6a23c"><Clock /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.lastBackupTime || '暂无' }}</div>
              <div class="stat-label">上次备份</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <el-icon :size="32" :color="autoConfig.enabled ? '#67c23a' : '#909399'">
              <Timer />
            </el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ autoConfig.enabled ? '已开启' : '未开启' }}</div>
              <div class="stat-label">自动备份</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作区 -->
    <el-card class="action-card" shadow="hover">
      <div class="action-bar">
        <div class="action-left">
          <el-button type="primary" @click="handleCreateBackup" :loading="creating">
            <el-icon><Plus /></el-icon>
            立即备份
          </el-button>
          <el-button @click="loadBackups">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        <div class="action-right">
          <el-select v-model="filter.type" placeholder="备份类型" clearable style="width: 130px" @change="loadBackups">
            <el-option label="手动备份" value="MANUAL" />
            <el-option label="自动备份" value="AUTO" />
          </el-select>
          <el-select v-model="filter.status" placeholder="状态" clearable style="width: 130px; margin-left: 8px" @change="loadBackups">
            <el-option label="完成" value="COMPLETED" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </div>
      </div>
    </el-card>

    <!-- 备份记录表格 -->
    <el-card shadow="hover" class="table-card">
      <el-table :data="backups" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="fileName" label="文件名" min-width="220">
          <template #default="{ row }">
            <div class="file-name-cell">
              <el-icon color="#409eff"><Document /></el-icon>
              <span>{{ row.fileName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="backupType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.backupType === 'AUTO' ? 'success' : 'primary'" size="small">
              {{ row.backupType === 'AUTO' ? '自动' : '手动' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'COMPLETED' ? 'success' : row.status === 'FAILED' ? 'danger' : 'warning'"
              size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="大小" width="100">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="durationMs" label="耗时" width="90">
          <template #default="{ row }">
            {{ row.durationMs ? (row.durationMs / 1000).toFixed(1) + 's' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'COMPLETED'"
              type="primary"
              size="small"
              link
              @click="handleDownload(row)">
              <el-icon><Download /></el-icon> 下载
            </el-button>
            <el-button
              v-if="row.status === 'COMPLETED'"
              type="warning"
              size="small"
              link
              @click="handleRestore(row)">
              <el-icon><RefreshLeft /></el-icon> 恢复
            </el-button>
            <el-popconfirm
              title="确定删除此备份记录？"
              @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" size="small" link>
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadBackups"
          @current-change="loadBackups"
        />
      </div>
    </el-card>

    <!-- 自动备份配置 -->
    <el-card shadow="hover" class="config-card">
      <template #header>
        <div class="config-header">
          <span>自动备份配置</span>
          <el-button type="primary" size="small" @click="handleSaveAutoConfig" :loading="savingConfig">
            保存配置
          </el-button>
        </div>
      </template>
      <el-form :model="autoConfigForm" label-width="140px">
        <el-form-item label="启用自动备份">
          <el-switch v-model="autoConfigForm.enabled" />
        </el-form-item>
        <el-form-item label="备份时间 (Cron)">
          <el-input v-model="autoConfigForm.cron" placeholder="0 0 2 * * ?" style="width: 300px">
            <template #append>
              <el-tooltip content="默认每天凌晨 2 点执行备份" placement="top">
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="保留备份数量">
          <el-input-number v-model="autoConfigForm.maxKeep" :min="1" :max="30" />
          <span class="form-hint">超出数量的旧自动备份将被自动清理</span>
        </el-form-item>
        <el-form-item label="备份存储目录">
          <el-input v-model="autoConfigForm.backupDir" placeholder="/opt/tutor-assist/backups" style="width: 400px" disabled />
          <span class="form-hint">由后端配置决定</span>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  FolderOpened, CircleCheck, Clock, Timer, Plus, Refresh,
  Document, Download, RefreshLeft, Delete, QuestionFilled
} from '@element-plus/icons-vue'
const TimerOff = Timer  // 复用 Timer 图标
import {
  listBackups, createBackup, deleteBackup, restoreBackup,
  getAutoBackupConfig, getBackupDownloadUrl,
  type BackupRecord, type AutoBackupConfig
} from '@/api/backup'
import { getSettings, updateSettings } from '@/api/settings'

// ==================== 状态 ====================
const loading = ref(false)
const creating = ref(false)
const savingConfig = ref(false)
const backups = ref<BackupRecord[]>([])
const total = ref(0)

const pagination = reactive({ page: 1, size: 20 })
const filter = reactive({ type: '', status: '' })

const autoConfig = reactive<AutoBackupConfig>({
  enabled: false,
  maxKeep: 5,
  backupDir: '/opt/tutor-assist/backups'
})

const autoConfigForm = reactive({
  enabled: false,
  cron: '0 0 2 * * ?',
  maxKeep: 5,
  backupDir: '/opt/tutor-assist/backups'
})

// ==================== 统计 ====================
const stats = computed(() => {
  const list = backups.value
  const completed = list.filter(b => b.status === 'COMPLETED')
  const lastBackup = list.length > 0 ? list[0].createdAt?.substring(0, 16) : null
  return {
    totalCount: total.value,
    successCount: completed.length,
    lastBackupTime: lastBackup
  }
})

// ==================== 数据加载 ====================
async function loadBackups() {
  loading.value = true
  try {
    const res = await listBackups({
      backupType: filter.type || undefined,
      status: filter.status || undefined,
      page: pagination.page,
      size: pagination.size
    })
    backups.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

async function loadAutoConfig() {
  try {
    const res = await getAutoBackupConfig()
    const config = res.data
    autoConfig.enabled = config.enabled
    autoConfig.maxKeep = config.maxKeep
    autoConfig.backupDir = config.backupDir
    autoConfigForm.enabled = config.enabled
    autoConfigForm.maxKeep = config.maxKeep
    autoConfigForm.backupDir = config.backupDir

    // 从系统设置获取 cron 表达式
    try {
      const settingsRes = await getSettings()
      const settings = settingsRes.data || []
      const cronSetting = settings.find((s: any) => s.key === 'backup.auto.cron')
      if (cronSetting) {
        autoConfigForm.cron = cronSetting.value
      }
    } catch {}
  } catch {}
}

// ==================== 操作 ====================
async function handleCreateBackup() {
  creating.value = true
  try {
    await ElMessageBox.confirm('确定要立即创建数据库备份吗？', '创建备份', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    const res = await createBackup('手动备份')
    if (res.data.status === 'COMPLETED') {
      ElMessage.success('备份创建成功')
    } else {
      ElMessage.warning('备份创建失败: ' + (res.data.errorMessage || '未知错误'))
    }
    loadBackups()
  } catch (e: any) {
    if (e !== 'cancel') {
      // 错误已由拦截器处理
    }
  } finally {
    creating.value = false
  }
}

function handleDownload(row: BackupRecord) {
  const token = localStorage.getItem('token')
  const url = getBackupDownloadUrl(row.id)
  // 使用 fetch 下载以携带 token
  fetch(url, {
    headers: { Authorization: `Bearer ${token}` }
  })
    .then(response => {
      if (!response.ok) throw new Error('下载失败')
      return response.blob()
    })
    .then(blob => {
      const downloadUrl = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = downloadUrl
      a.download = row.fileName
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(downloadUrl)
      document.body.removeChild(a)
      ElMessage.success('下载开始')
    })
    .catch(() => {
      ElMessage.error('下载失败')
    })
}

async function handleRestore(row: BackupRecord) {
  try {
    await ElMessageBox.confirm(
      `确定要从备份 "${row.fileName}" 恢复数据库吗？\n此操作将覆盖当前数据库中的所有数据，请谨慎操作！`,
      '恢复数据库',
      {
        confirmButtonText: '确定恢复',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    const res = await restoreBackup(row.id)
    ElMessage.success('数据库已恢复')
    loadBackups()
  } catch (e: any) {
    if (e !== 'cancel') {
      // 错误已由拦截器处理
    }
  }
}

async function handleDelete(row: BackupRecord) {
  try {
    await deleteBackup(row.id)
    ElMessage.success('已删除')
    loadBackups()
  } catch {}
}

async function handleSaveAutoConfig() {
  savingConfig.value = true
  try {
    await updateSettings({
      'backup.auto.enabled': autoConfigForm.enabled ? 'true' : 'false',
      'backup.auto.cron': autoConfigForm.cron,
      'backup.auto.max_keep': String(autoConfigForm.maxKeep)
    })
    autoConfig.enabled = autoConfigForm.enabled
    autoConfig.maxKeep = autoConfigForm.maxKeep
    ElMessage.success('配置已保存')
  } catch {} finally {
    savingConfig.value = false
  }
}

// ==================== 工具函数 ====================
function statusText(status: string): string {
  switch (status) {
    case 'COMPLETED': return '完成'
    case 'FAILED': return '失败'
    case 'IN_PROGRESS': return '进行中'
    default: return status
  }
}

function formatFileSize(bytes: number): string {
  if (!bytes || bytes === 0) return '-'
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return size.toFixed(1) + ' ' + units[unitIndex]
}

// ==================== 初始化 ====================
onMounted(() => {
  loadBackups()
  loadAutoConfig()
})
</script>

<style scoped>
.backup-view {
  padding: 16px;
}

.stat-cards {
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.action-card {
  margin-bottom: 16px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-left {
  display: flex;
  gap: 8px;
}

.action-right {
  display: flex;
  align-items: center;
}

.table-card {
  margin-bottom: 16px;
}

.file-name-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.config-card {
  margin-bottom: 16px;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-hint {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
