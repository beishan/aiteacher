import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getSetting } from '@/api/settings'
import type { SystemSetting } from '@/api/settings'

const SITE_ICON_SETTING_KEY = 'ui.site_icon_url'
const LOCAL_ICON_KEY = 'site-icon-url'
const DEFAULT_ICON = '/vite.svg'

function applyIcon(url: string) {
  let link = document.querySelector<HTMLLinkElement>('link[rel~="icon"]')
  if (!link) {
    link = document.createElement('link')
    link.rel = 'icon'
    document.head.appendChild(link)
  }
  link.type = url ? 'image/png' : 'image/svg+xml'
  link.href = url || DEFAULT_ICON
}

export const useBrandingStore = defineStore('branding', () => {
  const iconUrl = ref(localStorage.getItem(LOCAL_ICON_KEY) || '')
  applyIcon(iconUrl.value)

  function setIcon(url: string) {
    iconUrl.value = url
    if (url) localStorage.setItem(LOCAL_ICON_KEY, url)
    else localStorage.removeItem(LOCAL_ICON_KEY)
    applyIcon(url)
  }

  function hydrateFromSettings(settings: SystemSetting[]) {
    const url = settings.find(item => item.key === SITE_ICON_SETTING_KEY)?.value
    if (typeof url !== 'string') return false
    setIcon(url)
    return true
  }

  async function hydrateFromServer() {
    try {
      const response = await getSetting(SITE_ICON_SETTING_KEY)
      const url = response.data?.value
      if (typeof url !== 'string') return false
      setIcon(url)
      return true
    } catch {
      return false
    }
  }

  return { iconUrl, setIcon, hydrateFromSettings, hydrateFromServer }
})
