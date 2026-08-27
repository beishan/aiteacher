<template>
  <div class="settings-view">
    <header class="settings-page-header">
      <div>
        <h1>系统设置</h1>
        <p>管理界面外观、AI 服务、系统通知与用户账号</p>
      </div>
      <span v-if="activeTab !== 'profile' && activeTab !== 'subjects'" class="auto-save-status" :class="`state-${autoSaveState}`">
        <i aria-hidden="true" />{{ autoSaveLabel }}
      </span>
      <span v-else-if="activeTab === 'profile'" class="auto-save-status state-idle"><i aria-hidden="true" />个人信息需手动保存</span>
      <span v-else class="auto-save-status state-saved"><i aria-hidden="true" />科目变更即时生效</span>
    </header>

    <div class="settings-layout">
      <nav class="settings-nav" aria-label="设置导航">
        <section v-for="group in tabGroups" :key="group.label" class="settings-nav-group">
          <div class="settings-nav-title">{{ group.label }}</div>
          <el-button
            v-for="item in group.items"
            :key="item.key"
            text
            class="settings-nav-item"
            :class="{ active: activeTab === item.key }"
            :aria-current="activeTab === item.key ? 'page' : undefined"
            @click="activeTab = item.key"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </el-button>
        </section>
      </nav>

      <main class="settings-content">
        <el-card v-if="activeTab === 'profile'" class="settings-panel">
          <template #header>
            <div class="panel-heading">
              <span class="panel-icon panel-icon-blue"><el-icon><User /></el-icon></span>
              <div><h2>个人信息</h2><p>管理头像、昵称和个人备注</p></div>
            </div>
          </template>
          <ProfileSettings />
        </el-card>

        <el-card v-if="activeTab === 'subjects'" class="settings-panel">
          <template #header>
            <div class="panel-heading">
              <span class="panel-icon panel-icon-teal"><el-icon><Collection /></el-icon></span>
              <div><h2>科目管理</h2><p>统一维护教学业务使用的科目目录</p></div>
            </div>
          </template>
          <SubjectManagementSettings />
        </el-card>

        <el-card v-show="activeTab === 'appearance'" class="settings-panel appearance-panel">
          <el-tabs v-model="appearanceTab" class="appearance-tabs">
            <el-tab-pane label="主题设置" name="theme">
              <div class="appearance-tab-content">
                <SiteIconSettings />

                <section class="theme-section width-section">
                  <div class="section-heading">
                    <div><h3>界面主题宽度</h3><p>选择页面内容铺满可用区域，或以舒适宽度居中显示。</p></div>
                    <el-tag round effect="light">当前 · {{ themeStore.contentWidth === 'full' ? '全屏' : '居中' }}</el-tag>
                  </div>

                  <div class="width-options" role="radiogroup" aria-label="界面主题宽度">
                    <el-button
                      v-for="option in contentWidthOptions"
                      :key="option.value"
                      text
                      class="width-option"
                      :class="{ active: themeStore.contentWidth === option.value }"
                      role="radio"
                      :aria-checked="themeStore.contentWidth === option.value"
                      @click="handleContentWidthChange(option.value)"
                    >
                      <span class="width-preview" :class="`preview-${option.value}`">
                        <i class="width-preview-bar" />
                        <i class="width-preview-body" />
                      </span>
                      <span><strong>{{ option.label }}</strong><small>{{ option.description }}</small></span>
                      <span v-if="themeStore.contentWidth === option.value" class="width-check"><el-icon><Check /></el-icon></span>
                    </el-button>
                  </div>
                </section>

                <section class="theme-section">
                  <div class="section-heading">
                    <div><h3>主题风格</h3><p>不同主题包含独立的导航布局、色彩和组件外观。</p></div>
                    <el-tag round effect="light">当前 · {{ themeStore.config.label }}</el-tag>
                  </div>

                  <div class="theme-grid">
                    <el-button
                      v-for="theme in allThemes"
                      :key="theme.name"
                      text
                      class="theme-card"
                      :class="{ active: themeStore.currentTheme === theme.name }"
                      @click="handleThemeChange(theme.name)"
                    >
                      <span class="theme-preview" :class="{ 'preview-macos26': theme.name === 'macos26' }">
                        <span class="preview-sidebar" :style="{ background: theme.preview.bgSecondary }">
                          <i class="preview-dot" :style="{ background: theme.preview.accent }" />
                          <i v-for="i in 4" :key="i" class="preview-line" />
                        </span>
                        <span class="preview-main" :style="{ background: theme.preview.bgPrimary }">
                          <i class="preview-header" :style="{ background: theme.preview.bgSecondary }" />
                          <span class="preview-content"><i v-for="i in 3" :key="i" class="preview-card" /></span>
                        </span>
                      </span>
                      <span class="theme-info"><b class="theme-icon">{{ theme.icon }}</b><strong>{{ theme.label }}</strong></span>
                      <span class="theme-desc">{{ theme.description }}</span>
                      <span v-if="themeStore.currentTheme === theme.name" class="theme-check"><el-icon><Check /></el-icon></span>
                    </el-button>
                  </div>
                </section>
              </div>
            </el-tab-pane>

            <el-tab-pane label="Dock设置" name="dock">
              <div class="appearance-tab-content dock-tab-content">
                <el-alert
                  v-if="themeStore.currentTheme !== 'macos26'"
                  title="Dock 仅在 macOS 26 主题下显示；当前配置会保存，并在切换到该主题后生效。"
                  type="info"
                  :closable="false"
                  show-icon
                />
                <MacosDockSettings />
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>

        <el-card v-show="activeTab === 'ai'" class="settings-panel">
          <template #header>
            <div class="panel-heading">
              <span class="panel-icon panel-icon-purple"><el-icon><MagicStick /></el-icon></span>
              <div><h2>AI 模型配置</h2><p>配置默认模型和各服务商连接信息</p></div>
            </div>
          </template>
          <el-form label-position="top" class="settings-form">
            <div class="form-section">
              <h3>默认模型</h3>
              <el-form-item label="默认 AI 模型">
                <el-select v-model="settings['ai.default_model']" style="width: 100%">
                  <el-option label="Claude" value="claude" />
                  <el-option label="OpenAI GPT" value="openai" />
                  <el-option label="Ollama（本地）" value="ollama" />
                </el-select>
              </el-form-item>
            </div>

            <div class="form-section">
              <h3>Claude</h3>
              <div class="form-grid">
                <el-form-item label="API Key">
                  <el-input v-model="settings['ai.claude.api_key']" type="password" show-password placeholder="请输入 Claude API Key" />
                </el-form-item>
                <el-form-item label="模型版本">
                  <el-select v-model="settings['ai.claude.model']" style="width: 100%">
                    <el-option label="Claude Sonnet 4" value="claude-sonnet-4-20250514" />
                    <el-option label="Claude Haiku 3.5" value="claude-3-5-haiku-20241022" />
                  </el-select>
                </el-form-item>
              </div>
            </div>

            <div class="form-section">
              <h3>OpenAI</h3>
              <div class="form-grid">
                <el-form-item label="API Key">
                  <el-input v-model="settings['ai.openai.api_key']" type="password" show-password placeholder="请输入 OpenAI API Key" />
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
              </div>
            </div>

            <div class="form-section">
              <h3>Ollama 本地模型</h3>
              <div class="form-grid">
                <el-form-item label="API 地址">
                  <el-input v-model="settings['ai.ollama.base_url']" placeholder="http://localhost:11434" />
                </el-form-item>
                <el-form-item label="模型名称">
                  <el-input v-model="settings['ai.ollama.model']" placeholder="qwen2.5" />
                </el-form-item>
              </div>
            </div>
          </el-form>
        </el-card>

        <el-card v-show="activeTab === 'notification'" class="settings-panel">
          <template #header>
            <div class="panel-heading">
              <span class="panel-icon panel-icon-orange"><el-icon><Bell /></el-icon></span>
              <div><h2>通知配置</h2><p>管理消息推送渠道和课程提醒时间</p></div>
            </div>
          </template>
          <el-form label-position="top" class="settings-form">
            <div class="form-section">
              <h3>企业微信</h3>
              <el-form-item label="Webhook 地址">
                <el-input v-model="settings['notification.wechat_webhook']" placeholder="请输入企业微信机器人 Webhook 地址" />
              </el-form-item>
            </div>
            <div class="form-section">
              <h3>课程提醒</h3>
              <el-form-item label="提前提醒时间">
                <div class="number-setting">
                  <el-input-number v-model="reminderMinutes" :min="5" :max="120" :step="5" />
                  <span>分钟前</span>
                </div>
              </el-form-item>
            </div>
          </el-form>
        </el-card>

        <el-card v-show="activeTab === 'stats'" class="settings-panel">
          <template #header>
            <div class="panel-heading">
              <span class="panel-icon panel-icon-green"><el-icon><DataAnalysis /></el-icon></span>
              <div><h2>使用统计</h2><p>查看 AI 服务调用量和资源使用情况</p></div>
            </div>
          </template>
          <el-empty description="功能开发中" />
        </el-card>

        <el-card v-if="activeTab === 'systemInfo'" class="settings-panel">
          <template #header>
            <div class="panel-heading">
              <span class="panel-icon panel-icon-cyan"><el-icon><InfoFilled /></el-icon></span>
              <div><h2>系统信息</h2><p>查看版本、运行环境与相关服务健康状态</p></div>
            </div>
          </template>
          <SystemInfoSettings />
        </el-card>

        <el-card v-if="isAdmin && activeTab === 'users'" class="settings-panel">
          <template #header>
            <div class="panel-heading">
              <span class="panel-icon panel-icon-red"><el-icon><UserFilled /></el-icon></span>
              <div><h2>系统用户管理</h2><p>管理系统账号状态，并为用户重置登录密码</p></div>
            </div>
          </template>
          <UserManagementSettings />
        </el-card>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { Bell, Check, Collection, DataAnalysis, InfoFilled, MagicStick, Monitor, User, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import { getSettings, updateSettings } from '@/api/settings'
import MacosDockSettings from '@/components/MacosDockSettings.vue'
import SiteIconSettings from '@/components/SiteIconSettings.vue'
import UserManagementSettings from '@/components/UserManagementSettings.vue'
import SystemInfoSettings from '@/components/SystemInfoSettings.vue'
import ProfileSettings from '@/components/ProfileSettings.vue'
import SubjectManagementSettings from '@/components/SubjectManagementSettings.vue'
import { useThemeStore } from '@/stores/theme'
import type { ContentWidth, ThemeType } from '@/stores/theme'
import { useDockStore } from '@/stores/dock'
import { useBrandingStore } from '@/stores/branding'
import { useUserStore } from '@/stores/user'

type SettingsTab = 'profile' | 'subjects' | 'appearance' | 'ai' | 'notification' | 'stats' | 'systemInfo' | 'users'
type AutoSaveState = 'idle' | 'pending' | 'saving' | 'saved' | 'error'
const AUTO_SAVE_DELAY = 800
const AUTO_SAVE_KEYS = [
  'ai.default_model',
  'ai.claude.api_key',
  'ai.claude.model',
  'ai.openai.api_key',
  'ai.openai.base_url',
  'ai.openai.model',
  'ai.ollama.base_url',
  'ai.ollama.model',
  'notification.wechat_webhook',
  'notification.reminder_minutes',
] as const
const themeStore = useThemeStore()
const dockStore = useDockStore()
const brandingStore = useBrandingStore()
const userStore = useUserStore()
const route = useRoute()
const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')
const allThemes = themeStore.getAllThemes()
const contentWidthOptions: { value: ContentWidth; label: string; description: string }[] = [
  { value: 'full', label: '全屏', description: '内容宽度占满当前可用区域' },
  { value: 'centered', label: '居中', description: '内容以设置页相近宽度居中显示' },
]
const activeTab = ref<SettingsTab>(route.query.tab === 'profile' ? 'profile' : 'appearance')
const appearanceTab = ref<'theme' | 'dock'>('theme')
const autoSaveState = ref<AutoSaveState>('idle')
const autoSaveReady = ref(false)
const autoSaveLabel = computed(() => ({
  idle: '配置将自动保存',
  pending: '等待自动保存',
  saving: '正在自动保存',
  saved: '已自动保存',
  error: '自动保存失败',
})[autoSaveState.value])
let autoSaveTimer: ReturnType<typeof setTimeout> | undefined
let autoSaveGeneration = 0
const tabGroups = computed(() => [
  { label: '账号', items: [{ key: 'profile' as const, label: '个人信息', icon: User }] },
  { label: '教学', items: [{ key: 'subjects' as const, label: '科目管理', icon: Collection }] },
  { label: '外观', items: [{ key: 'appearance' as const, label: '系统外观', icon: Monitor }] },
  { label: '智能服务', items: [{ key: 'ai' as const, label: 'AI 模型', icon: MagicStick }] },
  { label: '系统', items: [
    { key: 'notification' as const, label: '通知配置', icon: Bell },
    { key: 'stats' as const, label: '使用统计', icon: DataAnalysis },
    { key: 'systemInfo' as const, label: '系统信息', icon: InfoFilled },
    ...(isAdmin.value ? [{ key: 'users' as const, label: '用户管理', icon: UserFilled }] : []),
  ] },
])

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
  'ui.theme': themeStore.currentTheme,
  'ui.dock.size': String(dockStore.size),
  'ui.dock.opacity': String(dockStore.opacity),
  'ui.dock.magnification': String(dockStore.magnification),
  'ui.dock.blur': String(dockStore.blur),
})

const reminderMinutes = computed({
  get: () => parseInt(settings['notification.reminder_minutes'] || '30'),
  set: value => { settings['notification.reminder_minutes'] = String(value) },
})

async function handleThemeChange(theme: ThemeType) {
  if (themeStore.currentTheme === theme) return
  themeStore.setTheme(theme)
  settings['ui.theme'] = theme
  try {
    await themeStore.persistTheme(theme)
    ElMessage.success(`已切换并保存「${themeStore.config.label}」主题`)
  } catch {
    ElMessage.warning(`已在当前浏览器切换主题，但服务器保存失败`)
  }
}

async function handleContentWidthChange(width: ContentWidth) {
  if (themeStore.contentWidth === width) return
  themeStore.setContentWidth(width)
  try {
    await themeStore.persistContentWidth(width)
    ElMessage.success(`界面宽度已切换并保存为「${width === 'full' ? '全屏' : '居中'}」`)
  } catch {
    ElMessage.warning('界面宽度已在当前浏览器切换，但服务器保存失败')
  }
}

async function fetchSettings() {
  autoSaveReady.value = false
  let loaded = false
  try {
    const response = await getSettings()
    for (const item of response.data) {
      if (item.key in settings) settings[item.key] = item.value || ''
    }
    themeStore.hydrateFromSettings(response.data)
    dockStore.hydrateFromSettings(response.data)
    brandingStore.hydrateFromSettings(response.data)
    loaded = true
  } catch {
    // request interceptor handles errors
  } finally {
    await nextTick()
    autoSaveReady.value = true
    autoSaveState.value = loaded ? 'saved' : 'error'
  }
}

function scheduleAutoSave() {
  if (!autoSaveReady.value) return
  if (autoSaveTimer) clearTimeout(autoSaveTimer)
  const generation = ++autoSaveGeneration
  autoSaveState.value = 'pending'
  autoSaveTimer = setTimeout(() => void persistGeneralSettings(generation), AUTO_SAVE_DELAY)
}

async function persistGeneralSettings(generation = autoSaveGeneration) {
  if (autoSaveTimer) {
    clearTimeout(autoSaveTimer)
    autoSaveTimer = undefined
  }
  autoSaveState.value = 'saving'
  try {
    await updateSettings(Object.fromEntries(AUTO_SAVE_KEYS.map(key => [key, settings[key]])))
    if (generation === autoSaveGeneration) autoSaveState.value = 'saved'
  } catch {
    if (generation === autoSaveGeneration) autoSaveState.value = 'error'
  }
}

watch(() => AUTO_SAVE_KEYS.map(key => settings[key]), scheduleAutoSave, { flush: 'post' })
watch(() => route.query.tab, tab => {
  if (tab === 'profile') activeTab.value = 'profile'
})
onMounted(fetchSettings)
onBeforeUnmount(() => {
  if (autoSaveState.value === 'pending') void persistGeneralSettings()
})
</script>

<style scoped>
.settings-view { width: min(1220px, 100%); margin: 0 auto; padding: 4px 0 18px; }
.settings-page-header { display: flex; align-items: center; justify-content: space-between; gap: 20px; margin-bottom: 24px; }
.settings-page-header h1 { margin: 0 0 7px; color: var(--color-text-primary, #303133); font-size: clamp(25px, 3vw, 32px); line-height: 1.2; }
.settings-page-header p { margin: 0; color: var(--color-text-secondary, #909399); font-size: 14px; }
.auto-save-status { display: inline-flex; flex: 0 0 auto; align-items: center; gap: 8px; padding: 7px 11px; border: 1px solid var(--color-border-light, #e4e7ed); border-radius: 999px; background: var(--color-bg-card, rgba(255,255,255,.72)); color: var(--color-text-secondary, #606266); font-size: 12px; white-space: nowrap; }
.auto-save-status i { width: 7px; height: 7px; border-radius: 50%; background: #909399; }
.auto-save-status.state-pending i, .auto-save-status.state-saving i { background: #409eff; box-shadow: 0 0 0 4px color-mix(in srgb, #409eff 12%, transparent); }
.auto-save-status.state-saving i { animation: auto-save-pulse 1s ease-in-out infinite; }
.auto-save-status.state-saved i { background: #25b864; box-shadow: 0 0 0 4px color-mix(in srgb, #25b864 12%, transparent); }
.auto-save-status.state-error { color: #f56c6c; }
.auto-save-status.state-error i { background: #f56c6c; box-shadow: 0 0 0 4px color-mix(in srgb, #f56c6c 12%, transparent); }
@keyframes auto-save-pulse { 50% { opacity: .38; transform: scale(.78); } }
.settings-layout { display: grid; grid-template-columns: 210px minmax(0, 1fr); align-items: start; gap: 24px; }
.settings-nav { position: sticky; top: 0; padding: 10px; border: 1px solid var(--color-border-light, #e4e7ed); border-radius: 18px; background: var(--color-bg-card, #fff); box-shadow: var(--shadow-sm, 0 2px 8px rgba(0,0,0,.06)); }
.settings-nav-group + .settings-nav-group { margin-top: 9px; padding-top: 9px; border-top: 1px solid var(--color-border-light, #ebeef5); }
.settings-nav-title { padding: 5px 12px; color: var(--color-text-tertiary, #909399); font-size: 11px; font-weight: 650; letter-spacing: .08em; }
.settings-nav-item.el-button { display: flex; width: 100%; height: 52px; min-height: 52px; align-items: center; gap: 12px; margin: 0; padding: 14px 14px; border: 0; border-radius: 12px; background: transparent; color: var(--color-text-secondary, #606266); cursor: pointer; font: inherit; font-weight: 500; text-align: left; transition: all .18s ease; }
.settings-nav-item :deep(> span) { width: 100%; justify-content: flex-start; gap: 10px; }
.settings-nav-item:hover { background: var(--color-bg-hover, #f5f7fa); color: var(--color-text-primary, #303133); }
.settings-nav-item.active { background: linear-gradient(145deg, var(--color-accent-light, #66b1ff), var(--color-accent, #409eff)); color: #fff; box-shadow: 0 7px 18px color-mix(in srgb, var(--color-accent, #409eff) 26%, transparent); }
.settings-nav-item .el-icon { width: 20px; height: 20px; font-size: 18px; }
.settings-content { min-width: 0; }
.settings-panel { min-height: 420px; }
.panel-heading { display: flex; align-items: center; gap: 13px; }
.panel-heading h2 { margin: 0 0 4px; color: var(--color-text-primary, #303133); font-size: 18px; }
.panel-heading p { margin: 0; color: var(--color-text-secondary, #909399); font-size: 12px; font-weight: 400; }
.panel-icon { display: grid; width: 38px; height: 38px; flex: 0 0 38px; place-items: center; border-radius: 11px; color: #fff; font-size: 20px; box-shadow: inset 0 1px 0 rgba(255,255,255,.4), 0 5px 12px rgba(34,51,75,.12); }
.panel-icon-blue { background: linear-gradient(145deg,#48a8ff,#0875e5); }
.panel-icon-purple { background: linear-gradient(145deg,#ae83ff,#7450df); }
.panel-icon-orange { background: linear-gradient(145deg,#ffbd59,#f17a26); }
.panel-icon-green { background: linear-gradient(145deg,#67db91,#18a854); }
.panel-icon-red { background: linear-gradient(145deg,#ff7b7b,#df3e4f); }
.panel-icon-cyan { background: linear-gradient(145deg,#55d7e8,#168aa4); }
.panel-icon-teal { background: linear-gradient(145deg,#45d3bd,#168f82); }
.appearance-panel :deep(.el-card__body) { padding-top: 6px; }
.appearance-tabs :deep(.el-tabs__header) { margin: 0 0 22px; }
.appearance-tabs :deep(.el-tabs__nav-wrap::after) { height: 1px; background: var(--color-border-light, #e4e7ed); }
.appearance-tabs :deep(.el-tabs__item) { height: 48px; padding: 0 24px; font-size: 15px; font-weight: 650; }
.appearance-tabs :deep(.el-tabs__active-bar) { bottom: 0; height: 5px; border-radius: 999px; background: var(--color-accent, #409eff); box-shadow: 0 2px 7px color-mix(in srgb, var(--color-accent, #409eff) 34%, transparent); }
.appearance-tab-content { min-height: 360px; padding-top: 2px; }
.dock-tab-content { display: grid; gap: 16px; }
.dock-tab-content :deep(.dock-settings) { margin-top: 0; }
.theme-section { padding: 4px 0; }
.width-section { margin-bottom: 26px; padding-bottom: 26px; border-bottom: 1px solid var(--color-border-light, #ebeef5); }
.width-options { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.width-option.el-button { position: relative; display: flex; min-width: 0; min-height: 0; height: auto; align-items: center; gap: 14px; margin: 0; padding: 14px; border: 1px solid var(--color-border-light, #e4e7ed); border-radius: 14px; background: var(--color-bg-card, #fff); color: inherit; cursor: pointer; font: inherit; text-align: left; transition: border-color .2s ease, box-shadow .2s ease, transform .2s ease; }
.width-option :deep(> span) { display: flex; width: 100%; align-items: center; gap: 14px; }
.width-option:hover { transform: translateY(-1px); border-color: color-mix(in srgb, var(--color-accent, #409eff) 42%, transparent); }
.width-option.active { border-color: var(--color-accent, #409eff); box-shadow: 0 0 0 2px color-mix(in srgb, var(--color-accent, #409eff) 14%, transparent); }
.width-option :deep(> span) > span:nth-child(2) { display: grid; min-width: 0; gap: 4px; }
.width-option strong { color: var(--color-text-primary, #303133); font-size: 14px; }
.width-option small { color: var(--color-text-secondary, #909399); font-size: 12px; line-height: 1.4; }
.width-preview { display: grid; width: 82px; height: 54px; flex: 0 0 82px; grid-template-rows: 10px 1fr; gap: 5px; padding: 6px; border-radius: 8px; background: var(--color-bg-page, #f2f4f7); box-shadow: inset 0 0 0 1px var(--color-border-light, #e4e7ed); }
.width-preview-bar, .width-preview-body { display: block; border-radius: 3px; background: color-mix(in srgb, var(--color-accent, #409eff) 34%, var(--color-bg-card, #fff)); }
.preview-centered .width-preview-bar, .preview-centered .width-preview-body { width: 68%; justify-self: center; }
.width-check { position: absolute; top: 10px; right: 10px; display: grid; width: 20px; height: 20px; place-items: center; border-radius: 50%; background: var(--color-accent, #409eff); color: #fff; font-size: 12px; }
.section-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 22px; }
.section-heading h3, .form-section h3 { margin: 0 0 6px; color: var(--color-text-primary, #303133); font-size: 16px; }
.section-heading p { margin: 0; color: var(--color-text-secondary, #909399); font-size: 13px; }
.theme-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.theme-card.el-button { position: relative; display: block; min-width: 0; min-height: 0; height: auto; margin: 0; padding: 14px; overflow: hidden; border: 1px solid var(--color-border-light, #e4e7ed); border-radius: 16px; background: var(--color-bg-card, #fff); color: inherit; cursor: pointer; font: inherit; text-align: left; white-space: normal; transition: transform .2s ease, border-color .2s ease, box-shadow .2s ease; }
.theme-card :deep(> span) { display: block; width: 100%; }
.theme-card:hover { transform: translateY(-2px); border-color: color-mix(in srgb, var(--color-accent, #409eff) 42%, transparent); box-shadow: var(--shadow-md, 0 8px 24px rgba(0,0,0,.1)); }
.theme-card.active { border-color: var(--color-accent, #409eff); box-shadow: 0 0 0 2px color-mix(in srgb, var(--color-accent, #409eff) 14%, transparent); }
.theme-preview { display: flex; height: 124px; margin-bottom: 12px; overflow: hidden; border: 1px solid var(--color-border-light, #ebeef5); border-radius: 11px; }
.preview-sidebar { display: flex; width: 34px; flex-direction: column; gap: 6px; padding: 8px 6px; }
.preview-dot { width: 20px; height: 20px; border-radius: 5px; }
.preview-line { height: 3px; border-radius: 2px; background: rgba(255,255,255,.3); }
.preview-main { display: flex; min-width: 0; flex: 1; flex-direction: column; }
.preview-header { height: 21px; }
.preview-content { display: flex; flex: 1; gap: 6px; padding: 9px; }
.preview-card { flex: 1; border-radius: 5px; background: rgba(0,0,0,.06); }
.preview-macos26 { position: relative; }
.preview-macos26 .preview-sidebar { position: absolute; z-index: 2; right: 8%; bottom: 7px; left: 18%; width: auto; height: 21px; padding: 3px 5px; flex-direction: row; border-radius: 8px; backdrop-filter: blur(6px); }
.preview-macos26 .preview-dot, .preview-macos26 .preview-line { width: 15px; height: 15px; border-radius: 4px; }
.preview-macos26 .preview-line { background: rgba(100,115,140,.26); }
.preview-macos26 .preview-header { background: rgba(255,255,255,.48) !important; }
.theme-info { display: flex; align-items: center; gap: 8px; margin-bottom: 5px; color: var(--color-text-primary, #303133); }
.theme-icon { font-size: 19px; }
.theme-desc { display: block; padding-right: 20px; color: var(--color-text-secondary, #909399); font-size: 12px; line-height: 1.5; }
.theme-check { position: absolute; right: 12px; bottom: 12px; display: grid; width: 22px; height: 22px; place-items: center; border-radius: 50%; background: var(--color-accent, #409eff); color: #fff; }
.settings-form { max-width: 820px; }
.form-section + .form-section { margin-top: 24px; padding-top: 23px; border-top: 1px solid var(--color-border-light, #ebeef5); }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 18px; }
.number-setting { display: flex; align-items: center; gap: 10px; color: var(--color-text-secondary, #606266); }
@media (max-width: 900px) {
  .settings-layout { display: block; }
  .settings-nav { position: static; display: flex; gap: 7px; margin-bottom: 20px; overflow-x: auto; scrollbar-width: none; }
  .settings-nav::-webkit-scrollbar { display: none; }
  .settings-nav-group { display: contents; }
  .settings-nav-title { display: none; }
  .settings-nav-item.el-button { width: auto; height: 44px; min-height: 44px; flex: 0 0 auto; padding: 10px 13px; }
}
@media (max-width: 640px) {
  .settings-page-header { align-items: flex-start; }
  .settings-page-header p { display: none; }
  .theme-grid, .form-grid, .width-options { grid-template-columns: 1fr; }
  .section-heading { flex-direction: column; }
}
</style>
