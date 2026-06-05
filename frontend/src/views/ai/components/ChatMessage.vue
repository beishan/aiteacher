<template>
  <div class="chat-message" :class="{ 'message-user': isUser, 'message-ai': !isUser }">
    <div class="message-avatar">
      <el-avatar :size="36" :style="{ backgroundColor: isUser ? '#409EFF' : '#67C23A' }">
        {{ isUser ? '师' : 'AI' }}
      </el-avatar>
    </div>
    <div class="message-content">
      <div class="message-bubble">
        <div class="message-text" v-html="formattedContent"></div>
      </div>
      <div class="message-meta" v-if="!isUser && meta">
        <span>{{ meta.modelName }}</span>
        <span v-if="meta.durationMs">· {{ (meta.durationMs / 1000).toFixed(1) }}s</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  content: string
  isUser: boolean
  meta?: {
    modelName?: string
    durationMs?: number
  }
}>()

const formattedContent = computed(() => {
  // 简单的 Markdown 转换
  let text = props.content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
  return text
})
</script>

<style scoped>
.chat-message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message-user {
  flex-direction: row-reverse;
}

.message-user .message-bubble {
  background: #409eff;
  color: white;
  border-radius: 12px 0 12px 12px;
}

.message-ai .message-bubble {
  background: #f4f4f5;
  color: #303133;
  border-radius: 0 12px 12px 12px;
}

.message-content {
  max-width: 70%;
}

.message-bubble {
  padding: 12px 16px;
  line-height: 1.6;
}

.message-text {
  word-break: break-word;
}

.message-text :deep(code) {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 4px;
  border-radius: 3px;
  font-family: monospace;
}

.message-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
