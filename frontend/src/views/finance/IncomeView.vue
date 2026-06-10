<template>
  <div class="income-view">
    <!-- 统计卡片 -->
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%)">
            <el-icon :size="28"><Wallet /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(summary.totalIncome) }}</div>
            <div class="stat-label">总收入</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%)">
            <el-icon :size="28"><Coin /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(summary.monthIncome) }}</div>
            <div class="stat-label">本月收入</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #E6A23C 0%, #ebb563 100%)">
            <el-icon :size="28"><Coin /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(summary.lastMonthIncome) }}</div>
            <div class="stat-label">上月收入</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" :style="{ background: summary.growthRate >= 0 ? 'linear-gradient(135deg, #67C23A 0%, #85ce61 100%)' : 'linear-gradient(135deg, #F56C6C 0%, #f89898 100%)' }">
            <el-icon :size="28"><Top v-if="summary.growthRate >= 0" /><Bottom v-else /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value" :class="summary.growthRate >= 0 ? 'text-success' : 'text-danger'">
              {{ summary.growthRate >= 0 ? '+' : '' }}{{ summary.growthRate }}%
            </div>
            <div class="stat-label">环比增长</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 月度趋势图 -->
    <el-card style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>月度收入趋势</span>
        </div>
      </template>
      <div class="trend-chart">
        <div class="chart-bars">
          <div v-for="item in trendData" :key="item.month" class="bar-group">
            <div class="bar-wrapper">
              <div
                class="bar bar-income"
                :style="{ height: getBarHeight(item.income) + 'px' }"
                :title="'收入: ¥' + formatMoney(item.income)"
              >
                <span v-if="item.income > 0" class="bar-value">¥{{ formatCompact(item.income) }}</span>
              </div>
              <div
                v-if="item.refund > 0"
                class="bar bar-refund"
                :style="{ height: getBarHeight(item.refund) + 'px' }"
                :title="'退款: ¥' + formatMoney(item.refund)"
              >
                <span class="bar-value">¥{{ formatCompact(item.refund) }}</span>
              </div>
            </div>
            <div class="bar-label">{{ formatMonth(item.month) }}</div>
          </div>
        </div>
        <div class="chart-legend">
          <span class="legend-item"><span class="legend-color" style="background: #409EFF"></span>收入</span>
          <span class="legend-item"><span class="legend-color" style="background: #F56C6C"></span>退款</span>
        </div>
      </div>
    </el-card>

    <!-- 搜索筛选栏 -->
    <el-card style="margin-top: 16px">
      <el-form :inline="true" :model="query">
        <el-form-item label="学生">
          <el-select v-model="query.studentId" placeholder="全部学生" clearable filterable style="width: 160px">
            <el-option
              v-for="s in students"
              :key="s.id"
              :label="s.name"
              :value="s.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.paymentType" placeholder="全部类型" clearable style="width: 120px">
            <el-option label="收入" value="INCOME" />
            <el-option label="退款" value="REFUND" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="query.paymentMethod" placeholder="全部方式" clearable style="width: 120px">
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="银行转账" value="BANK" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card style="margin-top: 16px">
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="studentName" label="学生" width="100" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            <span :class="row.paymentType === 'INCOME' ? 'text-success' : 'text-danger'" style="font-weight: 600">
              {{ row.paymentType === 'INCOME' ? '+' : '-' }}¥{{ formatMoney(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.paymentType === 'INCOME' ? 'success' : 'danger'" size="small">
              {{ row.paymentType === 'INCOME' ? '收入' : '退款' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100">
          <template #default="{ row }">
            {{ paymentMethodMap[row.paymentMethod] || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentDate" label="日期" width="110" />
        <el-table-column prop="note" label="备注" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.note || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
      </el-table>

      <el-pagination
        v-if="total > 0"
        style="margin-top: 16px; justify-content: flex-end"
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSearch"
        @current-change="fetchRecords"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Wallet, Coin, Top, Bottom, Search } from '@element-plus/icons-vue'
import { getFinanceRecords, getFinanceSummary, getFinanceTrend } from '@/api/finance'
import { getStudents } from '@/api/student'
import type { FinanceRecord, FinanceRecordQuery, FinanceSummary, MonthlyTrend } from '@/api/finance'
import type { Student } from '@/api/student'

const loading = ref(false)
const records = ref<FinanceRecord[]>([])
const total = ref(0)
const students = ref<Student[]>([])
const dateRange = ref<string[]>([])

const summary = reactive<FinanceSummary>({
  totalIncome: 0,
  monthIncome: 0,
  lastMonthIncome: 0,
  growthRate: 0,
  totalRefund: 0,
  recordCount: 0,
})

const trendData = ref<MonthlyTrend[]>([])

const query = reactive<FinanceRecordQuery>({
  studentId: undefined,
  paymentType: undefined,
  paymentMethod: undefined,
  startDate: undefined,
  endDate: undefined,
  page: 1,
  size: 20,
})

const paymentMethodMap: Record<string, string> = {
  CASH: '现金',
  WECHAT: '微信',
  ALIPAY: '支付宝',
  BANK: '银行转账',
}

// 趋势图最大值，用于计算柱子高度
const maxTrendValue = computed(() => {
  const maxIncome = Math.max(...trendData.value.map(d => d.income), 0)
  const maxRefund = Math.max(...trendData.value.map(d => d.refund), 0)
  return Math.max(maxIncome, maxRefund, 1)
})

function getBarHeight(value: number): number {
  const maxHeight = 160
  return Math.max((value / maxTrendValue.value) * maxHeight, value > 0 ? 4 : 0)
}

function formatMoney(amount: number): string {
  if (!amount) return '0'
  return amount.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}

function formatCompact(value: number): string {
  if (value >= 10000) {
    return (value / 10000).toFixed(1) + 'w'
  }
  if (value >= 1000) {
    return (value / 1000).toFixed(1) + 'k'
  }
  return String(Math.round(value))
}

function formatMonth(month: string): string {
  // "2026-01" -> "1月"
  const parts = month.split('-')
  return parseInt(parts[1]) + '月'
}

async function fetchRecords() {
  loading.value = true
  try {
    // 同步日期范围到查询参数
    if (dateRange.value && dateRange.value.length === 2) {
      query.startDate = dateRange.value[0]
      query.endDate = dateRange.value[1]
    } else {
      query.startDate = undefined
      query.endDate = undefined
    }

    const res = await getFinanceRecords(query)
    records.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    // handled
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchRecords()
}

async function fetchSummary() {
  try {
    const res = await getFinanceSummary()
    Object.assign(summary, res.data)
  } catch (error) {
    // handled
  }
}

async function fetchTrend() {
  try {
    const res = await getFinanceTrend(12)
    trendData.value = res.data
  } catch (error) {
    // handled
  }
}

async function fetchStudents() {
  try {
    const res = await getStudents({ size: 100 })
    students.value = res.data.records
  } catch (error) {
    // handled
  }
}

onMounted(() => {
  fetchSummary()
  fetchTrend()
  fetchRecords()
  fetchStudents()
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
  flex-shrink: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.text-success {
  color: #67C23A;
}

.text-danger {
  color: #F56C6C;
}

/* 趋势图 */
.trend-chart {
  padding: 8px 0;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 200px;
  padding: 0 8px;
  gap: 4px;
}

.bar-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 80px;
}

.bar-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  height: 170px;
}

.bar {
  width: 28px;
  min-height: 0;
  border-radius: 4px 4px 0 0;
  transition: all 0.3s ease;
  position: relative;
  cursor: pointer;
}

.bar:hover {
  opacity: 0.85;
  transform: scaleY(1.02);
  transform-origin: bottom;
}

.bar-income {
  background: linear-gradient(180deg, #409EFF 0%, #66b1ff 100%);
}

.bar-refund {
  background: linear-gradient(180deg, #F56C6C 0%, #f89898 100%);
}

.bar-value {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 10px;
  color: #606266;
  white-space: nowrap;
  font-weight: 500;
}

.bar-label {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}

.chart-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
