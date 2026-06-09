<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑课程' : '新增课程'"
    width="600px"
    @close="$emit('close')"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      label-position="right"
    >
      <el-form-item label="课程类型" prop="courseType">
        <el-radio-group v-model="form.courseType">
          <el-radio-button value="ONE_ON_ONE">一对一</el-radio-button>
          <el-radio-button value="ONLINE">网课</el-radio-button>
          <el-radio-button value="SMALL_CLASS">小班课</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="学生" prop="studentId" v-if="form.courseType !== 'SMALL_CLASS'">
        <el-select
          v-if="defaultStudent"
          :model-value="defaultStudent.id"
          disabled
          style="width: 100%"
        >
          <el-option :label="defaultStudent.name" :value="defaultStudent.id" />
        </el-select>
        <el-select
          v-else
          v-model="form.studentId"
          filterable
          remote
          :remote-method="searchStudents"
          :loading="studentLoading"
          placeholder="请输入学生姓名搜索"
          style="width: 100%"
        >
          <el-option
            v-for="s in studentOptions"
            :key="s.id"
            :label="s.name"
            :value="s.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="科目" prop="subject">
        <el-select v-model="form.subject" placeholder="请选择科目" style="width: 100%">
          <el-option label="语文" value="语文" />
          <el-option label="数学" value="数学" />
          <el-option label="英语" value="英语" />
          <el-option label="物理" value="物理" />
          <el-option label="化学" value="化学" />
          <el-option label="生物" value="生物" />
          <el-option label="历史" value="历史" />
          <el-option label="政治" value="政治" />
          <el-option label="地理" value="地理" />
        </el-select>
      </el-form-item>

      <el-form-item label="课程标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入课程标题（可选）" />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="form.startTime"
              type="datetime"
              placeholder="请选择开始时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="form.endTime"
              type="datetime"
              placeholder="请选择结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="上课地点" prop="location">
        <el-input v-model="form.location" placeholder="请输入上课地点" />
      </el-form-item>

      <el-form-item v-if="form.courseType === 'ONLINE'" label="会议链接" prop="meetingLink">
        <el-input v-model="form.meetingLink" placeholder="请输入网课会议链接" />
      </el-form-item>

      <el-divider content-position="left">重复排课</el-divider>

      <el-form-item label="重复类型" prop="repeatType">
        <el-select v-model="form.repeatType" style="width: 100%">
          <el-option label="不重复" value="NONE" />
          <el-option label="每周重复" value="WEEKLY" />
          <el-option label="隔周重复" value="BIWEEKLY" />
          <el-option label="自定义间隔" value="CUSTOM" />
        </el-select>
      </el-form-item>

      <el-form-item v-if="form.repeatType === 'CUSTOM'" label="间隔天数" prop="repeatInterval">
        <el-input-number v-model="form.repeatInterval" :min="1" :max="90" />
      </el-form-item>

      <el-form-item v-if="form.repeatType !== 'NONE'" label="截止日期" prop="repeatEndDate">
        <el-date-picker
          v-model="form.repeatEndDate"
          type="date"
          placeholder="请选择截止日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
      </el-form-item>

      <el-form-item label="颜色标记" prop="color">
        <el-color-picker v-model="form.color" :predefine="predefineColors" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="$emit('close')">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ isEdit ? '保存' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getStudents } from '@/api/student'
import type { Student } from '@/api/student'
import type { Course, CourseRequest } from '@/api/course'

const props = defineProps<{
  visible: boolean
  course?: Course | null
  defaultTime?: { start: string; end: string } | null
  defaultStudent?: { id: number; name: string } | null
}>()

const emit = defineEmits<{
  close: []
  submit: [data: CourseRequest]
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isEdit = ref(false)
const studentLoading = ref(false)
const studentOptions = ref<Student[]>([])

const predefineColors = [
  '#409EFF', '#67C23A', '#E6A23C', '#F56C6C',
  '#909399', '#9b59b6', '#1abc9c', '#3498db',
]

const form = reactive<CourseRequest>({
  studentId: undefined,
  subject: '',
  courseType: 'ONE_ON_ONE',
  title: '',
  startTime: '',
  endTime: '',
  location: '',
  meetingLink: '',
  repeatType: 'NONE',
  repeatInterval: 7,
  repeatEndDate: undefined,
  remark: '',
  color: '#409EFF',
})

const rules: FormRules = {
  courseType: [{ required: true, message: '请选择课程类型', trigger: 'change' }],
  subject: [{ required: true, message: '请选择科目', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
}

watch(() => props.visible, (val) => {
  if (val && props.course) {
    isEdit.value = true
    Object.assign(form, {
      studentId: props.course.studentId,
      subject: props.course.subject,
      courseType: props.course.courseType,
      title: props.course.title,
      startTime: props.course.startTime,
      endTime: props.course.endTime,
      location: props.course.location,
      meetingLink: props.course.meetingLink,
      repeatType: 'NONE',
      remark: props.course.remark,
      color: props.course.color,
    })
    if (props.course.studentId && props.course.studentName) {
      studentOptions.value = [{ id: props.course.studentId, name: props.course.studentName } as Student]
    }
  } else if (val) {
    isEdit.value = false
    Object.assign(form, {
      studentId: props.defaultStudent?.id || undefined,
      subject: '',
      courseType: 'ONE_ON_ONE',
      title: '',
      startTime: props.defaultTime?.start || '',
      endTime: props.defaultTime?.end || '',
      location: '',
      meetingLink: '',
      repeatType: 'NONE',
      repeatInterval: 7,
      repeatEndDate: undefined,
      remark: '',
      color: '#409EFF',
    })
    if (props.defaultStudent) {
      studentOptions.value = [props.defaultStudent as Student]
    }
  }
})

async function searchStudents(query: string) {
  if (!query) return
  studentLoading.value = true
  try {
    const res = await getStudents({ name: query, size: 20 })
    studentOptions.value = res.data.records
  } catch (error) {
    // handled
  } finally {
    studentLoading.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    emit('submit', { ...form })
  } finally {
    loading.value = false
  }
}
</script>
