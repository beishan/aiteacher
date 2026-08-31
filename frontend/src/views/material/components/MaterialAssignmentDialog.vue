<template>
  <el-dialog
    :model-value="visible"
    title="分配资料"
    width="min(540px, 92vw)"
    append-to-body
    teleported
    align-center
    destroy-on-close
    :close-on-click-modal="!submitting"
    :close-on-press-escape="!submitting"
    :show-close="!submitting"
    @close="emit('close')"
  >
    <div class="assignment-sheet">
      <div class="material-mark">
        <el-icon><Document /></el-icon>
      </div>
      <div class="material-copy">
        <span>即将分配</span>
        <strong>{{ material?.title || '未选择资料' }}</strong>
        <div class="material-meta">
          <el-tag v-if="material?.subject" size="small" effect="plain">{{ material.subject }}</el-tag>
          <el-tag v-if="material?.grade" size="small" type="info" effect="plain">{{ gradeName(material.grade) }}</el-tag>
          <small>{{ fileTypeLabel }}</small>
        </div>
      </div>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="assignment-form">
      <el-form-item label="接收学生" prop="studentId">
        <el-select
          v-model="form.studentId"
          filterable
          clearable
          :loading="studentsLoading"
          placeholder="输入姓名或从在读学生中选择"
          style="width: 100%"
        >
          <el-option v-for="student in students" :key="student.id" :value="student.id" :label="student.name">
            <div class="student-option">
              <span class="student-avatar">{{ student.name.slice(0, 1) }}</span>
              <span class="student-name"><strong>{{ student.name }}</strong><small>{{ gradeName(student.grade) }}</small></span>
              <span v-if="student.subjects?.length" class="student-subjects">{{ student.subjects.slice(0, 2).join(' · ') }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>
      <p class="assignment-note"><el-icon><InfoFilled /></el-icon>分配后可在学生详情的专属资料中查看；重复分配会被系统阻止。</p>
    </el-form>

    <template #footer>
      <el-button :disabled="submitting" @click="emit('close')">取消</el-button>
      <el-button type="primary" :loading="submitting" :disabled="studentsLoading" @click="submit">
        确认分配
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { Document, InfoFilled } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getStudents } from '@/api/student'
import type { Student } from '@/api/student'
import type { Material } from '@/api/material'

const props = defineProps<{
  visible: boolean
  material: Material | null
  submitting: boolean
}>()

const emit = defineEmits<{
  close: []
  submit: [studentId: number]
}>()

const formRef = ref<FormInstance>()
const form = reactive<{ studentId?: number }>({ studentId: undefined })
const students = ref<Student[]>([])
const studentsLoading = ref(false)
const rules: FormRules = {
  studentId: [{ required: true, message: '请选择接收资料的学生', trigger: 'change' }],
}

const fileTypeLabel = computed(() => props.material?.fileType?.toUpperCase() || '资料文件')

watch(() => props.visible, async visible => {
  if (!visible) return
  form.studentId = undefined
  formRef.value?.clearValidate()
  await loadStudents()
})

async function loadStudents() {
  studentsLoading.value = true
  try {
    const response = await getStudents({ status: 'ACTIVE', page: 1, size: 500 })
    students.value = response.data.records
  } catch {
    students.value = []
  } finally {
    studentsLoading.value = false
  }
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || !form.studentId) return
  emit('submit', form.studentId)
}

function gradeName(grade?: string | null) {
  const names: Record<string, string> = {
    PRIMARY_1: '小学一年级', PRIMARY_2: '小学二年级', PRIMARY_3: '小学三年级',
    PRIMARY_4: '小学四年级', PRIMARY_5: '小学五年级', PRIMARY_6: '小学六年级',
    JUNIOR_1: '初一', JUNIOR_2: '初二', JUNIOR_3: '初三',
    SENIOR_1: '高一', SENIOR_2: '高二', SENIOR_3: '高三',
  }
  return names[grade] || grade || '未设置年级'
}
</script>

<style scoped>
.assignment-sheet { position: relative; display: flex; align-items: center; gap: 15px; overflow: hidden; padding: 17px; border: 1px solid color-mix(in srgb, var(--color-accent, #409eff) 18%, var(--color-border-light, #dcdfe6)); border-radius: 16px; background: linear-gradient(120deg, color-mix(in srgb, var(--color-accent, #409eff) 9%, var(--color-bg-card, #fff)), var(--color-bg-card, #fff) 70%); }
.assignment-sheet::after { position: absolute; top: -22px; right: -18px; width: 92px; height: 92px; border: 1px solid color-mix(in srgb, var(--color-accent, #409eff) 14%, transparent); border-radius: 50%; content: ''; }
.material-mark { display: grid; width: 48px; height: 56px; flex: 0 0 auto; place-items: center; border-radius: 8px 15px 8px 8px; background: var(--color-accent, #409eff); box-shadow: 0 10px 24px color-mix(in srgb, var(--color-accent, #409eff) 24%, transparent); color: #fff; font-size: 23px; }
.material-copy { min-width: 0; }
.material-copy > span { display: block; margin-bottom: 3px; color: var(--color-text-secondary, #909399); font-size: 11px; letter-spacing: .12em; }
.material-copy > strong { display: block; overflow: hidden; color: var(--color-text-primary, #303133); font-size: 16px; text-overflow: ellipsis; white-space: nowrap; }
.material-meta { display: flex; align-items: center; gap: 6px; margin-top: 7px; }
.material-meta small { color: var(--color-text-secondary, #909399); font-size: 11px; }
.assignment-form { margin-top: 20px; }
.student-option { display: flex; min-width: 0; align-items: center; gap: 9px; }
.student-avatar { display: grid; width: 28px; height: 28px; flex: 0 0 auto; place-items: center; border-radius: 9px; background: color-mix(in srgb, var(--color-accent, #409eff) 12%, var(--color-bg-card, #fff)); color: var(--color-accent, #409eff); font-weight: 700; }
.student-name { display: flex; min-width: 100px; flex-direction: column; line-height: 1.25; }
.student-name small, .student-subjects { color: var(--color-text-secondary, #909399); font-size: 11px; }
.student-subjects { overflow: hidden; margin-left: auto; text-overflow: ellipsis; white-space: nowrap; }
.assignment-note { display: flex; align-items: flex-start; gap: 6px; margin: -3px 0 0; color: var(--color-text-secondary, #909399); font-size: 12px; line-height: 1.55; }
.assignment-note .el-icon { margin-top: 2px; color: var(--color-accent, #409eff); }
</style>
