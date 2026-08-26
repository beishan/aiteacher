<template>
  <div class="schedule-view">
    <el-tabs v-model="activeTab" class="schedule-tabs">
      <el-tab-pane label="日历视图" name="calendar">
        <!-- 日历操作栏 -->
        <el-card class="toolbar-card">
          <div class="toolbar">
            <div class="toolbar-left">
              <el-button type="primary" @click="showAddForm()">
                <el-icon><Plus /></el-icon>新增课程
              </el-button>
              <div class="nav-group">
                <el-button @click="navigateCalendar('prev')">
                  <el-icon><ArrowLeft /></el-icon>
                </el-button>
                <el-button @click="navigateToday">今天</el-button>
                <el-button @click="navigateCalendar('next')">
                  <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
              <span class="current-title">{{ currentTitle }}</span>
              <el-button-group style="margin-left: 16px">
                <el-button :type="viewType === 'dayGridMonth' ? 'primary' : ''" @click="changeView('dayGridMonth')">
                  月
                </el-button>
                <el-button :type="viewType === 'timeGridWeek' ? 'primary' : ''" @click="changeView('timeGridWeek')">
                  周
                </el-button>
                <el-button :type="viewType === 'timeGridDay' ? 'primary' : ''" @click="changeView('timeGridDay')">
                  日
                </el-button>
              </el-button-group>
            </div>
            <div class="toolbar-right">
              <el-select v-model="filterStudentId" placeholder="筛选学生" clearable style="width: 150px; margin-right: 8px">
                <el-option
                  v-for="s in studentOptions"
                  :key="s.id"
                  :label="s.name"
                  :value="s.id"
                />
              </el-select>
              <el-select v-model="filterSubject" placeholder="筛选科目" clearable style="width: 120px">
                <el-option v-for="subject in subjectOptions" :key="subject" :label="subject" :value="subject" />
              </el-select>
            </div>
          </div>
        </el-card>

        <el-card class="calendar-card">
          <FullCalendar ref="calendarRef" :options="calendarOptions" />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="课程列表" name="list">
        <el-card class="toolbar-card list-toolbar-card">
          <div class="list-toolbar-header">
            <div>
              <h3>课程列表</h3>
              <p>按条件筛选并管理全部课程记录</p>
            </div>
            <el-button type="primary" @click="showAddForm()">
              <el-icon><Plus /></el-icon>新增课程
            </el-button>
          </div>
          <el-form :inline="true" :model="listQuery" class="list-filters">
            <el-form-item label="课程状态">
              <el-select v-model="listQuery.status" placeholder="全部状态" clearable style="width: 130px">
                <el-option label="已排课" value="SCHEDULED" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
            </el-form-item>
            <el-form-item label="学生">
              <el-select v-model="listQuery.studentId" placeholder="全部学生" clearable filterable style="width: 150px">
                <el-option v-for="s in studentOptions" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="科目">
              <el-select v-model="listQuery.subject" placeholder="全部科目" clearable style="width: 120px">
                <el-option v-for="subject in subjectOptions" :key="subject" :label="subject" :value="subject" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序">
              <el-select v-model="listSort" style="width: 160px">
                <el-option label="上课时间从近到远" value="startTime:asc" />
                <el-option label="上课时间从远到近" value="startTime:desc" />
                <el-option label="最新创建优先" value="createdAt:desc" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleListSearch">筛选</el-button>
              <el-button @click="resetListFilters">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="course-list-card">
          <el-table :data="courseList" v-loading="listLoading" stripe>
            <el-table-column label="上课时间" min-width="180">
              <template #default="{ row }">
                <div class="course-time-range">
                  <strong>{{ formatDateTime(row.startTime) }}</strong>
                  <span>至 {{ formatDateTime(row.endTime) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="学生/班级" min-width="120">
              <template #default="{ row }">{{ row.studentName || row.className || '-' }}</template>
            </el-table-column>
            <el-table-column prop="subject" label="科目" width="90" />
            <el-table-column label="课程类型" width="100">
              <template #default="{ row }">{{ courseTypeMap[row.courseType] || row.courseType }}</template>
            </el-table-column>
            <el-table-column prop="title" label="课程标题" min-width="130" show-overflow-tooltip />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="courseStatusType[row.status]" size="small">
                  {{ courseStatusMap[row.status] || row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="location" label="地点" min-width="110" show-overflow-tooltip />
            <el-table-column label="操作" width="190" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="showCourseDetail(row)">详情</el-button>
                <el-button type="primary" link @click="showCourseEditForm(row)">编辑</el-button>
                <el-popconfirm title="确定删除该课程吗？" @confirm="handleDelete(row.id)">
                  <template #reference><el-button type="danger" link>删除</el-button></template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-if="courseTotal > 0"
            v-model:current-page="listQuery.page"
            v-model:page-size="listQuery.size"
            :total="courseTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            class="course-pagination"
            @size-change="fetchCourseList"
            @current-change="fetchCourseList"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 课程表单弹窗 -->
    <CourseForm
      :visible="formVisible"
      :course="currentCourse"
      :default-time="defaultTime"
      @close="formVisible = false"
      @submit="handleFormSubmit"
    />

    <!-- 课程详情弹窗 -->
    <CourseDetail
      :visible="detailVisible"
      :course="currentCourse"
      @close="detailVisible = false"
      @edit="handleEditFromDetail"
      @complete="handleComplete"
      @cancel="handleCancel"
      @delete="handleDelete"
    />

    <!-- 完成课程弹窗 -->
    <el-dialog
      v-model="completeVisible"
      title="完成课程"
      width="500px"
      append-to-body
      teleported
      align-center
    >
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="出勤状态">
          <el-select v-model="completeForm.attendanceStatus" style="width: 100%">
            <el-option label="出勤" value="PRESENT" />
            <el-option label="缺勤" value="ABSENT" />
            <el-option label="迟到" value="LATE" />
            <el-option label="请假" value="LEAVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="上课内容">
          <el-input v-model="completeForm.contentSummary" type="textarea" :rows="3" placeholder="请输入上课内容总结" />
        </el-form-item>
        <el-form-item label="布置作业">
          <el-input v-model="completeForm.homeworkAssigned" type="textarea" :rows="2" placeholder="请输入布置的作业" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="completeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" :loading="completeLoading" @click="handleCompleteSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref, reactive, onMounted, watch } from 'vue'
import { Plus, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import FullCalendar from '@fullcalendar/vue3'
import type { CalendarOptions, EventInput } from '@fullcalendar/core'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import { getStudents } from '@/api/student'
import type { Student } from '@/api/student'
import {
  getCalendarCourses,
  getCourses,
  createCourse,
  updateCourse,
  deleteCourse,
  completeCourse,
  updateCourseStatus,
} from '@/api/course'
import type { Course, CourseQuery, CourseRequest, CourseRecordRequest } from '@/api/course'
import CourseForm from './components/CourseForm.vue'
import CourseDetail from './components/CourseDetail.vue'

type CalendarViewType = 'dayGridMonth' | 'timeGridWeek' | 'timeGridDay'

const CALENDAR_VIEW_STORAGE_KEY = 'schedule-calendar-view'
const DEFAULT_CALENDAR_VIEW: CalendarViewType = 'timeGridWeek'
const calendarViewTypes = new Set<CalendarViewType>(['dayGridMonth', 'timeGridWeek', 'timeGridDay'])

function getSavedCalendarView(): CalendarViewType {
  const savedView = localStorage.getItem(CALENDAR_VIEW_STORAGE_KEY)
  return calendarViewTypes.has(savedView as CalendarViewType)
    ? savedView as CalendarViewType
    : DEFAULT_CALENDAR_VIEW
}

function syncCalendarView(view: string) {
  if (!calendarViewTypes.has(view as CalendarViewType)) return
  viewType.value = view as CalendarViewType
  localStorage.setItem(CALENDAR_VIEW_STORAGE_KEY, view)
}

const calendarRef = ref()
const activeTab = ref('calendar')
const viewType = ref<CalendarViewType>(getSavedCalendarView())
const currentTitle = ref('')
const formVisible = ref(false)
const detailVisible = ref(false)
const completeVisible = ref(false)
const completeLoading = ref(false)
const currentCourse = ref<Course | null>(null)
const defaultTime = ref<{ start: string; end: string } | null>(null)
const studentOptions = ref<Student[]>([])

const filterStudentId = ref<number | undefined>()
const filterSubject = ref<string | undefined>()
const subjectOptions = ['语文', '数学', '英语', '物理', '化学', '生物', '历史', '政治', '地理']

const listLoading = ref(false)
const listLoaded = ref(false)
const courseList = ref<Course[]>([])
const courseTotal = ref(0)
const listSort = ref('startTime:asc')
const listQuery = reactive<CourseQuery>({
  studentId: undefined,
  subject: undefined,
  status: undefined,
  page: 1,
  size: 20,
})

const completeForm = reactive<CourseRecordRequest>({
  attendanceStatus: 'PRESENT',
  contentSummary: '',
  homeworkAssigned: '',
  remark: '',
})

const courseTypeColors: Record<string, string> = {
  ONE_ON_ONE: '#409EFF',
  ONLINE: '#E6A23C',
  SMALL_CLASS: '#67C23A',
}

// 状态样式配置
const statusStyles: Record<string, { color: string; textColor: string; opacity: number; className: string }> = {
  SCHEDULED: { color: '', textColor: '#fff', opacity: 1, className: '' },
  COMPLETED: { color: '#67C23A', textColor: '#fff', opacity: 0.85, className: 'course-completed' },
  CANCELLED: { color: '#909399', textColor: '#fff', opacity: 0.6, className: 'course-cancelled' },
}

const statusLabels: Record<string, string> = {
  SCHEDULED: '',
  COMPLETED: '✓',
  CANCELLED: '✗',
}

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

const courseTypeMap: Record<string, string> = {
  ONE_ON_ONE: '一对一',
  ONLINE: '网课',
  SMALL_CLASS: '小班课',
}

const calendarOptions: CalendarOptions = {
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: viewType.value,
  locale: 'zh-cn',
  firstDay: 1,
  headerToolbar: false,
  allDaySlot: false,
  slotMinTime: '07:00:00',
  slotMaxTime: '22:00:00',
  slotDuration: '00:30:00',
  slotLabelInterval: '01:00:00',
  slotLabelFormat: {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  },
  height: 'auto',
  contentHeight: 900,
  events: fetchEvents,
  eventClick: handleEventClick,
  dateClick: handleDateClick,
  eventDrop: handleEventDrop,
  datesSet: (info) => {
    syncCalendarView(info.view.type)
    updateTitle()
  },
  editable: true,
  selectable: true,
  selectMirror: true,
  nowIndicator: true,
  eventDisplay: 'block',
  dayCellContent: (arg) => {
    return { html: String(arg.date.getDate()) }
  },
  // 月视图配置
  dayMaxEvents: 8,
  dayMaxEventRows: 8,
  moreLinkText: (num) => `+${num} 更多`,
  moreLinkClick: 'popover',
  fixedWeekCount: true,
  // 周视图配置
  eventMinHeight: 45,
  expandRows: true,
  stickyHeaderDates: true,
  // 通用配置
  weekNumbers: false,
  weekText: '周',
  navLinks: true,
  showNonCurrentDates: true,
  dayHeaderFormat: {
    weekday: 'short',
  },
}

async function fetchEvents(info: any, successCallback: Function) {
  try {
    const res = await getCalendarCourses({
      start: info.start.toISOString(),
      end: info.end.toISOString(),
      studentId: filterStudentId.value,
      subject: filterSubject.value,
    })

    const events: EventInput[] = res.data.map((course) => {
      const status = course.status || 'SCHEDULED'
      const style = statusStyles[status] || statusStyles.SCHEDULED
      const statusLabel = statusLabels[status] || ''
      const baseColor = course.color || courseTypeColors[course.courseType] || '#409EFF'

      return {
        id: String(course.id),
        title: `${statusLabel} ${course.studentName || ''} - ${course.subject}`.trim(),
        start: course.startTime,
        end: course.endTime,
        color: style.color || baseColor,
        textColor: style.textColor,
        opacity: style.opacity,
        classNames: style.className ? [style.className] : [],
        extendedProps: { course, status },
      }
    })

    successCallback(events)
  } catch (error) {
    successCallback([])
  }
}

function updateTitle() {
  const calendarApi = calendarRef.value?.getApi()
  if (!calendarApi) return
  const date = calendarApi.getDate()
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const view = calendarApi.view.type

  if (view === 'dayGridMonth') {
    currentTitle.value = `${year}年${month}月`
  } else if (view === 'timeGridWeek') {
    // 获取本周的起止日期
    const start = calendarApi.view.activeStart
    const end = calendarApi.view.activeEnd
    const startMonth = start.getMonth() + 1
    const startDay = start.getDate()
    const endMonth = end.getMonth() + 1
    const endDay = end.getDate() - 1
    if (startMonth === endMonth) {
      currentTitle.value = `${year}年${startMonth}月 ${startDay}日 - ${endDay}日`
    } else {
      currentTitle.value = `${startMonth}月${startDay}日 - ${endMonth}月${endDay}日`
    }
  } else if (view === 'timeGridDay') {
    const monthStr = String(month).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    currentTitle.value = `${year}年${monthStr}月${day}日 ${weekdays[date.getDay()]}`
  }
}

function changeView(view: CalendarViewType) {
  syncCalendarView(view)
  const calendarApi = calendarRef.value?.getApi()
  calendarApi?.changeView(view)
  updateTitle()
}

function navigateCalendar(action: 'prev' | 'next') {
  const calendarApi = calendarRef.value?.getApi()
  if (action === 'prev') {
    calendarApi?.prev()
  } else {
    calendarApi?.next()
  }
  updateTitle()
}

function navigateToday() {
  const calendarApi = calendarRef.value?.getApi()
  calendarApi?.today()
  updateTitle()
}

function handleEventClick(info: any) {
  currentCourse.value = info.event.extendedProps.course
  detailVisible.value = true
}

function handleDateClick(info: any) {
  const start = info.dateStr
  const endDate = new Date(info.date)
  endDate.setHours(endDate.getHours() + 2)
  const end = endDate.toISOString().replace('T', ' ').substring(0, 19)

  defaultTime.value = { start: start.replace('T', ' ').substring(0, 19), end }
  currentCourse.value = null
  formVisible.value = true
}

async function handleEventDrop(info: any) {
  const course = info.event.extendedProps.course
  const newStart = info.event.startStr
  const newEnd = info.event.endStr

  try {
    await updateCourse(course.id, {
      ...course,
      startTime: newStart,
      endTime: newEnd,
    })
    ElMessage.success('课程时间已更新')
    refreshCourseViews()
  } catch (error) {
    info.revert()
  }
}

function showAddForm() {
  currentCourse.value = null
  defaultTime.value = null
  formVisible.value = true
}

async function fetchCourseList() {
  listLoading.value = true
  try {
    const [sortBy, sortOrder] = listSort.value.split(':') as ['startTime' | 'createdAt', 'asc' | 'desc']
    const res = await getCourses({ ...listQuery, sortBy, sortOrder })
    courseList.value = res.data.records
    courseTotal.value = res.data.total
    listLoaded.value = true
  } catch (error) {
    // handled
  } finally {
    listLoading.value = false
  }
}

function handleListSearch() {
  listQuery.page = 1
  fetchCourseList()
}

function resetListFilters() {
  listQuery.studentId = undefined
  listQuery.subject = undefined
  listQuery.status = undefined
  listQuery.page = 1
  listSort.value = 'startTime:asc'
  fetchCourseList()
}

function showCourseDetail(course: Course) {
  currentCourse.value = course
  detailVisible.value = true
}

function showCourseEditForm(course: Course) {
  currentCourse.value = course
  defaultTime.value = null
  formVisible.value = true
}

function formatDateTime(value: string): string {
  if (!value) return '-'
  return value.replace('T', ' ').substring(0, 16)
}

async function handleFormSubmit(data: CourseRequest) {
  try {
    if (currentCourse.value) {
      await updateCourse(currentCourse.value.id, data)
      ElMessage.success('课程已更新')
    } else {
      const res = await createCourse(data)
      ElMessage.success(`已创建 ${res.data.length} 节课程`)
    }
    formVisible.value = false
    refreshCourseViews()
  } catch (error) {
    // handled
  }
}

function handleEditFromDetail() {
  detailVisible.value = false
  formVisible.value = true
}

function handleComplete() {
  detailVisible.value = false
  completeForm.attendanceStatus = 'PRESENT'
  completeForm.contentSummary = ''
  completeForm.homeworkAssigned = ''
  completeForm.remark = ''
  completeVisible.value = true
}

async function handleCompleteSubmit() {
  if (!currentCourse.value) return

  completeLoading.value = true
  try {
    await completeCourse(currentCourse.value.id, completeForm)
    ElMessage.success('课程已完成')
    completeVisible.value = false
    refreshCourseViews()
  } catch (error) {
    // handled
  } finally {
    completeLoading.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await deleteCourse(id)
    ElMessage.success('课程已删除')
    detailVisible.value = false
    refreshCourseViews()
  } catch (error) {
    // handled
  }
}

async function handleCancel() {
  if (!currentCourse.value) return

  try {
    await updateCourseStatus(currentCourse.value.id, 'CANCELLED')
    ElMessage.success('课程已取消')
    detailVisible.value = false
    refreshCourseViews()
  } catch (error) {
    // handled
  }
}

function refreshCalendar() {
  const calendarApi = calendarRef.value?.getApi()
  calendarApi?.refetchEvents()
}

function refreshCourseViews() {
  refreshCalendar()
  if (listLoaded.value || activeTab.value === 'list') {
    fetchCourseList()
  }
}

watch([filterStudentId, filterSubject], () => {
  refreshCalendar()
})

watch(activeTab, async (tab) => {
  if (tab === 'list' && !listLoaded.value) {
    fetchCourseList()
  } else if (tab === 'calendar') {
    await nextTick()
    calendarRef.value?.getApi()?.updateSize()
  }
})

onMounted(async () => {
  try {
    const res = await getStudents({ size: 100 })
    studentOptions.value = res.data.records
  } catch (error) {
    // handled
  }
})
</script>

<style scoped>
.schedule-tabs :deep(.el-tabs__header) {
  margin-bottom: 18px;
}

.schedule-tabs :deep(.el-tabs__item) {
  height: 44px;
  padding: 0 22px;
  font-size: 15px;
  font-weight: 600;
}

.toolbar-card :deep(.el-card__body) {
  padding: 16px 24px;
}

.calendar-card,
.course-list-card {
  margin-top: 16px;
}

.list-toolbar-card :deep(.el-card__body) {
  padding-bottom: 8px;
}

.list-toolbar-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.list-toolbar-header h3 {
  margin: 0;
  font-size: 18px;
  color: var(--color-text-primary, #303133);
}

.list-toolbar-header p {
  margin: 5px 0 0;
  font-size: 13px;
  color: var(--color-text-secondary, #909399);
}

.list-filters :deep(.el-form-item) {
  margin-bottom: 12px;
}

.course-time-range {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.course-time-range strong {
  font-weight: 600;
  color: var(--color-text-primary, #303133);
}

.course-time-range span {
  font-size: 12px;
  color: var(--color-text-secondary, #909399);
}

.course-pagination {
  justify-content: flex-end;
  margin-top: 18px;
}

@media (max-width: 760px) {
  .list-toolbar-header {
    align-items: stretch;
    flex-direction: column;
  }

  .list-toolbar-header .el-button {
    width: 100%;
  }
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
}

.nav-group {
  margin-left: 16px;
  display: flex;
  align-items: center;
}

.nav-group .el-button {
  padding: 8px 12px;
}

.current-title {
  margin-left: 16px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  min-width: 180px;
}

/* ===== 日历整体样式 ===== */
:deep(.fc) {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', -apple-system, sans-serif;
  --fc-border-color: #e4e7ed;
  --fc-today-bg-color: rgba(64, 158, 255, 0.04);
}

:deep(.fc .fc-toolbar-title) {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

:deep(.fc .fc-button) {
  border-radius: 6px;
  font-weight: 500;
  padding: 8px 16px;
  transition: all 0.2s;
}

:deep(.fc .fc-button:hover) {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.fc .fc-button-active) {
  transform: none;
}

/* ===== 时间槽样式 ===== */
:deep(.fc-timegrid-slot) {
  height: 56px;
}

:deep(.fc-timegrid-slot-label-cushion) {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

/* ===== 周视图内容区 ===== */
:deep(.fc-timegrid-body) {
  min-height: 700px;
}

:deep(.fc-timegrid-slots) {
  min-height: 700px;
}

/* ===== 时间轴线条 ===== */
:deep(.fc-timegrid-axis) {
  padding-right: 12px;
}

:deep(.fc-timegrid-slot-lane) {
  border-bottom-style: dashed;
}

/* ===== 当前时间指示器 ===== */
:deep(.fc-timegrid-now-indicator-line) {
  border-color: #409EFF;
  border-width: 2px;
}

:deep(.fc-timegrid-now-indicator-arrow) {
  border-color: #409EFF;
}

/* ===== 事件卡片样式 ===== */
:deep(.fc-event) {
  cursor: pointer;
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 13px;
  font-weight: 500;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  transition: all 0.2s ease;
  line-height: 1.4;
  min-height: 36px;
  display: flex;
  align-items: center;
}

:deep(.fc-event:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  z-index: 10 !important;
}

:deep(.fc-event:active) {
  transform: translateY(0);
}

/* 事件文字样式 */
:deep(.fc-event-title) {
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.fc-event-time) {
  font-size: 11px;
  opacity: 0.9;
  font-weight: 400;
}

/* ===== 日期格子样式 ===== */
:deep(.fc-daygrid-day) {
  transition: background-color 0.2s;
}

:deep(.fc-daygrid-day:hover) {
  background-color: rgba(64, 158, 255, 0.04);
}

:deep(.fc-day-today) {
  background-color: rgba(64, 158, 255, 0.06) !important;
}

:deep(.fc-day-today .fc-daygrid-day-number) {
  background: #409EFF;
  color: #fff;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

/* ===== 表头样式 ===== */
:deep(.fc-col-header-cell) {
  padding: 12px 0;
  font-weight: 600;
  color: #606266;
  background: #fafafa;
}

:deep(.fc-col-header-cell-cushion) {
  font-size: 14px;
}

/* ===== 周末日期颜色 ===== */
:deep(.fc-day-sat .fc-col-header-cell-cushion) {
  color: #F56C6C;
}

/* 隐藏月视图周日表头 */
:deep(.fc-dayGridMonth-view .fc-day-sun .fc-col-header-cell-cushion) {
  visibility: hidden;
}

/* ===== 滚动条美化 ===== */
:deep(.fc-scroller) {
  scrollbar-width: thin;
  scrollbar-color: #c0c4cc transparent;
}

:deep(.fc-scroller::-webkit-scrollbar) {
  width: 6px;
  height: 6px;
}

:deep(.fc-scroller::-webkit-scrollbar-thumb) {
  background: #c0c4cc;
  border-radius: 3px;
}

:deep(.fc-scroller::-webkit-scrollbar-track) {
  background: transparent;
}

/* ===== 已完成课程样式 ===== */
:deep(.course-completed) {
  border-left: 4px solid #67C23A !important;
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%) !important;
  opacity: 0.85;
}

:deep(.course-completed::after) {
  content: '✓';
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
  opacity: 0.6;
}

/* ===== 已取消课程样式 ===== */
:deep(.course-cancelled) {
  border-left: 4px solid #909399 !important;
  background: linear-gradient(135deg, #909399 0%, #a8a9ad 100%) !important;
  opacity: 0.6;
}

:deep(.course-cancelled .fc-event-title) {
  text-decoration: line-through;
  opacity: 0.8;
}

:deep(.course-cancelled::after) {
  content: '✗';
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
  opacity: 0.6;
}

/* ===== 月份视图事件 ===== */
:deep(.fc-daygrid-event) {
  border-radius: 6px;
  padding: 5px 10px;
  margin: 2px 4px;
  min-height: 32px;
  font-size: 13px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease;
}

:deep(.fc-daygrid-event:hover) {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* ===== 月份视图日期格子 ===== */
:deep(.fc-daygrid-day-frame) {
  min-height: 120px;
  padding: 6px;
}

:deep(.fc-daygrid-body td) {
  height: auto;
}

:deep(.fc-daygrid-body tr) {
  height: calc(900px / 7);
}

:deep(.fc-scrollgrid-sync-table) {
  height: 100%;
}

:deep(.fc-daygrid-body) {
  min-height: 600px;
}

:deep(.fc-daygrid-day-number) {
  font-size: 15px;
  font-weight: 600;
  color: #606266;
  padding: 8px 10px;
  margin-bottom: 6px;
  transition: all 0.2s;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.fc-daygrid-day-number:hover) {
  color: #409EFF;
  background: rgba(64, 158, 255, 0.08);
  border-radius: 50%;
}

/* ===== 月份视图事件容器 ===== */
:deep(.fc-daygrid-day-events) {
  padding: 4px 0;
  margin: 0;
}

/* ===== 月份视图今日样式增强 ===== */
:deep(.fc-day-today .fc-daygrid-day-frame) {
  background: rgba(64, 158, 255, 0.04);
  border-radius: 10px;
  border: 2px solid rgba(64, 158, 255, 0.15);
}

:deep(.fc-day-today .fc-daygrid-day-number) {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  color: #fff;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);
}

/* ===== 月份视图周末样式 ===== */
:deep(.fc-day-sat .fc-daygrid-day-number),
:deep(.fc-day-sun .fc-daygrid-day-number) {
  color: #F56C6C;
}

:deep(.fc-day-sat.fc-day-today .fc-daygrid-day-number),
:deep(.fc-day-sun.fc-day-today .fc-daygrid-day-number) {
  color: #fff;
  background: linear-gradient(135deg, #F56C6C 0%, #f89898 100%);
}

/* ===== 更多事件提示 ===== */
:deep(.fc-daygrid-more-link) {
  font-size: 12px;
  color: #409EFF;
  font-weight: 600;
  border-radius: 6px;
  padding: 5px 10px;
  margin: 2px 4px;
  background: rgba(64, 158, 255, 0.06);
  transition: all 0.2s;
}

:deep(.fc-daygrid-more-link:hover) {
  background: rgba(64, 158, 255, 0.12);
  color: #337ecc;
}

/* ===== 月份视图表头 ===== */
:deep(.fc-daygrid-day-top) {
  justify-content: flex-start;
  padding: 2px 4px;
}

/* ===== 日历整体边框 ===== */
:deep(.fc-scrollgrid) {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

:deep(.fc-scrollgrid td) {
  border-color: #f0f2f5;
}

:deep(.fc-scrollgrid th) {
  border-color: #e4e7ed;
  background: linear-gradient(180deg, #fafafa 0%, #f5f7fa 100%);
}

/* ===== 月份视图行高 ===== */
:deep(.fc-daygrid-body tr) {
  height: auto;
}

:deep(.fc-daygrid-body td) {
  vertical-align: top;
}

/* ===== 非当月日期 ===== */
:deep(.fc-daygrid-day.fc-day-other .fc-daygrid-day-number) {
  color: #c0c4cc;
  font-weight: 400;
}

:deep(.fc-daygrid-day.fc-day-other) {
  background: #fafafa;
}

/* ===== 月份视图单元格边框 ===== */
:deep(.fc-daygrid-day.fc-day) {
  border-right: 1px solid #f0f2f5;
}

:deep(.fc-daygrid-day.fc-day:last-child) {
  border-right: none;
}

/* ===== 月份视图选中效果 ===== */
:deep(.fc-daygrid-day.fc-day-selected) {
  background: rgba(64, 158, 255, 0.06);
}

:deep(.fc-daygrid-day.fc-day-selected .fc-daygrid-day-frame) {
  background: rgba(64, 158, 255, 0.08);
  border-radius: 8px;
}
</style>
