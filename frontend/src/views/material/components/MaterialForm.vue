<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑资料' : (isFolder ? '新建文件夹' : '上传资料')"
    width="500px"
    @close="$emit('close')"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item :label="isFolder ? '文件夹名' : '资料标题'" prop="title">
        <el-input v-model="form.title" :placeholder="isFolder ? '请输入文件夹名' : '请输入资料标题'" />
      </el-form-item>

      <template v-if="!isFolder">
        <el-form-item label="科目">
          <el-select v-model="form.subject" placeholder="请选择科目" style="width: 100%">
            <el-option label="语文" value="语文" />
            <el-option label="数学" value="数学" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
          </el-select>
        </el-form-item>

        <el-form-item label="年级">
          <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%">
            <el-option v-for="i in 6" :key="'P'+i" :label="`小学${i}年级`" :value="`PRIMARY_${i}`" />
            <el-option v-for="i in 3" :key="'J'+i" :label="`初${i}`" :value="`JUNIOR_${i}`" />
            <el-option v-for="i in 3" :key="'S'+i" :label="`高${i}`" :value="`SENIOR_${i}`" />
          </el-select>
        </el-form-item>

        <el-form-item label="标签">
          <el-select
            v-model="form.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或输入标签"
            style="width: 100%"
          >
            <el-option label="讲义" value="讲义" />
            <el-option label="练习" value="练习" />
            <el-option label="试卷" value="试卷" />
            <el-option label="笔记" value="笔记" />
            <el-option label="参考" value="参考" />
          </el-select>
        </el-form-item>

        <el-form-item label="文件路径">
          <el-input v-model="form.filePath" placeholder="请输入文件路径或URL" />
        </el-form-item>

        <el-form-item label="文件类型">
          <el-select v-model="form.fileType" placeholder="请选择" style="width: 100%">
            <el-option label="PDF" value="pdf" />
            <el-option label="Word" value="docx" />
            <el-option label="Excel" value="xlsx" />
            <el-option label="图片" value="jpg" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
      </template>
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
import type { Material, MaterialRequest } from '@/api/material'

const props = defineProps<{
  visible: boolean
  material?: Material | null
  isFolder?: boolean
  parentId?: number | null
}>()

const emit = defineEmits<{
  close: []
  submit: [data: MaterialRequest]
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isEdit = ref(false)

const form = reactive<MaterialRequest>({
  title: '',
  filePath: undefined,
  fileType: undefined,
  subject: undefined,
  grade: undefined,
  tags: [],
  parentId: undefined,
  isFolder: false,
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
}

watch(() => props.visible, (val) => {
  if (val && props.material) {
    isEdit.value = true
    Object.assign(form, {
      title: props.material.title,
      filePath: props.material.filePath,
      fileType: props.material.fileType,
      subject: props.material.subject,
      grade: props.material.grade,
      tags: props.material.tags || [],
      parentId: props.material.parentId,
      isFolder: props.material.isFolder,
    })
  } else if (val) {
    isEdit.value = false
    Object.assign(form, {
      title: '',
      filePath: undefined,
      fileType: undefined,
      subject: undefined,
      grade: undefined,
      tags: [],
      parentId: props.parentId || undefined,
      isFolder: props.isFolder || false,
    })
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
