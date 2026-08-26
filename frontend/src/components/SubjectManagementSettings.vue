<template>
  <div class="subject-settings">
    <div class="subject-toolbar">
      <div>
        <h3>科目目录</h3>
        <p>新增或修改后，排课、学生、作业、成绩、班级和资料等页面会同步使用这里的科目。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增科目</el-button>
    </div>

    <el-alert
      title="修改科目名称会同步更新已有业务数据；已被排课或其他记录使用的科目不能删除。"
      type="info"
      :closable="false"
      show-icon
    />

    <el-table v-loading="subjectStore.loading" :data="subjectStore.subjects" class="subject-table">
      <el-table-column prop="name" label="科目名称" min-width="150">
        <template #default="{ row }">
          <span class="subject-name"><i :style="{ background: subjectColor(row.id) }" />{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="90" align="center" />
      <el-table-column label="使用情况" min-width="190">
        <template #default="{ row }">
          <el-tag v-if="row.courseCount" type="warning" effect="light">{{ row.courseCount }} 节课程</el-tag>
          <el-tag v-if="row.usageCount > row.courseCount" type="info" effect="light">
            其他 {{ row.usageCount - row.courseCount }} 条
          </el-tag>
          <span v-if="row.usageCount === 0" class="unused-text">暂未使用</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-tooltip
            :disabled="row.canDelete"
            :content="row.courseCount ? '已有排课，请先调整相关课程' : '已有业务数据引用，暂不能删除'"
            placement="top"
          >
            <span>
              <el-button link type="danger" :disabled="!row.canDelete" @click="handleDelete(row)">删除</el-button>
            </span>
          </el-tooltip>
        </template>
      </el-table-column>
      <template #empty><el-empty description="暂无科目，请先新增" /></template>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingSubject ? '编辑科目' : '新增科目'" width="440px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="科目名称" prop="name">
          <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="例如：科学" @keyup.enter="submit" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" :step="10" controls-position="right" />
          <span class="sort-hint">数值越小，科目在下拉框中越靠前</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createSubject, deleteSubject, updateSubject } from '@/api/subject'
import type { Subject } from '@/api/subject'
import { useSubjectStore } from '@/stores/subject'

const subjectStore = useSubjectStore()
const dialogVisible = ref(false)
const submitting = ref(false)
const editingSubject = ref<Subject>()
const formRef = ref<FormInstance>()
const form = reactive({ name: '', sortOrder: 10 })
const rules: FormRules = {
  name: [{ required: true, message: '请输入科目名称', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入显示顺序', trigger: 'blur' }],
}
const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#9b72e8', '#2bb7a9', '#ed6a9e', '#7f8ea3', '#3d83df']

subjectStore.fetchSubjects()

function subjectColor(id: number) {
  return colors[(id - 1) % colors.length]
}

function openCreate() {
  editingSubject.value = undefined
  form.name = ''
  form.sortOrder = subjectStore.subjects.length
    ? Math.min(Math.max(...subjectStore.subjects.map(item => item.sortOrder)) + 10, 9999)
    : 10
  dialogVisible.value = true
}

function openEdit(subject: Subject) {
  editingSubject.value = subject
  form.name = subject.name
  form.sortOrder = subject.sortOrder
  dialogVisible.value = true
}

async function submit() {
  if (!formRef.value || !(await formRef.value.validate().catch(() => false))) return
  submitting.value = true
  try {
    const payload = { name: form.name.trim(), sortOrder: form.sortOrder }
    if (editingSubject.value) {
      await updateSubject(editingSubject.value.id, payload)
      ElMessage.success('科目已更新，相关业务数据已同步')
    } else {
      await createSubject(payload)
      ElMessage.success('科目已新增')
    }
    dialogVisible.value = false
    await subjectStore.fetchSubjects(true)
  } finally {
    submitting.value = false
  }
}

async function handleDelete(subject: Subject) {
  if (!subject.canDelete) return
  await ElMessageBox.confirm(`确定删除科目“${subject.name}”吗？`, '删除科目', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })
  await deleteSubject(subject.id)
  ElMessage.success('科目已删除')
  await subjectStore.fetchSubjects(true)
}
</script>

<style scoped>
.subject-settings { display: grid; gap: 18px; }
.subject-toolbar { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; }
.subject-toolbar h3 { margin: 0 0 6px; color: var(--color-text-primary, #303133); font-size: 17px; }
.subject-toolbar p { max-width: 660px; margin: 0; color: var(--color-text-secondary, #909399); font-size: 13px; line-height: 1.6; }
.subject-table { width: 100%; }
.subject-name { display: inline-flex; align-items: center; gap: 10px; color: var(--color-text-primary, #303133); font-weight: 650; }
.subject-name i { width: 9px; height: 9px; border-radius: 50%; box-shadow: 0 0 0 4px color-mix(in srgb, currentColor 8%, transparent); }
.subject-table .el-tag + .el-tag { margin-left: 7px; }
.unused-text { color: var(--color-text-tertiary, #a8abb2); font-size: 13px; }
.sort-hint { margin-left: 12px; color: var(--color-text-tertiary, #a8abb2); font-size: 12px; }
@media (max-width: 640px) {
  .subject-toolbar { align-items: stretch; flex-direction: column; }
  .subject-toolbar .el-button { align-self: flex-start; }
}
</style>
