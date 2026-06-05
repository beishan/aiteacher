<template>
  <router-view />
</template>

<script setup lang="ts">
import { useThemeStore } from '@/stores/theme'

// 初始化主题
useThemeStore()
</script>

<style>
@import './styles/themes.css';
@import './styles/minimal-overrides.css';
@import './styles/cyber-overrides.css';

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
</style>
