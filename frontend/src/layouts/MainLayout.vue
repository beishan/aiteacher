<template>
  <component :is="currentLayout" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useThemeStore } from '@/stores/theme'
import DefaultLayout from './default/DefaultLayout.vue'
import MinimalLayout from './minimal/MinimalLayout.vue'
import CyberLayout from './cyber/CyberLayout.vue'
import Macos26Layout from './macos26/Macos26Layout.vue'

const themeStore = useThemeStore()

const layoutComponents: Record<string, any> = {
  default: DefaultLayout,
  minimal: MinimalLayout,
  cyber: CyberLayout,
  macos26: Macos26Layout,
}

const currentLayout = computed(() => {
  return layoutComponents[themeStore.currentTheme] || Macos26Layout
})
</script>
