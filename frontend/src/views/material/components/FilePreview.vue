<template>
  <el-dialog
    :model-value="visible"
    :title="'预览：' + title"
    width="80%"
    top="5vh"
    destroy-on-close
    @close="$emit('close')"
  >
    <div class="preview-container" v-loading="loading">
      <!-- PDF 预览 -->
      <iframe
        v-if="isPdf"
        :src="url"
        class="preview-iframe"
        @load="loading = false"
      />

      <!-- Word 预览（mammoth 转 HTML） -->
      <div
        v-else-if="isWord"
        class="preview-html"
        v-html="wordHtml"
      />

      <!-- Excel / PPT：下载提示 -->
      <div v-else-if="isOffice" class="preview-unsupported">
        <el-icon :size="64" color="#c0c4cc"><Document /></el-icon>
        <p>{{ title }}</p>
        <p class="hint">该格式暂不支持在线预览，请下载后查看</p>
        <el-button type="primary" @click="downloadFile">
          <el-icon><Download /></el-icon>下载文件
        </el-button>
      </div>

      <!-- 图片预览 -->
      <div v-else-if="isImage" class="preview-image-wrapper">
        <img :src="url" class="preview-image" @load="loading = false" />
      </div>

      <!-- 不支持的类型 -->
      <div v-else class="preview-unsupported">
        <el-icon :size="64" color="#c0c4cc"><Document /></el-icon>
        <p>该文件类型暂不支持在线预览</p>
        <el-button type="primary" @click="downloadFile">下载文件</el-button>
      </div>
    </div>

    <template #footer>
      <el-button @click="$emit('close')">关闭</el-button>
      <el-button type="primary" @click="downloadFile">
        <el-icon><Download /></el-icon>下载
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Document, Download } from '@element-plus/icons-vue'

const props = defineProps<{
  visible: boolean
  url: string
  title: string
  fileType: string
}>()

defineEmits<{
  close: []
}>()

const loading = ref(true)
const wordHtml = ref('')

const isPdf = computed(() => props.fileType === 'pdf')
const isImage = computed(() => ['jpg', 'jpeg', 'png', 'gif'].includes(props.fileType))
const isWord = computed(() => ['doc', 'docx'].includes(props.fileType))
const isOffice = computed(() => ['xls', 'xlsx', 'ppt', 'pptx'].includes(props.fileType))

watch(() => props.visible, async (val) => {
  if (val) {
    loading.value = true
    wordHtml.value = ''
    if (isWord.value && props.url) {
      await loadWordPreview()
    }
  }
})

async function loadWordPreview() {
  try {
    const response = await fetch(props.url)
    const arrayBuffer = await response.arrayBuffer()
    const mammoth = await import('mammoth')
    const result = await mammoth.convertToHtml({ arrayBuffer })
    wordHtml.value = result.value || '<p style="color:#909399">文档内容为空</p>'

    // 如果有警告信息，输出到控制台
    if (result.messages.length > 0) {
      console.warn('Word 预览警告:', result.messages)
    }
  } catch (error) {
    console.error('Word 预览失败:', error)
    wordHtml.value = '<p style="color:#f56c6c">文档解析失败，请下载后查看</p>'
  } finally {
    loading.value = false
  }
}

function downloadFile() {
  if (props.url) {
    window.open(props.url, '_blank')
  }
}
</script>

<style scoped>
.preview-container {
  height: 75vh;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-html {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  padding: 32px 48px;
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
}

.preview-html :deep(h1) {
  font-size: 24px;
  margin: 20px 0 12px;
}

.preview-html :deep(h2) {
  font-size: 20px;
  margin: 18px 0 10px;
}

.preview-html :deep(h3) {
  font-size: 17px;
  margin: 16px 0 8px;
}

.preview-html :deep(p) {
  margin: 8px 0;
}

.preview-html :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 12px 0;
}

.preview-html :deep(th),
.preview-html :deep(td) {
  border: 1px solid #dcdfe6;
  padding: 8px 12px;
  text-align: left;
}

.preview-html :deep(th) {
  background: #f5f7fa;
  font-weight: 600;
}

.preview-html :deep(img) {
  max-width: 100%;
  height: auto;
}

.preview-html :deep(ul),
.preview-html :deep(ol) {
  padding-left: 24px;
  margin: 8px 0;
}

.preview-image-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: auto;
  background: #f5f5f5;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.preview-unsupported {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.preview-unsupported p {
  margin: 12px 0 4px;
  font-size: 16px;
}

.preview-unsupported .hint {
  font-size: 13px;
  color: #c0c4cc;
  margin-bottom: 16px;
}
</style>
