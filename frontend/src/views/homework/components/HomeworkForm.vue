<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑作业' : '布置作业'"
    width="600px"
    @close="$emit('close')"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="作业标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入作业标题" />
      </el-form-item>

      <el-form-item label="布置对象" prop="targetType">
        <el-radio-group v-model="targetType">
          <el-radio value="student">单个学生</el-radio>
          <el-radio value="class">班级</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="targetType === 'student'" label="选择学生" prop="studentId">
        <el-select
          v-model="form.studentId"
          filterable
          placeholder="搜索或选择学生"
          style="width: 100%"
          @visible-change="onStudentDropdownChange"
        >
          <el-option v-for="s in studentOptions" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>

      <el-form-item v-if="targetType === 'class'" label="选择班级" prop="classId">
        <el-select v-model="form.classId" placeholder="请选择班级" style="width: 100%">
          <el-option v-for="c in classOptions" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="科目" prop="subject">
        <el-select v-model="form.subject" placeholder="请选择科目" style="width: 100%">
          <el-option label="语文" value="语文" />
          <el-option label="数学" value="数学" />
          <el-option label="英语" value="英语" />
          <el-option label="物理" value="物理" />
          <el-option label="化学" value="化学" />
        </el-select>
      </el-form-item>

      <el-form-item label="截止日期" prop="dueDate">
        <el-date-picker
          v-model="form.dueDate"
          type="date"
          placeholder="请选择截止日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="评分方式" prop="scoreType">
        <el-select v-model="form.scoreType" placeholder="请选择评分方式" style="width: 100%">
          <el-option label="百分制" value="PERCENTAGE" />
          <el-option label="五星制" value="STAR" />
          <el-option label="等级制（优良中差）" value="RANK" />
        </el-select>
      </el-form-item>

      <el-form-item label="作业内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="5"
          placeholder="请输入作业内容描述"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="$emit('close')">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ isEdit ? '保存' : '布置' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getStudents } from '@/api/student'
import type { Student } from '@/api/student'
import { getClassrooms } from '@/api/classroom'
import type { VirtualClass } from '@/api/classroom'
import type { Homework, HomeworkRequest } from '@/api/homework'

const props = defineProps<{
  visible: boolean
  homework?: Homework | null
}>()

const emit = defineEmits<{
  close: []
  submit: [data: HomeworkRequest]
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isEdit = ref(false)
const targetType = ref('student')
const studentLoading = ref(false)
const studentOptions = ref<Student[]>([])
const classOptions = ref<VirtualClass[]>([])

const form = reactive<HomeworkRequest>({
  title: '',
  studentId: undefined,
  classId: undefined,
  subject: undefined,
  content: undefined,
  dueDate: undefined,
  scoreType: 'PERCENTAGE',
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入作业标题', trigger: 'blur' }],
}

watch(() => props.visible, (val) => {
  if (val && props.homework) {
    isEdit.value = true
    targetType.value = props.homework.classId ? 'class' : 'student'
    Object.assign(form, {
      title: props.homework.title,
      studentId: props.homework.studentId,
      classId: props.homework.classId,
      subject: props.homework.subject,
      content: props.homework.content,
      dueDate: props.homework.dueDate,
      scoreType: props.homework.scoreType,
    })
  } else if (val) {
    isEdit.value = false
    targetType.value = 'student'
    Object.assign(form, {
      title: '', studentId: undefined, classId: undefined,
      subject: undefined, content: undefined, dueDate: undefined, scoreType: 'PERCENTAGE',
    })
  }
})

async function loadStudents() {
  studentLoading.value = true
  try {
    const res = await getStudents({ size: 100 })
    studentOptions.value = res.data.records
  } catch (error) {
    // handled
  } finally {
    studentLoading.value = false
  }
}

function onStudentDropdownChange(visible: boolean) {
  if (visible && studentOptions.value.length === 0) {
    loadStudents()
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = { ...form }
    if (targetType.value === 'student') {
      data.classId = undefined
    } else {
      data.studentId = undefined
    }
    emit('submit', data)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const res = await getClassrooms()
    classOptions.value = res.data
  } catch (error) {
    // handled
  }
  loadStudents()
})
</script>
