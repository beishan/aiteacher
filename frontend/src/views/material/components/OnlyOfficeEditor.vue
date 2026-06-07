<template>
  <el-dialog
    :model-value="visible"
    :title="title"
    fullscreen
    :close-on-click-modal="false"
    destroy-on-close
    @close="handleClose"
  >
    <div class="editor-wrapper" v-loading="loading" element-loading-text="加载编辑器中...">
      <div id="onlyoffice-editor-container" ref="editorContainer" class="editor-container" />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onBeforeUnmount } from 'vue'
import { getOnlyOfficeConfig } from '@/api/material'

const props = defineProps<{
  visible: boolean
  materialId: number
  title: string
}>()

const emit = defineEmits<{
  close: []
  saved: []
}>()

const loading = ref(true)
const editorContainer = ref<HTMLElement>()
let editorInstance: any = null

// 已加载的 script 缓存
let scriptLoaded = false

function loadScript(src: string): Promise<void> {
  return new Promise((resolve, reject) => {
    if (scriptLoaded || (window as any).DocsAPI) {
      scriptLoaded = true
      resolve()
      return
    }
    const script = document.createElement('script')
    script.src = src
    script.async = true
    script.onload = () => {
      scriptLoaded = true
      resolve()
    }
    script.onerror = () => reject(new Error('加载 OnlyOffice API 失败'))
    document.head.appendChild(script)
  })
}

watch(() => props.visible, async (val) => {
  if (val && props.materialId) {
    loading.value = true
    await nextTick()
    await initEditor()
  }
}, { immediate: true })

async function initEditor() {
  try {
    // 1. 从后端获取编辑器配置
    const res = await getOnlyOfficeConfig(props.materialId)
    const config = res.data

    // 2. 加载 OnlyOffice JS API
    await loadScript(config.documentServerApiUrl)

    // 3. 等待 DOM 就绪
    await nextTick()
    await new Promise(r => setTimeout(r, 100))

    if (!editorContainer.value) {
      console.error('编辑器容器不存在')
      loading.value = false
      return
    }

    // 4. 创建编辑器
    const DocsAPI = (window as any).DocsAPI
    editorInstance = new DocsAPI.DocEditor('onlyoffice-editor-container', {
      document: config.document,
      editorConfig: config.editorConfig,
      type: config.type,
      width: '100%',
      height: '100%',
      token: config.token,
      events: {
        onAppReady: () => {
          loading.value = false
        },
        onError: (event: any) => {
          console.error('OnlyOffice 错误:', event)
          loading.value = false
        },
      },
    })
  } catch (error: any) {
    console.error('初始化编辑器失败:', error?.message || error)
    loading.value = false
  }
}

function handleClose() {
  destroyEditor()
  emit('saved')
  emit('close')
}

function destroyEditor() {
  if (editorInstance) {
    try {
      editorInstance.destroyEditor()
    } catch (e) {
      // ignore
    }
    editorInstance = null
  }
}

onBeforeUnmount(() => {
  destroyEditor()
})
</script>

<style scoped>
.editor-wrapper {
  height: calc(100vh - 100px);
  background: #fff;
  border-radius: 4px;
}

.editor-container {
  width: 100%;
  height: 100%;
}
</style>
