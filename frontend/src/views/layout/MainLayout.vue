<template>
  <div class="main-layout">
    <SideMenu :is-collapse="isCollapse" />
    <div class="main-content">
      <TopBar :is-collapse="isCollapse" @toggle-collapse="isCollapse = !isCollapse" />
      <div class="page-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import SideMenu from './SideMenu.vue'
import TopBar from './TopBar.vue'

const isCollapse = ref(false)
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.page-content {
  flex: 1;
  padding: 20px;
  background: #f0f2f5;
  overflow-y: auto;
}
</style>
