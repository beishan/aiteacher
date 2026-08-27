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
          <div v-if="stats.recentCourses.length" class="today-course-list" role="list">
            <article v-for="course in stats.recentCourses" :key="course.id" class="today-course-item" role="listitem">
              <div class="today-course-time">
                <span class="today-course-time-icon"><el-icon><Clock /></el-icon></span>
                <span><strong>{{ course.startTime }}</strong><small>上课时间</small></span>
              </div>
              <div class="today-course-main">
                <div class="today-course-student">
                  <el-icon><User /></el-icon>
                  <strong>{{ course.studentName || '班级课程' }}</strong>
                </div>
                <div class="today-course-subject">
                  <el-icon><Reading /></el-icon>
                  <span>{{ course.subject }}</span>
                </div>
              </div>
              <el-tag class="today-course-status" :type="courseStatusType[course.status]" effect="light" round>
                {{ courseStatusMap[course.status] || course.status }}
              </el-tag>
            </article>
          </div>
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
import { User, Calendar, Notebook, Wallet, Plus, EditPen, Clock, Reading } from '@element-plus/icons-vue'
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

.today-course-list {
  display: grid;
  gap: 12px;
  max-height: 372px;
  overflow-y: auto;
  padding: 2px;
}

.today-course-item {
  display: grid;
  min-height: 84px;
  grid-template-columns: 112px minmax(0, 1fr) auto;
  align-items: center;
  gap: 18px;
  padding: 15px 18px;
  border: 1px solid var(--color-border-light, #e4e7ed);
  border-radius: 16px;
  background: var(--color-bg-card, #fff);
  transition: transform .2s ease, border-color .2s ease, box-shadow .2s ease;
}

.today-course-item:hover {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--color-accent, #409eff) 32%, transparent);
  box-shadow: 0 9px 22px rgba(43, 66, 98, .09);
}

.today-course-time,
.today-course-student,
.today-course-subject {
  display: flex;
  align-items: center;
}

.today-course-time {
  gap: 10px;
}

.today-course-time-icon {
  display: grid;
  width: 36px;
  height: 36px;
  flex: 0 0 36px;
  place-items: center;
  border-radius: 11px;
  background: color-mix(in srgb, var(--color-accent, #409eff) 12%, transparent);
  color: var(--color-accent, #409eff);
  font-size: 18px;
}

.today-course-time > span:last-child {
  display: grid;
  gap: 2px;
}

.today-course-time strong {
  color: var(--color-text-primary, #303133);
  font-size: 16px;
  line-height: 1.1;
}

.today-course-time small {
  color: var(--color-text-tertiary, #909399);
  font-size: 10px;
  white-space: nowrap;
}

.today-course-main {
  display: grid;
  min-width: 0;
  gap: 7px;
}

.today-course-student {
  gap: 8px;
  min-width: 0;
  color: var(--color-text-primary, #303133);
}

.today-course-student .el-icon {
  color: var(--color-accent, #409eff);
}

.today-course-student strong {
  overflow: hidden;
  font-size: 15px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.today-course-subject {
  width: fit-content;
  gap: 6px;
  padding: 3px 9px;
  border-radius: 999px;
  background: var(--color-bg-hover, #f5f7fa);
  color: var(--color-text-secondary, #606266);
  font-size: 12px;
}

.today-course-status {
  min-width: 68px;
  justify-content: center;
}

:global([data-theme="macos26"]) .today-course-list {
  gap: 14px;
  padding: 3px 4px 5px;
}

:global([data-theme="macos26"]) .today-course-item {
  min-height: 92px;
  padding: 17px 20px;
  border-color: rgba(255,255,255,.82);
  border-radius: 19px;
  background: linear-gradient(145deg, rgba(255,255,255,.68), rgba(239,246,255,.38));
  box-shadow: 0 10px 26px rgba(42,64,94,.09), inset 0 1px 0 rgba(255,255,255,.92);
  backdrop-filter: blur(20px) saturate(170%);
  -webkit-backdrop-filter: blur(20px) saturate(170%);
}

:global([data-theme="macos26"]) .today-course-item:hover {
  border-color: rgba(255,255,255,.96);
  box-shadow: 0 14px 32px rgba(42,64,94,.14), inset 0 1px 0 #fff;
  transform: translateY(-2px);
}

:global([data-theme="macos26"]) .today-course-time-icon {
  border: 1px solid rgba(255,255,255,.82);
  background: linear-gradient(145deg, rgba(105,181,255,.3), rgba(0,122,255,.12));
  box-shadow: inset 0 1px 0 rgba(255,255,255,.78), 0 5px 13px rgba(0,122,255,.1);
}

:global([data-theme="macos26"]) .today-course-subject {
  border: 1px solid rgba(255,255,255,.68);
  background: rgba(255,255,255,.46);
}

:global([data-theme="macos26"]) .today-course-status {
  height: 30px;
  border-color: rgba(255,255,255,.8);
  box-shadow: inset 0 1px 0 rgba(255,255,255,.72);
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

@media (max-width: 720px) {
  .today-course-item {
    grid-template-columns: minmax(0, 1fr) auto;
    gap: 12px;
    padding: 14px;
  }

  .today-course-main {
    grid-column: 1 / -1;
    grid-row: 2;
  }
}
</style>
