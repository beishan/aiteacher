<template>
  <el-dialog
    :model-value="visible"
    title="课程详情"
    width="550px"
    @close="$emit('close')"
  >
    <div v-if="course" class="course-detail">
      <!-- 状态提示横幅 -->
      <div v-if="course.status === 'COMPLETED'" class="status-banner status-completed">
        <el-icon><CircleCheckFilled /></el-icon>
        <span>课程已完成</span>
      </div>
      <div v-else-if="course.status === 'CANCELLED'" class="status-banner status-cancelled">
        <el-icon><CircleCloseFilled /></el-icon>
        <span>课程已取消</span>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="课程类型" :span="2">
          <el-tag :type="courseTypeTag[course.courseType]">
            {{ courseTypeMap[course.courseType] }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="学生" :span="2">
          {{ course.studentName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="科目">{{ course.subject }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag[course.status]" size="small">
            {{ statusMap[course.status] }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间" :span="2">
          {{ formatDateTime(course.startTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="结束时间" :span="2">
          {{ formatDateTime(course.endTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="course.location" label="上课地点" :span="2">
          {{ course.location }}
        </el-descriptions-item>
        <el-descriptions-item v-if="course.meetingLink" label="会议链接" :span="2">
          <el-link type="primary" :href="course.meetingLink" target="_blank">
            {{ course.meetingLink }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item v-if="course.title" label="课程标题" :span="2">
          {{ course.title }}
        </el-descriptions-item>
        <el-descriptions-item v-if="course.remark" label="备注" :span="2">
          {{ course.remark }}
        </el-descriptions-item>
        <el-descriptions-item v-if="course.recurrenceRule" label="重复规则" :span="2">
          {{ course.recurrenceRule }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 上课记录 -->
      <div v-if="records.length > 0" style="margin-top: 16px">
        <h4 style="margin-bottom: 8px">上课记录</h4>
        <el-timeline>
          <el-timeline-item
            v-for="record in records"
            :key="record.id"
            :timestamp="formatDateTime(record.createdAt)"
            placement="top"
          >
            <el-card shadow="never">
              <div><strong>出勤：</strong>{{ attendanceMap[record.attendanceStatus] || '-' }}</div>
              <div v-if="record.contentSummary"><strong>内容：</strong>{{ record.contentSummary }}</div>
              <div v-if="record.homeworkAssigned"><strong>作业：</strong>{{ record.homeworkAssigned }}</div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>

    <template #footer>
      <el-button @click="$emit('close')">关闭</el-button>
      <el-button v-if="course?.status === 'SCHEDULED'" type="success" @click="$emit('complete')">
        完成课程
      </el-button>
      <el-button v-if="course?.status === 'SCHEDULED'" type="warning" @click="$emit('cancel')">
        取消课程
      </el-button>
      <el-button type="primary" @click="$emit('edit')">编辑</el-button>
      <el-popconfirm v-if="course" title="确定删除该课程吗？" @confirm="$emit('delete', course!.id)">
        <template #reference>
          <el-button type="danger">删除</el-button>
        </template>
      </el-popconfirm>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import type { Course, CourseRecord } from '@/api/course'
import { getCourseRecords } from '@/api/course'

const props = defineProps<{
  visible: boolean
  course: Course | null
}>()

defineEmits<{
  close: []
  edit: []
  complete: []
  cancel: []
  delete: [id: number]
}>()

const records = ref<CourseRecord[]>([])

const courseTypeMap: Record<string, string> = {
  ONE_ON_ONE: '一对一',
  ONLINE: '网课',
  SMALL_CLASS: '小班课',
}

const courseTypeTag: Record<string, string> = {
  ONE_ON_ONE: '',
  ONLINE: 'warning',
  SMALL_CLASS: 'success',
}

const statusMap: Record<string, string> = {
  SCHEDULED: '已排课',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

const statusTag: Record<string, string> = {
  SCHEDULED: 'primary',
  COMPLETED: 'success',
  CANCELLED: 'info',
}

const attendanceMap: Record<string, string> = {
  PRESENT: '出勤',
  ABSENT: '缺勤',
  LATE: '迟到',
  LEAVE: '请假',
}

function formatDateTime(dateStr: string) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

watch(() => props.visible, async (val) => {
  if (val && props.course) {
    try {
      const res = await getCourseRecords(props.course.id)
      records.value = res.data
    } catch (error) {
      records.value = []
    }
  }
})
</script>

<style scoped>
.status-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  font-weight: 500;
}

.status-completed {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
  color: #67C23A;
  border: 1px solid #b3e19d;
}

.status-cancelled {
  background: linear-gradient(135deg, #f4f4f5 0%, #e9e9eb 100%);
  color: #909399;
  border: 1px solid #c8c9cc;
  text-decoration: line-through;
}
</style>
