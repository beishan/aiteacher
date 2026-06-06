<template>
  <div class="ai-chat">
    <el-card class="chat-card">
      <template #header>
        <div class="chat-header">
          <div>
            <el-icon :size="24" color="#67C23A"><ChatDotRound /></el-icon>
            <span style="margin-left: 8px; font-size: 18px; font-weight: 600">AI 智能助手</span>
          </div>
          <el-button @click="clearChat">
            <el-icon><Delete /></el-icon>清空对话
          </el-button>
        </div>
      </template>

      <!-- 消息列表 -->
      <div class="chat-messages" ref="messagesRef">
        <div v-if="messages.length === 0" class="empty-chat">
          <el-icon :size="64" color="#c0c4cc"><ChatDotRound /></el-icon>
          <p>你好！我是你的 AI 教学助手，有什么可以帮你的？</p>
          <div class="quick-questions">
            <el-button v-for="q in quickQuestions" :key="q" @click="sendQuickQuestion(q)" round>
              {{ q }}
            </el-button>
          </div>
        </div>

        <ChatMessage
          v-for="(msg, index) in messages"
          :key="index"
          :content="msg.content"
          :is-user="msg.role === 'user'"
          :meta="msg.meta"
        />

        <div v-if="loading" class="typing-indicator">
          <ChatMessage content="正在思考中..." :is-user="false" />
        </div>
      </div>

      <!-- 输入框 -->
      <div class="chat-input">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="2"
          placeholder="输入你的问题... (Enter 发送，Shift+Enter 换行)"
          @keydown="handleKeydown"
          :disabled="loading"
        />
        <el-button
          type="primary"
          :loading="loading"
          @click="sendMessage"
          :disabled="!inputMessage.trim()"
        >
          发送
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { ChatDotRound, Delete } from '@element-plus/icons-vue'
import { aiChat } from '@/api/ai'
import type { ChatMessage as ChatMsg } from '@/api/ai'
import ChatMessage from './components/ChatMessage.vue'

interface Message {
  role: 'user' | 'assistant'
  content: string
  meta?: {
    modelName?: string
    durationMs?: number
  }
}

const messagesRef = ref<HTMLElement>()
const inputMessage = ref('')
const loading = ref(false)
const messages = ref<Message[]>([])

const quickQuestions = [
  '今天有哪些课？',
  '哪些作业还没批改？',
  '帮我出5道数学题',
  '张同学最近学习情况怎么样？',
]

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

async function sendQuickQuestion(question: string) {
  inputMessage.value = question
  await sendMessage()
}

async function sendMessage() {
  const content = inputMessage.value.trim()
  if (!content || loading.value) return

  // 添加用户消息
  messages.value.push({ role: 'user', content })
  inputMessage.value = ''
  loading.value = true

  scrollToBottom()

  try {
    // 构建历史消息
    const history: ChatMsg[] = messages.value
      .slice(0, -1)
      .map(m => ({ role: m.role, content: m.content }))

    const res = await aiChat({ message: content, history })

    // 添加 AI 回复
    messages.value.push({
      role: 'assistant',
      content: res.data.reply,
      meta: {
        modelName: res.data.modelName,
        durationMs: res.data.durationMs,
      },
    })
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: '抱歉，出现了错误，请稍后重试。',
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function clearChat() {
  messages.value = []
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.ai-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.chat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-card :deep(.el-card__header) {
  flex-shrink: 0;
}

.chat-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
  min-height: 0;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.empty-chat p {
  margin-top: 16px;
  font-size: 16px;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 24px;
  justify-content: center;
}

.chat-input {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
  background: #fff;
}

.chat-input .el-input {
  flex: 1;
}

.chat-input .el-button {
  align-self: flex-end;
}
</style>
