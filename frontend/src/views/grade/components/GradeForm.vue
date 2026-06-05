<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑成绩' : '录入成绩'"
    width="600px"
    @close="$emit('close')"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="学生" prop="studentId">
        <el-select
          v-model="form.studentId"
          filterable
          remote
          :remote-method="searchStudents"
          :loading="studentLoading"
          placeholder="搜索学生"
          style="width: 100%"
        >
          <el-option v-for="s in studentOptions" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="考试名称" prop="examName">
        <el-input v-model="form.examName" placeholder="如：2024年期末数学考试" />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="考试类型" prop="examType">
            <el-select v-model="form.examType" placeholder="请选择" style="width: 100%">
              <el-option label="月考" value="MONTHLY" />
              <el-option label="期中考试" value="MIDTERM" />
              <el-option label="期末考试" value="FINAL" />
              <el-option label="联考" value="JOINT" />
              <el-option label="模拟考试" value="MOCK" />
              <el-option label="自测" value="SELF_TEST" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="科目" prop="subject">
            <el-select v-model="form.subject" placeholder="请选择" style="width: 100%">
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
        </el-col>
      </el-row>

      <el-form-item label="考试日期" prop="examDate">
        <el-date-picker
          v-model="form.examDate"
          type="date"
          placeholder="请选择日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="分数" prop="score">
            <el-input-number v-model="form.score" :min="0" :precision="1" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="满分" prop="fullScore">
            <el-input-number v-model="form.fullScore" :min="1" :precision="1" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="班级排名">
            <el-input-number v-model="form.classRank" :min="1" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="年级排名">
            <el-input-number v-model="form.gradeRank" :min="1" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="分析备注">
        <el-input v-model="form.analysis" type="textarea" :rows="3" placeholder="请输入分析备注" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="$emit('close')">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ isEdit ? '保存' : '录入' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getStudents } from '@/api/student'
import type { Student } from '@/api/student'
import type { ExamRecord, ExamRequest } from '@/api/exam'

const props = defineProps<{
  visible: boolean
  exam?: ExamRecord | null
}>()

const emit = defineEmits<{
  close: []
  submit: [data: ExamRequest]
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isEdit = ref(false)
const studentLoading = ref(false)
const studentOptions = ref<Student[]>([])

const form = reactive<ExamRequest>({
  studentId: 0,
  examName: '',
  examType: undefined,
  examDate: '',
  subject: '',
  score: 0,
  fullScore: 100,
  classRank: undefined,
  gradeRank: undefined,
  analysis: undefined,
})

const rules: FormRules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  examName: [{ required: true, message: '请输入考试名称', trigger: 'blur' }],
  examDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  subject: [{ required: true, message: '请选择科目', trigger: 'change' }],
  score: [{ required: true, message: '请输入分数', trigger: 'blur' }],
  fullScore: [{ required: true, message: '请输入满分', trigger: 'blur' }],
}

watch(() => props.visible, (val) => {
  if (val && props.exam) {
    isEdit.value = true
    Object.assign(form, {
      studentId: props.exam.studentId,
      examName: props.exam.examName,
      examType: props.exam.examType,
      examDate: props.exam.examDate,
      subject: props.exam.subject,
      score: props.exam.score,
      fullScore: props.exam.fullScore,
      classRank: props.exam.classRank,
      gradeRank: props.exam.gradeRank,
      analysis: props.exam.analysis,
    })
    if (props.exam.studentId && props.exam.studentName) {
      studentOptions.value = [{ id: props.exam.studentId, name: props.exam.studentName } as Student]
    }
  } else if (val) {
    isEdit.value = false
    Object.assign(form, {
      studentId: 0, examName: '', examType: undefined, examDate: '',
      subject: '', score: 0, fullScore: 100, classRank: undefined, gradeRank: undefined, analysis: undefined,
    })
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
