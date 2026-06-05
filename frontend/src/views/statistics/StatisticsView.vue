<template>
  <div class="statistics-view">
    <!-- 统计卡片 -->
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #409EFF">
            <el-icon :size="28"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalStudents }}</div>
            <div class="stat-label">学生总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #67C23A">
            <el-icon :size="28"><Calendar /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ courseStats.thisMonthCourses }}</div>
            <div class="stat-label">本月课程</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #E6A23C">
            <el-icon :size="28"><Notebook /></el-icon>
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
            <el-icon :size="28"><Wallet /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(stats.monthRevenue) }}</div>
            <div class="stat-label">本月收入</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>学生状态分布</span>
          </template>
          <div class="chart-container">
            <div class="pie-chart">
              <div v-for="(item, index) in studentStatusData" :key="index" class="pie-item">
                <div class="pie-color" :style="{ background: item.color }"></div>
                <div class="pie-label">{{ item.label }}</div>
                <div class="pie-value">{{ item.value }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>作业完成情况</span>
          </template>
          <div class="chart-container">
            <div class="progress-list">
              <div class="progress-item">
                <div class="progress-label">完成率</div>
                <el-progress :percentage="homeworkStats.completionRate" :stroke-width="20" />
              </div>
              <div class="progress-item">
                <div class="progress-label">待批改</div>
                <div class="progress-value">{{ homeworkStats.submittedHomeworks }} 份</div>
              </div>
              <div class="progress-item">
                <div class="progress-label">已完成</div>
                <div class="progress-value">{{ homeworkStats.gradedHomeworks }} 份</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 收入统计 -->
    <el-card style="margin-top: 16px">
      <template #header>
        <span>收入统计</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="revenue-item">
            <div class="revenue-label">总收入</div>
            <div class="revenue-value">¥{{ formatMoney(revenueStats.totalRevenue) }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="revenue-item">
            <div class="revenue-label">本月收入</div>
            <div class="revenue-value">¥{{ formatMoney(revenueStats.monthRevenue) }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="revenue-item">
            <div class="revenue-label">环比增长</div>
            <div class="revenue-value" :class="revenueStats.growthRate >= 0 ? 'text-success' : 'text-danger'">
              {{ revenueStats.growthRate >= 0 ? '+' : '' }}{{ revenueStats.growthRate }}%
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 课时统计 -->
    <el-card style="margin-top: 16px">
      <template #header>
        <span>课时统计</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="course-item">
            <div class="course-label">总课程</div>
            <div class="course-value">{{ courseStats.totalCourses }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="course-item">
            <div class="course-label">已完成</div>
            <div class="course-value">{{ courseStats.completedCourses }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="course-item">
            <div class="course-label">已取消</div>
            <div class="course-value">{{ courseStats.cancelledCourses }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="course-item">
            <div class="course-label">本月课程</div>
            <div class="course-value">{{ courseStats.thisMonthCourses }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { User, Calendar, Notebook, Wallet } from '@element-plus/icons-vue'
import {
  getDashboardStats,
  getRevenueStats,
  getStudentStats,
  getCourseStats,
  getHomeworkStats,
} from '@/api/statistics'
import type { DashboardStats, RevenueStats, CourseStats, HomeworkStats } from '@/api/statistics'

const stats = reactive<DashboardStats>({
  totalStudents: 0,
  activeStudents: 0,
  todayCourses: 0,
  pendingHomeworks: 0,
  monthRevenue: 0,
  recentCourses: [],
  pendingTasks: [],
})

const revenueStats = reactive<RevenueStats>({
  totalRevenue: 0,
  monthRevenue: 0,
  lastMonthRevenue: 0,
  growthRate: 0,
  monthlyData: [],
})

const studentStatusData = ref([
  { label: '在读', value: 0, color: '#67C23A' },
  { label: '暂停', value: 0, color: '#E6A23C' },
  { label: '毕业', value: 0, color: '#909399' },
  { label: '流失', value: 0, color: '#F56C6C' },
])

const homeworkStats = reactive<HomeworkStats>({
  totalHomeworks: 0,
  pendingHomeworks: 0,
  submittedHomeworks: 0,
  gradedHomeworks: 0,
  completionRate: 0,
  subjectData: [],
})

const courseStats = reactive<CourseStats>({
  totalCourses: 0,
  completedCourses: 0,
  cancelledCourses: 0,
  thisMonthCourses: 0,
  weeklyData: [],
})

function formatMoney(amount: number) {
  if (!amount) return '0'
  return amount.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}

async function fetchStats() {
  try {
    const [dashboardRes, revenueRes, studentRes, courseRes, homeworkRes] = await Promise.all([
      getDashboardStats(),
      getRevenueStats(),
      getStudentStats(),
      getCourseStats(),
      getHomeworkStats(),
    ])

    Object.assign(stats, dashboardRes.data)
    Object.assign(revenueStats, revenueRes.data)
    Object.assign(courseStats, courseRes.data)
    Object.assign(homeworkStats, homeworkRes.data)

    studentStatusData.value = [
      { label: '在读', value: studentRes.data.activeStudents, color: '#67C23A' },
      { label: '暂停', value: studentRes.data.pausedStudents, color: '#E6A23C' },
      { label: '毕业', value: studentRes.data.graduatedStudents, color: '#909399' },
      { label: '流失', value: studentRes.data.lostStudents, color: '#F56C6C' },
    ]
  } catch (error) {
    // handled
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.chart-container {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pie-chart {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: center;
}

.pie-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pie-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.pie-label {
  font-size: 14px;
  color: #606266;
}

.pie-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.progress-list {
  width: 100%;
  padding: 0 20px;
}

.progress-item {
  margin-bottom: 16px;
}

.progress-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.progress-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.revenue-item,
.course-item {
  text-align: center;
  padding: 16px;
}

.revenue-label,
.course-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.revenue-value,
.course-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.text-success {
  color: #67C23A;
}

.text-danger {
  color: #F56C6C;
}
</style>
