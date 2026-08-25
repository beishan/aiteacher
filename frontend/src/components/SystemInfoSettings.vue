<template>
  <section v-loading="loading" class="system-info">
    <div class="system-toolbar">
      <div>
        <h3>运行概览</h3>
        <p>查看当前部署版本、运行资源和相关服务连接状态。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadSystemInfo">刷新状态</el-button>
    </div>

    <template v-if="info">
      <div class="overview-card" :class="{ degraded: info.overallStatus !== 'UP' }">
        <div class="system-mark">TA</div>
        <div class="system-summary">
          <span class="eyebrow">{{ info.applicationName }}</span>
          <strong>v{{ info.version }}</strong>
          <p>已连续运行 {{ formatDuration(info.uptimeSeconds) }}</p>
        </div>
        <el-tag :type="info.overallStatus === 'UP' ? 'success' : 'warning'" size="large" round effect="light">
          <el-icon><CircleCheckFilled v-if="info.overallStatus === 'UP'" /><WarningFilled v-else /></el-icon>
          {{ info.overallStatus === 'UP' ? '系统运行正常' : '部分服务异常' }}
        </el-tag>
      </div>

      <div class="metrics-grid">
        <article class="metric-card">
          <span>当前环境</span><strong>{{ environmentName(info.environment) }}</strong><small>Spring Profile</small>
        </article>
        <article class="metric-card">
          <span>启动时间</span><strong>{{ formatDate(info.startedAt) }}</strong><small>服务器本地时间</small>
        </article>
        <article class="metric-card">
          <span>处理器</span><strong>{{ info.processors }} 核</strong><small>{{ info.operatingSystem }}</small>
        </article>
        <article class="metric-card memory-card">
          <span>JVM 内存</span><strong>{{ info.memoryUsedMb }} / {{ info.memoryMaxMb }} MB</strong>
          <el-progress :percentage="memoryPercentage" :show-text="false" :stroke-width="6" />
        </article>
      </div>

      <section class="info-section">
        <div class="section-title"><div><h3>服务状态</h3><p>状态检查不会展示连接地址或账号等敏感信息。</p></div></div>
        <div class="service-grid">
          <article v-for="service in info.services" :key="service.key" class="service-card">
            <span class="status-dot" :class="service.status.toLowerCase()" />
            <div><strong>{{ service.name }}</strong><p>{{ service.detail }}</p></div>
            <el-tag :type="service.status === 'UP' ? 'success' : 'danger'" size="small" round effect="plain">
              {{ service.status === 'UP' ? '正常' : '异常' }}
            </el-tag>
          </article>
        </div>
      </section>

      <section class="info-section">
        <div class="section-title"><div><h3>运行环境</h3><p>用于部署检查和故障排查的基础运行信息。</p></div></div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="系统版本">{{ info.version }}</el-descriptions-item>
          <el-descriptions-item label="Java 版本">Java {{ info.javaVersion }}</el-descriptions-item>
          <el-descriptions-item label="操作系统">{{ info.operatingSystem }}</el-descriptions-item>
          <el-descriptions-item label="服务器时间">{{ formatDate(info.serverTime) }}</el-descriptions-item>
        </el-descriptions>
      </section>
    </template>

    <el-empty v-else-if="!loading" description="暂时无法获取系统信息">
      <el-button type="primary" @click="loadSystemInfo">重新加载</el-button>
    </el-empty>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { CircleCheckFilled, Refresh, WarningFilled } from '@element-plus/icons-vue'
import { getSystemInfo } from '@/api/system'
import type { SystemInfo } from '@/api/system'

const info = ref<SystemInfo | null>(null)
const loading = ref(false)
const memoryPercentage = computed(() => {
  if (!info.value?.memoryMaxMb) return 0
  return Math.min(100, Math.round(info.value.memoryUsedMb / info.value.memoryMaxMb * 100))
})

async function loadSystemInfo() {
  loading.value = true
  try {
    const response = await getSystemInfo()
    info.value = response.data
  } finally {
    loading.value = false
  }
}

function formatDuration(totalSeconds: number) {
  const days = Math.floor(totalSeconds / 86400)
  const hours = Math.floor(totalSeconds % 86400 / 3600)
  const minutes = Math.floor(totalSeconds % 3600 / 60)
  if (days > 0) return `${days} 天 ${hours} 小时`
  if (hours > 0) return `${hours} 小时 ${minutes} 分钟`
  return `${Math.max(1, minutes)} 分钟`
}

function formatDate(value: string) {
  if (!value) return '-'
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit',
  }).format(new Date(value.replace(' ', 'T')))
}

function environmentName(value: string) {
  const names: Record<string, string> = { dev: '开发环境', prod: '生产环境', default: '默认环境' }
  return names[value] || value
}

onMounted(loadSystemInfo)
</script>

<style scoped>
.system-info { min-height: 400px; }
.system-toolbar, .section-title { display: flex; align-items: flex-start; justify-content: space-between; gap: 18px; }
.system-toolbar { margin-bottom: 20px; }
.system-toolbar h3, .section-title h3 { margin: 0 0 6px; color: var(--color-text-primary, #303133); font-size: 16px; }
.system-toolbar p, .section-title p { margin: 0; color: var(--color-text-secondary, #909399); font-size: 13px; line-height: 1.55; }
.overview-card { display: flex; align-items: center; gap: 16px; padding: 20px; border: 1px solid color-mix(in srgb, #25b864 25%, var(--color-border-light, #e4e7ed)); border-radius: 17px; background: linear-gradient(135deg, color-mix(in srgb, #25b864 9%, var(--color-bg-card, #fff)), var(--color-bg-card, #fff)); }
.overview-card.degraded { border-color: color-mix(in srgb, #e6a23c 30%, var(--color-border-light, #e4e7ed)); background: linear-gradient(135deg, color-mix(in srgb, #e6a23c 10%, var(--color-bg-card, #fff)), var(--color-bg-card, #fff)); }
.system-mark { display: grid; width: 54px; height: 54px; flex: 0 0 54px; place-items: center; border-radius: 15px; background: linear-gradient(145deg, #54a9ff, #3478ea); box-shadow: 0 9px 20px rgba(52,120,234,.24); color: #fff; font-size: 18px; font-weight: 800; }
.system-summary { min-width: 0; flex: 1; }
.system-summary .eyebrow { display: block; color: var(--color-text-secondary, #606266); font-size: 12px; }
.system-summary strong { display: block; margin: 3px 0; color: var(--color-text-primary, #303133); font-size: 22px; }
.system-summary p { margin: 0; color: var(--color-text-secondary, #909399); font-size: 12px; }
.overview-card .el-tag { display: inline-flex; align-items: center; gap: 5px; }
.metrics-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; margin-top: 14px; }
.metric-card { min-width: 0; padding: 15px; border: 1px solid var(--color-border-light, #e4e7ed); border-radius: 14px; background: var(--color-bg-card, #fff); }
.metric-card span, .metric-card small { display: block; color: var(--color-text-secondary, #909399); font-size: 11px; }
.metric-card strong { display: block; margin: 7px 0 5px; overflow: hidden; color: var(--color-text-primary, #303133); font-size: 14px; text-overflow: ellipsis; white-space: nowrap; }
.memory-card .el-progress { margin-top: 9px; }
.info-section { margin-top: 26px; padding-top: 24px; border-top: 1px solid var(--color-border-light, #ebeef5); }
.section-title { margin-bottom: 15px; }
.service-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
.service-card { display: flex; min-width: 0; align-items: center; gap: 11px; padding: 14px; border: 1px solid var(--color-border-light, #e4e7ed); border-radius: 13px; background: var(--color-bg-card, #fff); }
.service-card > div { min-width: 0; flex: 1; }
.service-card strong { color: var(--color-text-primary, #303133); font-size: 14px; }
.service-card p { margin: 4px 0 0; overflow: hidden; color: var(--color-text-secondary, #909399); font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.status-dot { width: 9px; height: 9px; flex: 0 0 9px; border-radius: 50%; box-shadow: 0 0 0 4px color-mix(in srgb, #25b864 13%, transparent); background: #25b864; }
.status-dot.down { box-shadow: 0 0 0 4px color-mix(in srgb, #f56c6c 13%, transparent); background: #f56c6c; }
@media (max-width: 900px) { .metrics-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 640px) {
  .system-toolbar { align-items: stretch; flex-direction: column; }
  .system-toolbar .el-button { align-self: flex-start; }
  .overview-card { align-items: flex-start; flex-wrap: wrap; }
  .overview-card .el-tag { margin-left: 70px; }
  .metrics-grid, .service-grid { grid-template-columns: 1fr; }
  :deep(.el-descriptions__body) { overflow-x: auto; }
}
</style>
