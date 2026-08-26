<template>
  <div class="dashboard" v-loading="loading">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #409EFF">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.activeStudents }}</div>
            <div class="stat-label">在读学生</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #67C23A">
            <el-icon :size="32"><Calendar /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.weekCourses }}</div>
            <div class="stat-label">本周课程</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #E6A23C">
            <el-icon :size="32"><Notebook /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingHomeworks }}</div>
            <div class="stat-label">待批作业</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #F56C6C">
            <el-icon :size="32"><Wallet /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(stats.monthRevenue) }}</div>
            <div class="stat-label">本月收入</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>今日课程</span>
              <el-tag type="primary" effect="plain">{{ stats.todayCourses }} 节</el-tag>
            </div>
          </template>
          <el-table v-if="stats.recentCourses.length" :data="stats.recentCourses" size="small">
            <el-table-column prop="startTime" label="时间" width="90" />
            <el-table-column label="学生" min-width="110">
              <template #default="{ row }">{{ row.studentName || '班级课程' }}</template>
            </el-table-column>
            <el-table-column prop="subject" label="科目" min-width="100" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="courseStatusType[row.status]" size="small">
                  {{ courseStatusMap[row.status] || row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="今天没有课程安排" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/students')">
              <span class="quick-btn-content">
                <el-icon><Plus /></el-icon><span>添加学生</span>
              </span>
            </el-button>
            <el-button type="success" @click="$router.push('/schedule')">
              <span class="quick-btn-content">
                <el-icon><Calendar /></el-icon><span>排课</span>
              </span>
            </el-button>
            <el-button type="warning" @click="$router.push('/homework')">
              <span class="quick-btn-content">
                <el-icon><EditPen /></el-icon><span>布置作业</span>
              </span>
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { User, Calendar, Notebook, Wallet, Plus, EditPen } from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/statistics'
import type { DashboardStats } from '@/api/statistics'

const loading = ref(false)
const stats = reactive<DashboardStats>({
  totalStudents: 0,
  activeStudents: 0,
  weekCourses: 0,
  todayCourses: 0,
  pendingHomeworks: 0,
  monthRevenue: 0,
  recentCourses: [],
  pendingTasks: [],
})

const courseStatusMap: Record<string, string> = {
  SCHEDULED: '已排课',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

const courseStatusType: Record<string, 'primary' | 'success' | 'info'> = {
  SCHEDULED: 'primary',
  COMPLETED: 'success',
  CANCELLED: 'info',
}

function formatMoney(amount: number): string {
  return Number(amount || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  })
}

async function fetchDashboardStats() {
  loading.value = true
  try {
    const res = await getDashboardStats()
    Object.assign(stats, res.data)
  } catch (error) {
    // handled by request interceptor
  } finally {
    loading.value = false
  }
}

onMounted(fetchDashboardStats)
</script>

<style scoped>
.stat-card {
  cursor: pointer;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.quick-actions :deep(.el-button) {
  width: 100%;
}

.quick-btn-content {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}
</style>
