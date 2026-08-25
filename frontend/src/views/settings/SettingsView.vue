<template>
  <div class="settings-view">
    <header class="settings-page-header">
      <div>
        <h1>系统设置</h1>
        <p>管理界面外观、AI 服务、系统通知与用户账号</p>
      </div>
      <el-button v-if="activeTab !== 'users'" type="primary" :icon="Check" :loading="saving" @click="handleSave">保存设置</el-button>
    </header>

    <div class="settings-layout">
      <nav class="settings-nav" aria-label="设置导航">
        <section v-for="group in tabGroups" :key="group.label" class="settings-nav-group">
          <div class="settings-nav-title">{{ group.label }}</div>
          <button
            v-for="item in group.items"
            :key="item.key"
            type="button"
            class="settings-nav-item"
            :class="{ active: activeTab === item.key }"
            :aria-current="activeTab === item.key ? 'page' : undefined"
            @click="activeTab = item.key"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </button>
        </section>
      </nav>

      <main class="settings-content">
        <el-card v-show="activeTab === 'appearance'" class="settings-panel">
          <template #header>
            <div class="panel-heading">
              <span class="panel-icon panel-icon-blue"><el-icon><Monitor /></el-icon></span>
              <div><h2>界面外观</h2><p>选择整体主题，并调整 macOS 26 Dock 的显示效果</p></div>
            </div>
          </template>

          <SiteIconSettings />

          <section class="theme-section">
            <div class="section-heading">
              <div><h3>主题风格</h3><p>不同主题包含独立的导航布局、色彩和组件外观。</p></div>
              <el-tag round effect="light">当前 · {{ themeStore.config.label }}</el-tag>
            </div>

            <div class="theme-grid">
              <button
                v-for="theme in allThemes"
                :key="theme.name"
                type="button"
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
              </button>
            </div>
          </section>

          <transition name="settings-reveal">
            <MacosDockSettings v-if="themeStore.currentTheme === 'macos26'" />
          </transition>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { Bell, Check, DataAnalysis, MagicStick, Monitor, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getSettings, updateSettings } from '@/api/settings'
import MacosDockSettings from '@/components/MacosDockSettings.vue'
import SiteIconSettings from '@/components/SiteIconSettings.vue'
import UserManagementSettings from '@/components/UserManagementSettings.vue'
import { useThemeStore } from '@/stores/theme'
import type { ThemeType } from '@/stores/theme'
import { useDockStore } from '@/stores/dock'
import { useBrandingStore } from '@/stores/branding'
import { useUserStore } from '@/stores/user'

type SettingsTab = 'appearance' | 'ai' | 'notification' | 'stats' | 'users'
const themeStore = useThemeStore()
const dockStore = useDockStore()
const brandingStore = useBrandingStore()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')
const allThemes = themeStore.getAllThemes()
const activeTab = ref<SettingsTab>('appearance')
const saving = ref(false)
const tabGroups = computed(() => [
  { label: '外观', items: [{ key: 'appearance' as const, label: '界面与品牌', icon: Monitor }] },
  { label: '智能服务', items: [{ key: 'ai' as const, label: 'AI 模型', icon: MagicStick }] },
  { label: '系统', items: [
    { key: 'notification' as const, label: '通知配置', icon: Bell },
    { key: 'stats' as const, label: '使用统计', icon: DataAnalysis },
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

async function fetchSettings() {
  try {
    const response = await getSettings()
    for (const item of response.data) {
      if (item.key in settings) settings[item.key] = item.value || ''
    }
    themeStore.hydrateFromSettings(response.data)
    dockStore.hydrateFromSettings(response.data)
    brandingStore.hydrateFromSettings(response.data)
  } catch {
    // request interceptor handles errors
  }
}

async function handleSave() {
  saving.value = true
  try {
    settings['ui.theme'] = themeStore.currentTheme
    settings['ui.dock.size'] = String(dockStore.size)
    settings['ui.dock.opacity'] = String(dockStore.opacity)
    settings['ui.dock.magnification'] = String(dockStore.magnification)
    settings['ui.dock.blur'] = String(dockStore.blur)
    await updateSettings(settings)
    ElMessage.success('设置已保存')
  } finally {
    saving.value = false
  }
}

onMounted(fetchSettings)
</script>

<style scoped>
.settings-view { width: min(1220px, 100%); margin: 0 auto; padding: 4px 0 18px; }
.settings-page-header { display: flex; align-items: center; justify-content: space-between; gap: 20px; margin-bottom: 24px; }
.settings-page-header h1 { margin: 0 0 7px; color: var(--color-text-primary, #303133); font-size: clamp(25px, 3vw, 32px); line-height: 1.2; }
.settings-page-header p { margin: 0; color: var(--color-text-secondary, #909399); font-size: 14px; }
.settings-layout { display: grid; grid-template-columns: 210px minmax(0, 1fr); align-items: start; gap: 24px; }
.settings-nav { position: sticky; top: 0; padding: 10px; border: 1px solid var(--color-border-light, #e4e7ed); border-radius: 18px; background: var(--color-bg-card, #fff); box-shadow: var(--shadow-sm, 0 2px 8px rgba(0,0,0,.06)); }
.settings-nav-group + .settings-nav-group { margin-top: 9px; padding-top: 9px; border-top: 1px solid var(--color-border-light, #ebeef5); }
.settings-nav-title { padding: 5px 12px; color: var(--color-text-tertiary, #909399); font-size: 11px; font-weight: 650; letter-spacing: .08em; }
.settings-nav-item { display: flex; width: 100%; align-items: center; gap: 10px; padding: 11px 12px; border: 0; border-radius: 11px; background: transparent; color: var(--color-text-secondary, #606266); cursor: pointer; font: inherit; font-weight: 500; text-align: left; transition: all .18s ease; }
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
.theme-section { padding: 4px 0; }
.section-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 22px; }
.section-heading h3, .form-section h3 { margin: 0 0 6px; color: var(--color-text-primary, #303133); font-size: 16px; }
.section-heading p { margin: 0; color: var(--color-text-secondary, #909399); font-size: 13px; }
.theme-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.theme-card { position: relative; display: block; min-width: 0; padding: 14px; overflow: hidden; border: 1px solid var(--color-border-light, #e4e7ed); border-radius: 16px; background: var(--color-bg-card, #fff); color: inherit; cursor: pointer; font: inherit; text-align: left; transition: transform .2s ease, border-color .2s ease, box-shadow .2s ease; }
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
.settings-reveal-enter-active, .settings-reveal-leave-active { transition: opacity .2s ease, transform .2s ease; }
.settings-reveal-enter-from, .settings-reveal-leave-to { opacity: 0; transform: translateY(10px); }
@media (max-width: 900px) {
  .settings-layout { display: block; }
  .settings-nav { position: static; display: flex; gap: 7px; margin-bottom: 20px; overflow-x: auto; scrollbar-width: none; }
  .settings-nav::-webkit-scrollbar { display: none; }
  .settings-nav-group { display: contents; }
  .settings-nav-title { display: none; }
  .settings-nav-item { width: auto; flex: 0 0 auto; padding: 10px 13px; }
}
@media (max-width: 640px) {
  .settings-page-header { align-items: flex-start; }
  .settings-page-header p { display: none; }
  .theme-grid, .form-grid { grid-template-columns: 1fr; }
  .section-heading { flex-direction: column; }
}
</style>
