import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { getSettings, updateSettings } from '@/api/settings'
import type { SystemSetting } from '@/api/settings'

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
} as const

export const useDockStore = defineStore('dock', () => {
  const size = ref(clamp(readNumber('macos26-dock-size', 58), 44, 76))
  const opacity = ref(clamp(readNumber('macos26-dock-opacity', 72), 40, 96))
  const magnification = ref(clamp(readNumber('macos26-dock-magnification', 132), 100, 150))
  const blur = ref(clamp(readNumber('macos26-dock-blur', 28), 8, 40))

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
  }

  function hydrateFromSettings(settings: SystemSetting[]) {
    const values = new Map(settings.map(item => [item.key, item.value]))
    const savedSize = Number(values.get(settingKeys.size))
    const savedOpacity = Number(values.get(settingKeys.opacity))
    const savedMagnification = Number(values.get(settingKeys.magnification))
    const savedBlur = Number(values.get(settingKeys.blur))
    if (Number.isFinite(savedSize)) update('size', savedSize)
    if (Number.isFinite(savedOpacity)) update('opacity', savedOpacity)
    if (Number.isFinite(savedMagnification)) update('magnification', savedMagnification)
    if (Number.isFinite(savedBlur)) update('blur', savedBlur)
  }

  async function hydrateFromServer() {
    try {
      const response = await getSettings()
      hydrateFromSettings(response.data)
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
    })
  }

  return { size, opacity, magnification, blur, cssVars, update, reset, hydrateFromSettings, hydrateFromServer, persist }
})
