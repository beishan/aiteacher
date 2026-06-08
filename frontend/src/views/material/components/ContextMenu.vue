<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="context-menu-overlay"
      @click="close"
      @contextmenu.prevent="close"
    >
      <div
        class="context-menu"
        :style="{ left: x + 'px', top: y + 'px' }"
        @click.stop
      >
        <!-- 文件夹菜单 -->
        <template v-if="material?.isFolder">
          <div class="menu-item" @click="handleAction('open')">
            <el-icon><FolderOpened /></el-icon>
            <span>打开</span>
          </div>
          <div class="menu-item" @click="handleAction('createSubfolder')">
            <el-icon><FolderAdd /></el-icon>
            <span>新建子文件夹</span>
          </div>
          <div class="menu-item" @click="handleAction('upload')">
            <el-icon><Upload /></el-icon>
            <span>上传文件到此文件夹</span>
          </div>
          <div class="menu-divider" />
          <div class="menu-item" @click="handleAction('rename')">
            <el-icon><Edit /></el-icon>
            <span>重命名</span>
          </div>
          <div class="menu-item" @click="handleAction('color')">
            <el-icon><Brush /></el-icon>
            <span>设置颜色</span>
          </div>
          <div class="menu-divider" />
          <div class="menu-item" @click="handleAction('pin')">
            <el-icon><Top /></el-icon>
            <span>{{ material.isPinned ? '取消置顶' : '置顶' }}</span>
          </div>
          <div class="menu-item" @click="handleAction('favorite')">
            <el-icon><Star /></el-icon>
            <span>{{ material.isFavorite ? '取消收藏' : '收藏' }}</span>
          </div>
          <div class="menu-divider" />
          <div class="menu-item" @click="handleAction('copy')">
            <el-icon><CopyDocument /></el-icon>
            <span>复制</span>
          </div>
          <div class="menu-item" @click="handleAction('move')">
            <el-icon><Right /></el-icon>
            <span>移动到...</span>
          </div>
          <div class="menu-divider" />
          <div class="menu-item danger" @click="handleAction('delete')">
            <el-icon><Delete /></el-icon>
            <span>删除</span>
          </div>
        </template>

        <!-- 文件菜单 -->
        <template v-else>
          <div class="menu-item" @click="handleAction('preview')">
            <el-icon><View /></el-icon>
            <span>预览</span>
          </div>
          <div v-if="isEditable" class="menu-item" @click="handleAction('edit')">
            <el-icon><EditPen /></el-icon>
            <span>在线编辑</span>
          </div>
          <div class="menu-item" @click="handleAction('download')">
            <el-icon><Download /></el-icon>
            <span>下载</span>
          </div>
          <div class="menu-divider" />
          <div class="menu-item" @click="handleAction('rename')">
            <el-icon><Edit /></el-icon>
            <span>重命名</span>
          </div>
          <div class="menu-divider" />
          <div class="menu-item" @click="handleAction('pin')">
            <el-icon><Top /></el-icon>
            <span>{{ material.isPinned ? '取消置顶' : '置顶' }}</span>
          </div>
          <div class="menu-item" @click="handleAction('favorite')">
            <el-icon><Star /></el-icon>
            <span>{{ material.isFavorite ? '取消收藏' : '收藏' }}</span>
          </div>
          <div class="menu-item" @click="handleAction('assign')">
            <el-icon><User /></el-icon>
            <span>分配给学生</span>
          </div>
          <div class="menu-divider" />
          <div class="menu-item" @click="handleAction('copy')">
            <el-icon><CopyDocument /></el-icon>
            <span>复制</span>
          </div>
          <div class="menu-item" @click="handleAction('move')">
            <el-icon><Right /></el-icon>
            <span>移动到...</span>
          </div>
          <div class="menu-item" @click="handleAction('info')">
            <el-icon><InfoFilled /></el-icon>
            <span>查看详情</span>
          </div>
          <div class="menu-divider" />
          <div class="menu-item danger" @click="handleAction('delete')">
            <el-icon><Delete /></el-icon>
            <span>删除</span>
          </div>
        </template>
      </div>
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
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 4px 0;
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

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  font-size: 13px;
  color: #303133;
  cursor: pointer;
  transition: background-color 0.15s;
}

.menu-item:hover {
  background-color: #f5f7fa;
}

.menu-item.danger {
  color: #f56c6c;
}

.menu-item.danger:hover {
  background-color: #fef0f0;
}

.menu-item .el-icon {
  font-size: 16px;
  color: #909399;
}

.menu-item.danger .el-icon {
  color: #f56c6c;
}

.menu-divider {
  height: 1px;
  background: #ebeef5;
  margin: 4px 0;
}
</style>
