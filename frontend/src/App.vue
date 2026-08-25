<template>
  <router-view />
</template>

<script setup lang="ts">
import { useThemeStore } from '@/stores/theme'
import { useDockStore } from '@/stores/dock'
import { useUserStore } from '@/stores/user'

// 初始化主题
const themeStore = useThemeStore()
const dockStore = useDockStore()
const userStore = useUserStore()

if (userStore.isLoggedIn) {
  void Promise.all([themeStore.hydrateFromServer(), dockStore.hydrateFromServer()])
}
</script>

<style>
@import './styles/themes.css';
@import './styles/minimal-overrides.css';
@import './styles/cyber-overrides.css';
@import './styles/macos26-overrides.css';

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

/* 自定义主题时覆盖字体 */
[data-theme="minimal"] html,
[data-theme="minimal"] body,
[data-theme="minimal"] #app {
  font-family: var(--font-family);
}

[data-theme="cyber"] html,
[data-theme="cyber"] body,
[data-theme="cyber"] #app {
  font-family: var(--font-family);
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
}

[data-theme="macos26"] html,
[data-theme="macos26"] body,
[data-theme="macos26"] #app {
  font-family: var(--font-family);
  background: var(--macos-wallpaper);
  color: var(--color-text-primary);
}
</style>
