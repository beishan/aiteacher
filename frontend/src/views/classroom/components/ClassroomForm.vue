<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑班级' : '创建班级'"
    width="500px"
    @close="$emit('close')"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="班级名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入班级名称" />
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
      <el-form-item label="描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入班级描述" />
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
import type { VirtualClass, ClassroomRequest } from '@/api/classroom'

const props = defineProps<{
  visible: boolean
  classroom?: VirtualClass | null
}>()

const emit = defineEmits<{
  close: []
  submit: [data: ClassroomRequest]
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isEdit = ref(false)

const form = reactive<ClassroomRequest>({
  name: '',
  subject: undefined,
  description: undefined,
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
}

watch(() => props.visible, (val) => {
  if (val && props.classroom) {
    isEdit.value = true
    Object.assign(form, {
      name: props.classroom.name,
      subject: props.classroom.subject,
      description: props.classroom.description,
    })
  } else if (val) {
    isEdit.value = false
    Object.assign(form, { name: '', subject: undefined, description: undefined })
  }
})

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
