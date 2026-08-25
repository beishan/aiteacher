import { defineStore } from 'pinia'
import { computed, reactive, ref } from 'vue'
import { getDockIcons, getSettings, removeDockIcon, updateSettings, uploadDockIcon } from '@/api/settings'
import type { SystemSetting } from '@/api/settings'
import type { DockIconName } from '@/config/dockItems'

export type DockIconStyle = 'macos26' | 'custom'

const clamp = (value: number, min: number, max: number) => Math.min(max, Math.max(min, value))
const readNumber = (key: string, fallback: number) => {
  const value = Number(localStorage.getItem(key))
  return Number.isFinite(value) && value > 0 ? value : fallback
}

const settingKeys = {
  size: 'ui.dock.size',
  opacity: 'ui.dock.opacity',
  magnification: 'ui.dock.magnification',
  blur: 'ui.dock.blur',
  iconStyle: 'ui.dock.icon_style',
} as const

const dockIconNames: DockIconName[] = [
  'dashboard', 'students', 'schedule', 'homework', 'classrooms', 'grades',
  'finance', 'materials', 'ai-chat', 'statistics', 'backup', 'settings',
]
const iconCacheKey = 'macos26-dock-custom-icons'

const emptyIconRecord = () => Object.fromEntries(dockIconNames.map(name => [name, ''])) as Record<DockIconName, string>

function readIconCache() {
  const icons = emptyIconRecord()
  try {
    const cached = JSON.parse(localStorage.getItem(iconCacheKey) || '{}') as Record<string, unknown>
    dockIconNames.forEach(name => {
      const value = cached[name]
      if (typeof value === 'string' && value.startsWith(`/api/v1/settings/dock-icons/${name}?v=`)) icons[name] = value
    })
  } catch {
    // Ignore malformed legacy browser data.
  }
  return icons
}

function readIconStyle(): DockIconStyle {
  return localStorage.getItem('macos26-dock-icon-style') === 'custom' ? 'custom' : 'macos26'
}

export const useDockStore = defineStore('dock', () => {
  const size = ref(clamp(readNumber('macos26-dock-size', 58), 44, 76))
  const opacity = ref(clamp(readNumber('macos26-dock-opacity', 72), 40, 96))
  const magnification = ref(clamp(readNumber('macos26-dock-magnification', 132), 100, 150))
  const blur = ref(clamp(readNumber('macos26-dock-blur', 28), 8, 40))
  const iconStyle = ref<DockIconStyle>(readIconStyle())
  const iconUrls = reactive<Record<DockIconName, string>>(readIconCache())
  const uploading = reactive<Record<DockIconName, boolean>>(Object.fromEntries(dockIconNames.map(name => [name, false])) as Record<DockIconName, boolean>)

  const cssVars = computed(() => ({
    '--dock-icon-size': `${size.value}px`,
    '--dock-opacity': `${opacity.value / 100}`,
    '--dock-blur': `${blur.value}px`,
  }))

  function update(key: 'size' | 'opacity' | 'magnification' | 'blur', value: number) {
    const ranges = { size: [44, 76], opacity: [40, 96], magnification: [100, 150], blur: [8, 40] } as const
    const next = clamp(value, ranges[key][0], ranges[key][1])
    if (key === 'size') size.value = next
    if (key === 'opacity') opacity.value = next
    if (key === 'magnification') magnification.value = next
    if (key === 'blur') blur.value = next
    localStorage.setItem(`macos26-dock-${key}`, String(next))
  }

  function reset() {
    update('size', 58)
    update('opacity', 72)
    update('magnification', 132)
    update('blur', 28)
    setIconStyle('macos26')
  }

  function setIconStyle(style: DockIconStyle) {
    iconStyle.value = style
    localStorage.setItem('macos26-dock-icon-style', style)
  }

  function hydrateFromSettings(settings: SystemSetting[]) {
    const values = new Map(settings.map(item => [item.key, item.value]))
    const savedSize = Number(values.get(settingKeys.size))
    const savedOpacity = Number(values.get(settingKeys.opacity))
    const savedMagnification = Number(values.get(settingKeys.magnification))
    const savedBlur = Number(values.get(settingKeys.blur))
    const savedIconStyle = values.get(settingKeys.iconStyle)
    if (Number.isFinite(savedSize)) update('size', savedSize)
    if (Number.isFinite(savedOpacity)) update('opacity', savedOpacity)
    if (Number.isFinite(savedMagnification)) update('magnification', savedMagnification)
    if (Number.isFinite(savedBlur)) update('blur', savedBlur)
    if (savedIconStyle === 'macos26' || savedIconStyle === 'custom') setIconStyle(savedIconStyle)
  }

  async function hydrateFromServer() {
    try {
      const [settingsResponse, iconsResponse] = await Promise.all([getSettings(), getDockIcons()])
      hydrateFromSettings(settingsResponse.data)
      Object.assign(iconUrls, emptyIconRecord())
      dockIconNames.forEach(name => {
        const url = iconsResponse.data[name]
        if (typeof url === 'string') iconUrls[name] = url
      })
      localStorage.setItem(iconCacheKey, JSON.stringify(iconUrls))
      return true
    } catch {
      return false
    }
  }

  async function persist(_value?: number | number[]) {
    await updateSettings({
      [settingKeys.size]: String(size.value),
      [settingKeys.opacity]: String(opacity.value),
      [settingKeys.magnification]: String(magnification.value),
      [settingKeys.blur]: String(blur.value),
      [settingKeys.iconStyle]: iconStyle.value,
    })
  }

  async function persistIconStyle(style: DockIconStyle) {
    setIconStyle(style)
    await updateSettings({ [settingKeys.iconStyle]: style })
  }

  async function uploadIcon(name: DockIconName, file: File) {
    if (!['image/png', 'image/jpeg'].includes(file.type)) throw new Error('仅支持 PNG 和 JPG 图片')
    if (file.size > 5 * 1024 * 1024) throw new Error('图标图片不能超过 5MB')
    uploading[name] = true
    try {
      const response = await uploadDockIcon(name, file)
      iconUrls[name] = response.data
      localStorage.setItem(iconCacheKey, JSON.stringify(iconUrls))
    } finally {
      uploading[name] = false
    }
  }

  async function removeIcon(name: DockIconName) {
    uploading[name] = true
    try {
      await removeDockIcon(name)
      iconUrls[name] = ''
      localStorage.setItem(iconCacheKey, JSON.stringify(iconUrls))
    } finally {
      uploading[name] = false
    }
  }

  return {
    size, opacity, magnification, blur, iconStyle, iconUrls, uploading, cssVars,
    update, reset, setIconStyle, hydrateFromSettings, hydrateFromServer, persist,
    persistIconStyle, uploadIcon, removeIcon,
  }
})
