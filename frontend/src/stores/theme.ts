import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export type ThemeType = 'default' | 'minimal' | 'cyber'

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

export const themes: ThemeType[] = ['default', 'minimal', 'cyber']

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
}

export const useThemeStore = defineStore('theme', () => {
  // 当前主题
  const currentTheme = ref<ThemeType>(
    (localStorage.getItem('app-theme') as ThemeType) || 'default'
  )

  // 主题配置
  const config = computed(() => themeConfigs[currentTheme.value])

  // 是否为自定义主题（非 Element Plus）
  const isCustomTheme = computed(() => currentTheme.value !== 'default')

  // 布局类型
  const layoutType = computed(() => {
    if (currentTheme.value === 'minimal') return 'top-nav'
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

  // 获取所有主题配置
  function getAllThemes(): ThemeConfig[] {
    return themes.map(t => themeConfigs[t])
  }

  // 初始化时应用主题
  applyTheme()

  return {
    currentTheme,
    config,
    isCustomTheme,
    layoutType,
    setTheme,
    applyTheme,
    getAllThemes,
  }
})
