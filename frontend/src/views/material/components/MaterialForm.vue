<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑资料' : (isFolder ? '新建文件夹' : '上传资料')"
    width="560px"
    @close="$emit('close')"
  >
    <!-- 文件夹模式：文件夹名 + 颜色 -->
    <el-form v-if="isFolder" ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="文件夹名" prop="title">
        <el-input v-model="form.title" placeholder="请输入文件夹名" />
      </el-form-item>
      <el-form-item label="颜色标识">
        <div class="color-picker">
          <div
            v-for="c in folderColors"
            :key="c.value"
            class="color-item"
            :class="{ active: form.color === c.value }"
            :style="{ backgroundColor: c.value }"
            @click="form.color = form.color === c.value ? undefined : c.value"
            :title="c.label"
          >
            <el-icon v-if="form.color === c.value" :size="14" color="#fff"><Check /></el-icon>
          </div>
        </div>
      </el-form-item>
    </el-form>

    <!-- 资料模式：拖拽上传 + 基本信息 -->
    <template v-else>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <!-- 拖拽上传区域 -->
        <el-form-item label="上传文件" prop="filePath" :rules="[{ required: !isEdit, message: '请上传文件', trigger: 'change' }]">
          <el-upload
            ref="uploadRef"
            class="file-upload-area"
            drag
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="fileList"
            accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.jpg,.jpeg,.png,.gif,.bmp,.txt,.zip,.rar"
          >
            <div v-if="!form.filePath" class="upload-placeholder">
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
              <div class="upload-text">将文件拖到此处，或<em>点击上传</em></div>
              <div class="upload-hint">支持 PDF、Word、Excel、PPT、图片、压缩包等</div>
            </div>
            <div v-else class="upload-file-info">
              <el-icon :size="32" :color="fileIconColor"><Document /></el-icon>
              <div class="file-detail">
                <div class="file-name">{{ uploadedFileName }}</div>
                <div class="file-size">{{ formatFileSize(uploadedFileSize) }}</div>
              </div>
              <el-icon class="re-upload-icon"><RefreshRight /></el-icon>
            </div>
          </el-upload>
        </el-form-item>

        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>

        <el-form-item label="资料标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入资料标题" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="科目">
              <el-select v-model="form.subject" placeholder="请选择科目" style="width: 100%" clearable>
                <el-option label="语文" value="语文" />
                <el-option label="数学" value="数学" />
                <el-option label="英语" value="英语" />
                <el-option label="物理" value="物理" />
                <el-option label="化学" value="化学" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年级">
              <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%" clearable>
                <el-option v-for="i in 6" :key="'P'+i" :label="`小学${i}年级`" :value="`PRIMARY_${i}`" />
                <el-option v-for="i in 3" :key="'J'+i" :label="`初${i}`" :value="`JUNIOR_${i}`" />
                <el-option v-for="i in 3" :key="'S'+i" :label="`高${i}`" :value="`SENIOR_${i}`" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

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

        <el-form-item label="文件类型">
          <el-select v-model="form.fileType" placeholder="自动识别" style="width: 100%" clearable>
            <el-option label="PDF" value="pdf" />
            <el-option label="Word" value="docx" />
            <el-option label="Excel" value="xlsx" />
            <el-option label="PPT" value="pptx" />
            <el-option label="图片" value="jpg" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
      </el-form>
    </template>

    <template #footer>
      <el-button @click="$emit('close')">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ isEdit ? '保存' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import type { FormInstance, UploadFile, UploadInstance } from 'element-plus'
import { UploadFilled, Document, RefreshRight, Check } from '@element-plus/icons-vue'
import type { Material, MaterialRequest } from '@/api/material'

const props = defineProps<{
  visible: boolean
  material?: Material | null
  isFolder?: boolean
  parentId?: number | null
}>()

const emit = defineEmits<{
  close: []
  submit: [data: MaterialRequest & { file?: File }]
}>()

const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const loading = ref(false)
const isEdit = ref(false)
const fileList = ref<UploadFile[]>([])

// 文件夹颜色选项
const folderColors = [
  { value: '#409EFF', label: '蓝色' },
  { value: '#67C23A', label: '绿色' },
  { value: '#E6A23C', label: '橙色' },
  { value: '#F56C6C', label: '红色' },
  { value: '#909399', label: '灰色' },
  { value: '#9B59B6', label: '紫色' },
  { value: '#1ABC9C', label: '青色' },
  { value: '#3498DB', label: '天蓝' },
]

const uploadedFileName = ref('')
const uploadedFileSize = ref(0)

const form = reactive<MaterialRequest & { file?: File }>({
  title: '',
  filePath: undefined,
  fileType: undefined,
  subject: undefined,
  grade: undefined,
  tags: [],
  parentId: undefined,
  isFolder: false,
  color: undefined,
  file: undefined,
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
}

// 文件类型对应的颜色
const fileIconColor = computed(() => {
  const type = form.fileType
  if (type === 'pdf') return '#F56C6C'
  if (type === 'docx') return '#409EFF'
  if (type === 'xlsx') return '#67C23A'
  if (type === 'pptx') return '#E6A23C'
  if (type === 'jpg') return '#909399'
  return '#909399'
})

// 文件类型映射
const extTypeMap: Record<string, string> = {
  pdf: 'pdf',
  doc: 'docx', docx: 'docx',
  xls: 'xlsx', xlsx: 'xlsx',
  ppt: 'pptx', pptx: 'pptx',
  jpg: 'jpg', jpeg: 'jpg', png: 'jpg', gif: 'jpg', bmp: 'jpg',
}

// 科目关键词
const subjectKeywords: Record<string, string> = {
  '语文': '语文', '数学': '数学', '英语': '英语', '物理': '物理', '化学': '化学',
  '生物': '生物', '历史': '历史', '地理': '地理', '政治': '政治',
  'math': '数学', 'english': '英语', 'chinese': '语文', 'physics': '物理', 'chemistry': '化学',
}

// 标签关键词
const tagKeywords = ['讲义', '练习', '试卷', '笔记', '参考', '答案', '教案', '课件', '知识点', '总结', '期中', '期末', '月考', '单元测试']

// 年级解析规则
function parseGrade(name: string): string | undefined {
  // 小学: 小学X年级 / 小X
  const primaryMatch = name.match(/小学\s*(\d)\s*年级|小\s*(\d)/)
  if (primaryMatch) {
    const n = primaryMatch[1] || primaryMatch[2]
    return `PRIMARY_${n}`
  }
  // 初中: 初X / 初一初二初三 / 七年级八年级九年级
  const juniorMatch = name.match(/初\s*([一二三123])|([七八九789])\s*年级/)
  if (juniorMatch) {
    const v = juniorMatch[1] || juniorMatch[2]
    const map: Record<string, number> = { '一': 1, '二': 2, '三': 3, '七': 1, '八': 2, '九': 3 }
    const n = map[v] || parseInt(v)
    return `JUNIOR_${n}`
  }
  // 高中: 高X / 高一高二高三
  const seniorMatch = name.match(/高\s*([一二三123])/)
  if (seniorMatch) {
    const v = seniorMatch[1]
    const map: Record<string, number> = { '一': 1, '二': 2, '三': 3 }
    const n = map[v] || parseInt(v)
    return `SENIOR_${n}`
  }
  // 纯数字年级 1-6 = 小学, 7-9 = 初中, 10-12 = 高中
  const numMatch = name.match(/(\d)\s*年级/)
  if (numMatch) {
    const n = parseInt(numMatch[1])
    if (n >= 1 && n <= 6) return `PRIMARY_${n}`
    if (n >= 7 && n <= 9) return `JUNIOR_${n - 6}`
    if (n >= 10 && n <= 12) return `SENIOR_${n - 9}`
  }
  return undefined
}

// 解析文件名，自动填充表单
function parseFileName(fileName: string) {
  // 去掉扩展名，用于解析
  const nameWithoutExt = fileName.replace(/\.[^.]+$/, '')

  // 解析标题：用去掉扩展名的文件名作为标题
  form.title = nameWithoutExt

  // 解析科目
  for (const [keyword, subject] of Object.entries(subjectKeywords)) {
    if (nameWithoutExt.includes(keyword)) {
      form.subject = subject
      break
    }
  }

  // 解析年级
  form.grade = parseGrade(nameWithoutExt)

  // 解析标签
  const matchedTags: string[] = []
  for (const tag of tagKeywords) {
    if (nameWithoutExt.includes(tag)) {
      matchedTags.push(tag)
    }
  }
  form.tags = matchedTags
}

// 文件类型映射
function getFileType(fileName: string): string {
  const ext = fileName.split('.').pop()?.toLowerCase() || ''
  return extTypeMap[ext] || 'other'
}

// 格式化文件大小
function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + units[i]
}

// 文件选择回调
function handleFileChange(uploadFile: UploadFile) {
  const file = uploadFile.raw
  if (!file) return

  form.file = file
  form.filePath = file.name
  form.fileType = getFileType(file.name)
  uploadedFileName.value = file.name
  uploadedFileSize.value = file.size

  // 自动解析文件名
  parseFileName(file.name)
}

// 文件移除回调
function handleFileRemove() {
  form.file = undefined
  form.filePath = undefined
  form.fileType = undefined
  uploadedFileName.value = ''
  uploadedFileSize.value = 0
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
      color: props.material.color || undefined,
      file: undefined,
    })
    if (props.material.filePath) {
      uploadedFileName.value = props.material.filePath.split('/').pop() || props.material.filePath
      uploadedFileSize.value = props.material.fileSize || 0
    }
    fileList.value = []
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
      color: undefined,
      file: undefined,
    })
    uploadedFileName.value = ''
    uploadedFileSize.value = 0
    fileList.value = []
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

<style scoped>
.file-upload-area {
  width: 100%;
}

.file-upload-area :deep(.el-upload) {
  width: 100%;
}

.file-upload-area :deep(.el-upload-dragger) {
  width: 100%;
  padding: 24px 20px;
  border-radius: 8px;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-icon {
  font-size: 48px;
  color: #c0c4cc;
}

.upload-text {
  font-size: 14px;
  color: #606266;
}

.upload-text em {
  color: #409eff;
  font-style: normal;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
}

.upload-file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.file-detail {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.re-upload-icon {
  font-size: 20px;
  color: #909399;
  cursor: pointer;
  flex-shrink: 0;
}

.re-upload-icon:hover {
  color: #409eff;
}

:deep(.el-divider__text) {
  font-size: 13px;
  color: #909399;
}

.color-picker {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.color-item {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.color-item:hover {
  transform: scale(1.1);
}

.color-item.active {
  border-color: #303133;
  box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1);
}
</style>
