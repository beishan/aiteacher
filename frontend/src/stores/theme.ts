import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUiPreferences, updateSettings } from '@/api/settings'
import type { SystemSetting } from '@/api/settings'

export type ThemeType = 'default' | 'minimal' | 'cyber' | 'macos26'
export type ContentWidth = 'full' | 'centered'

export interface ThemeConfig {
  name: ThemeType
  label: string
  description: string
  icon: string
  preview: {
    bgPrimary: string
    bgSecondary: string
    accent: string
    text: string
  }
}

export const themes: ThemeType[] = ['default', 'minimal', 'cyber', 'macos26']
const THEME_SETTING_KEY = 'ui.theme'
const CONTENT_WIDTH_SETTING_KEY = 'ui.content_width'

function isThemeType(value: unknown): value is ThemeType {
  return typeof value === 'string' && themes.includes(value as ThemeType)
}

function readLocalTheme(): ThemeType {
  const savedTheme = localStorage.getItem('app-theme')
  return isThemeType(savedTheme) ? savedTheme : 'macos26'
}

function isContentWidth(value: unknown): value is ContentWidth {
  return value === 'full' || value === 'centered'
}

function readLocalContentWidth(): ContentWidth {
  const savedWidth = localStorage.getItem('app-content-width')
  return isContentWidth(savedWidth) ? savedWidth : 'full'
}

export const themeConfigs: Record<ThemeType, ThemeConfig> = {
  default: {
    name: 'default',
    label: '经典',
    description: 'Element Plus 默认主题，左侧导航布局',
    icon: '🎨',
    preview: {
      bgPrimary: '#f0f2f5',
      bgSecondary: '#001529',
      accent: '#409EFF',
      text: '#303133',
    },
  },
  minimal: {
    name: 'minimal',
    label: '极简',
    description: '现代极简风格，顶部导航布局',
    icon: '✨',
    preview: {
      bgPrimary: '#ffffff',
      bgSecondary: '#fafafa',
      accent: '#1a1a1a',
      text: '#1a1a1a',
    },
  },
  cyber: {
    name: 'cyber',
    label: '赛博',
    description: '科技未来风格，深色霓虹布局',
    icon: '🔮',
    preview: {
      bgPrimary: '#0a0e1a',
      bgSecondary: '#111827',
      accent: '#3b82f6',
      text: '#e5e7eb',
    },
  },
  macos26: {
    name: 'macos26',
    label: 'macOS 26',
    description: 'Liquid Glass 液态玻璃风格，底部 Dock 导航布局',
    icon: '◉',
    preview: {
      bgPrimary: 'linear-gradient(145deg, #dff4ff 0%, #f5eaff 48%, #fff5e4 100%)',
      bgSecondary: 'rgba(255, 255, 255, 0.55)',
      accent: '#007aff',
      text: '#172033',
    },
  },
}

export const useThemeStore = defineStore('theme', () => {
  // 当前主题
  const currentTheme = ref<ThemeType>(readLocalTheme())
  const contentWidth = ref<ContentWidth>(readLocalContentWidth())

  // 主题配置
  const config = computed(() => themeConfigs[currentTheme.value])

  // 是否为自定义主题（非 Element Plus）
  const isCustomTheme = computed(() => currentTheme.value !== 'default')

  // 布局类型
  const layoutType = computed(() => {
    if (currentTheme.value === 'minimal') return 'top-nav'
    if (currentTheme.value === 'macos26') return 'dock'
    return 'sidebar'
  })

  // 应用主题
  function applyTheme(theme?: ThemeType) {
    const targetTheme = theme || currentTheme.value
    const html = document.documentElement

    // 移除所有主题属性
    html.removeAttribute('data-theme')

    // 设置新主题
    if (targetTheme !== 'default') {
      html.setAttribute('data-theme', targetTheme)
    }

    // 添加主题类名
    html.className = `theme-${targetTheme}`
  }

  // 切换主题
  function setTheme(theme: ThemeType) {
    currentTheme.value = theme
    localStorage.setItem('app-theme', theme)
    applyTheme(theme)
  }

  function applyContentWidth(width: ContentWidth = contentWidth.value) {
    document.documentElement.setAttribute('data-content-width', width)
  }

  function setContentWidth(width: ContentWidth) {
    contentWidth.value = width
    localStorage.setItem('app-content-width', width)
    applyContentWidth(width)
  }

  function hydrateFromSettings(settings: SystemSetting[]) {
    const savedTheme = settings.find(item => item.key === THEME_SETTING_KEY)?.value
    const savedContentWidth = settings.find(item => item.key === CONTENT_WIDTH_SETTING_KEY)?.value
    if (isThemeType(savedTheme)) setTheme(savedTheme)
    if (isContentWidth(savedContentWidth)) setContentWidth(savedContentWidth)
    return isThemeType(savedTheme) || isContentWidth(savedContentWidth)
  }

  async function hydrateFromServer() {
    try {
      const response = await getUiPreferences()
      return hydrateFromSettings(response.data)
    } catch {
      return false
    }
  }

  async function persistTheme(theme: ThemeType = currentTheme.value) {
    await updateSettings({ [THEME_SETTING_KEY]: theme })
  }

  async function persistContentWidth(width: ContentWidth = contentWidth.value) {
    await updateSettings({ [CONTENT_WIDTH_SETTING_KEY]: width })
  }

  // 获取所有主题配置
  function getAllThemes(): ThemeConfig[] {
    return themes.map(t => themeConfigs[t])
  }

  // 初始化时应用主题
  applyTheme()
  applyContentWidth()

  return {
    currentTheme,
    contentWidth,
    config,
    isCustomTheme,
    layoutType,
    setTheme,
    setContentWidth,
    applyTheme,
    hydrateFromSettings,
    hydrateFromServer,
    persistTheme,
    persistContentWidth,
    getAllThemes,
  }
})
