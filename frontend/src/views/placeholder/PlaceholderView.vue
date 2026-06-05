<template>
  <div class="placeholder">
    <el-empty description="功能开发中，敬请期待...">
      <el-icon :size="64" color="#c0c4cc"><Tools /></el-icon>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { Tools } from '@element-plus/icons-vue'
</script>

<style scoped>
.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60vh;
}
</style>
