<template>
  <div class="settings-view">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-size: 18px; font-weight: 600">系统设置</span>
          <el-button type="primary" :loading="saving" @click="handleSave">保存设置</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 界面设置 -->
        <el-tab-pane label="界面设置" name="appearance">
          <div class="theme-section">
            <h3 class="section-title">主题风格</h3>
            <p class="section-desc">选择您喜欢的界面主题，不同主题有不同的布局和视觉风格</p>

            <div class="theme-grid">
              <div
                v-for="theme in allThemes"
                :key="theme.name"
                class="theme-card"
                :class="{ active: themeStore.currentTheme === theme.name }"
                @click="handleThemeChange(theme.name)"
              >
                <div class="theme-preview">
                  <div class="preview-sidebar" :style="{ background: theme.preview.bgSecondary }">
                    <div class="preview-dot" :style="{ background: theme.preview.accent }"></div>
                    <div class="preview-line" v-for="i in 4" :key="i"></div>
                  </div>
                  <div class="preview-main" :style="{ background: theme.preview.bgPrimary }">
                    <div class="preview-header" :style="{ background: theme.preview.bgSecondary }"></div>
                    <div class="preview-content">
                      <div class="preview-card" v-for="i in 3" :key="i"></div>
                    </div>
                  </div>
                </div>
                <div class="theme-info">
                  <span class="theme-icon">{{ theme.icon }}</span>
                  <span class="theme-name">{{ theme.label }}</span>
                </div>
                <p class="theme-desc">{{ theme.description }}</p>
                <div v-if="themeStore.currentTheme === theme.name" class="theme-badge">
                  <span>✓ 当前使用</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- AI 模型配置 -->
        <el-tab-pane label="AI 模型配置" name="ai">
          <el-form label-width="150px" style="max-width: 600px">
            <el-divider content-position="left">默认模型</el-divider>
            <el-form-item label="默认 AI 模型">
              <el-select v-model="settings['ai.default_model']" style="width: 100%">
                <el-option label="Claude" value="claude" />
                <el-option label="OpenAI GPT" value="openai" />
                <el-option label="Ollama (本地)" value="ollama" />
              </el-select>
            </el-form-item>

            <el-divider content-position="left">Claude 配置</el-divider>
            <el-form-item label="API Key">
              <el-input
                v-model="settings['ai.claude.api_key']"
                type="password"
                show-password
                placeholder="请输入 Claude API Key"
              />
            </el-form-item>
            <el-form-item label="模型版本">
              <el-select v-model="settings['ai.claude.model']" style="width: 100%">
                <el-option label="Claude Sonnet 4" value="claude-sonnet-4-20250514" />
                <el-option label="Claude Haiku 3.5" value="claude-3-5-haiku-20241022" />
              </el-select>
            </el-form-item>

            <el-divider content-position="left">OpenAI 配置</el-divider>
            <el-form-item label="API Key">
              <el-input
                v-model="settings['ai.openai.api_key']"
                type="password"
                show-password
                placeholder="请输入 OpenAI API Key"
              />
            </el-form-item>
            <el-form-item label="API 地址">
              <el-input v-model="settings['ai.openai.base_url']" placeholder="https://api.openai.com/v1" />
            </el-form-item>
            <el-form-item label="模型版本">
              <el-select v-model="settings['ai.openai.model']" style="width: 100%">
                <el-option label="GPT-4o" value="gpt-4o" />
                <el-option label="GPT-4o Mini" value="gpt-4o-mini" />
                <el-option label="GPT-4 Turbo" value="gpt-4-turbo" />
              </el-select>
            </el-form-item>

            <el-divider content-position="left">Ollama 本地模型配置</el-divider>
            <el-form-item label="API 地址">
              <el-input v-model="settings['ai.ollama.base_url']" placeholder="http://localhost:11434" />
            </el-form-item>
            <el-form-item label="模型名称">
              <el-input v-model="settings['ai.ollama.model']" placeholder="qwen2.5" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 通知配置 -->
        <el-tab-pane label="通知配置" name="notification">
          <el-form label-width="150px" style="max-width: 600px">
            <el-form-item label="企业微信 Webhook">
              <el-input
                v-model="settings['notification.wechat_webhook']"
                placeholder="请输入企业微信机器人 Webhook 地址"
              />
            </el-form-item>
            <el-form-item label="课程提醒时间">
              <el-input-number
                v-model="reminderMinutes"
                :min="5"
                :max="120"
                :step="5"
              />
              <span style="margin-left: 8px">分钟前</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- AI 使用统计 -->
        <el-tab-pane label="使用统计" name="stats">
          <el-empty description="功能开发中" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSettings, updateSettings } from '@/api/settings'
import { useThemeStore } from '@/stores/theme'
import type { ThemeType } from '@/stores/theme'

const themeStore = useThemeStore()
const allThemes = themeStore.getAllThemes()

const activeTab = ref('appearance')
const saving = ref(false)

function handleThemeChange(theme: ThemeType) {
  themeStore.setTheme(theme)
  ElMessage.success(`已切换到「${themeStore.config.label}」主题`)
}

const settings = reactive<Record<string, string>>({
  'ai.default_model': 'claude',
  'ai.claude.api_key': '',
  'ai.claude.model': 'claude-sonnet-4-20250514',
  'ai.openai.api_key': '',
  'ai.openai.base_url': 'https://api.openai.com/v1',
  'ai.openai.model': 'gpt-4o',
  'ai.ollama.base_url': 'http://localhost:11434',
  'ai.ollama.model': 'qwen2.5',
  'notification.wechat_webhook': '',
  'notification.reminder_minutes': '30',
})

const reminderMinutes = computed({
  get: () => parseInt(settings['notification.reminder_minutes'] || '30'),
  set: (val) => { settings['notification.reminder_minutes'] = String(val) },
})

async function fetchSettings() {
  try {
    const res = await getSettings()
    for (const item of res.data) {
      if (item.key in settings) {
        settings[item.key] = item.value || ''
      }
    }
  } catch (error) {
    // handled
  }
}

async function handleSave() {
  saving.value = true
  try {
    await updateSettings(settings)
    ElMessage.success('设置已保存')
  } catch (error) {
    // handled
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchSettings()
})
</script>

<style scoped>
.settings-view {
  width: 100%;
}

/* ===== 主题设置区域 ===== */
.theme-section {
  padding: 20px 0;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.section-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 24px;
}

/* ===== 主题卡片网格 ===== */
.theme-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.theme-card {
  position: relative;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fff;
}

.theme-card:hover {
  border-color: #c0c4cc;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.theme-card.active {
  border-color: #409EFF;
  box-shadow: 0 0 0 1px #409EFF;
}

/* ===== 主题预览 ===== */
.theme-preview {
  display: flex;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;
  border: 1px solid #ebeef5;
}

.preview-sidebar {
  width: 32px;
  padding: 8px 6px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.preview-dot {
  width: 20px;
  height: 20px;
  border-radius: 4px;
}

.preview-line {
  height: 3px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}

.preview-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.preview-header {
  height: 20px;
}

.preview-content {
  flex: 1;
  padding: 8px;
  display: flex;
  gap: 6px;
}

.preview-card {
  flex: 1;
  background: rgba(0, 0, 0, 0.06);
  border-radius: 4px;
}

/* ===== 主题信息 ===== */
.theme-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.theme-icon {
  font-size: 20px;
}

.theme-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.theme-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.theme-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #409EFF;
  color: #fff;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 500;
}
</style>
