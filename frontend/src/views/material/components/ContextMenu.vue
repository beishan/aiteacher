<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="context-menu-overlay"
      @click="close"
      @contextmenu.prevent="close"
    >
      <el-menu
        class="context-menu"
        :style="{ left: x + 'px', top: y + 'px' }"
        @select="handleAction"
        @click.stop
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.action"
          :index="item.action"
          :class="{ divided: item.divided, danger: item.danger }"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  FolderOpened, FolderAdd, Upload, Edit, Brush, Top, Star,
  CopyDocument, Right, Delete, View, EditPen, Download,
  User, InfoFilled,
} from '@element-plus/icons-vue'
import type { Material } from '@/api/material'

const props = defineProps<{
  visible: boolean
  x: number
  y: number
  material: Material | null
}>()

const emit = defineEmits<{
  close: []
  action: [action: string, material: Material]
}>()

const editableTypes = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx']
const isEditable = computed(() => {
  if (!props.material || props.material.isFolder) return false
  return editableTypes.includes(props.material.fileType?.toLowerCase() || '')
})

const menuItems = computed(() => {
  if (props.material?.isFolder) {
    return [
      { action: 'open', label: '打开', icon: FolderOpened },
      { action: 'createSubfolder', label: '新建子文件夹', icon: FolderAdd },
      { action: 'upload', label: '上传文件到此文件夹', icon: Upload },
      { action: 'rename', label: '重命名', icon: Edit, divided: true },
      { action: 'color', label: '设置颜色', icon: Brush },
      { action: 'pin', label: props.material.isPinned ? '取消置顶' : '置顶', icon: Top, divided: true },
      { action: 'favorite', label: props.material.isFavorite ? '取消收藏' : '收藏', icon: Star },
      { action: 'copy', label: '复制', icon: CopyDocument, divided: true },
      { action: 'move', label: '移动到...', icon: Right },
      { action: 'delete', label: '删除', icon: Delete, divided: true, danger: true },
    ]
  }

  return [
    { action: 'preview', label: '预览', icon: View },
    ...(isEditable.value ? [{ action: 'edit', label: '在线编辑', icon: EditPen }] : []),
    { action: 'download', label: '下载', icon: Download },
    { action: 'rename', label: '重命名', icon: Edit, divided: true },
    { action: 'pin', label: props.material?.isPinned ? '取消置顶' : '置顶', icon: Top, divided: true },
    { action: 'favorite', label: props.material?.isFavorite ? '取消收藏' : '收藏', icon: Star },
    { action: 'assign', label: '分配给学生', icon: User },
    { action: 'copy', label: '复制', icon: CopyDocument, divided: true },
    { action: 'move', label: '移动到...', icon: Right },
    { action: 'info', label: '查看详情', icon: InfoFilled },
    { action: 'delete', label: '删除', icon: Delete, divided: true, danger: true },
  ]
})

function close() {
  emit('close')
}

function handleAction(action: string) {
  if (props.material) {
    emit('action', action, props.material)
    close()
  }
}
</script>

<style scoped>
.context-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
}

.context-menu {
  position: fixed;
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 4px;
  min-width: 200px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  z-index: 10000;
  animation: menuFadeIn 0.15s ease;
}

@keyframes menuFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.context-menu :deep(.el-menu-item) {
  gap: 10px;
  min-height: 34px;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
  color: var(--el-text-color-primary);
  transition: background-color 0.15s;
}

.context-menu :deep(.el-menu-item:not(.is-disabled):hover) {
  background-color: var(--el-fill-color-light);
}

.context-menu :deep(.el-menu-item.divided) {
  margin-top: 4px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.context-menu :deep(.el-menu-item.danger) {
  color: #f56c6c;
}

.context-menu :deep(.el-menu-item.danger:hover) {
  background-color: var(--el-color-danger-light-9);
}

.context-menu :deep(.el-menu-item .el-icon) {
  font-size: 16px;
  color: var(--el-text-color-secondary);
}

.context-menu :deep(.el-menu-item.danger .el-icon) {
  color: #f56c6c;
}
</style>
